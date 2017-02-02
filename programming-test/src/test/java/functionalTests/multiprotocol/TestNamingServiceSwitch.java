/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionalTests.multiprotocol;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;

import functionalTests.FunctionalTest;


/**
 * TestNamingServiceSwitch tests the automatic switching between protocols when using the NamingService. The switching is triggered by disabling a given protocol
 * on the remote remote object.
 *
 * The test is designed to test all possible switching from a given set of protocols. This is mainly to test robustness
 * of the switching
 *
 * @author The ProActive Team
 */
public class TestNamingServiceSwitch extends FunctionalTest {

    URL gcma = TestNamingServiceSwitch.class.getResource("TestMultiProtocol.xml");

    // remote protocols that will be used, the local protocol will always be the protocol used in the test suite
    ArrayList<String> protocolsToTest = new ArrayList<String>(Arrays.asList(new String[] { "rmi", "pnp", "pamr" }));

    // pamr router
    static Router router;

    public TestNamingServiceSwitch() {
    }

    @BeforeClass
    static public void prepareForTest() throws Exception {
        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).setLevel(Level.DEBUG);
        ProActiveLogger.getLogger(Loggers.PAPROXY).setLevel(Level.DEBUG);
        PAMRConfig.PA_NET_ROUTER_ADDRESS.setValue("localhost");
        PAMRConfig.PA_NET_ROUTER_PORT.setValue(0);
        FunctionalTest.prepareForTest();
    }

    @BeforeClass
    public static void prepare() throws Exception {
        RouterConfig config = new RouterConfig();
        config.setPort(PAMRConfig.PA_NET_ROUTER_PORT.getValue());
        router = Router.createAndStart(config);
    }

    /**
     * Testing the multi-protocol switching for naming service
     * @throws Exception
     */
    @Test
    public void testNamingServiceSwitch() throws Exception {

        System.out.println("**************** Testing Switching with protocol list : " + protocolsToTest);

        // we use the facility of the FuntionalTest class to deploy the remote jvm :

        // Here we need to clone the variable contract received from the super class in order to be able to use a new
        // VC at each loop iteration
        VariableContractImpl variableContract = (VariableContractImpl) super.getVariableContract().clone();

        // we remove the value of the proactive.communication.protocol set by the FuntionalTest
        List<String> jvmParameters = super.getJvmParameters();

        Node node = MultiProtocolHelper.deployANodeWithProtocols(protocolsToTest,
                                                                 gcma,
                                                                 variableContract,
                                                                 jvmParameters);

        AONamingServiceSwitch ao = PAActiveObject.newActive(AONamingServiceSwitch.class, new Object[0], node);
        String[] aouris = PAActiveObject.getUrls(ao);

        // Ensure that the remote active object is deployed using the correct protocols
        Assert.assertEquals("Number of uris match protocol list size", protocolsToTest.size(), aouris.length);
        for (int i = 0; i < aouris.length; i++) {
            Assert.assertEquals(aouris[i] + " is protocol " + protocolsToTest.get(i),
                                protocolsToTest.get(i),
                                (new URI(aouris[i])).getScheme());
        }

        NamingService namingService = ao.createNamingService();

        PAFuture.waitFor(namingService);

        Set<SpaceInstanceInfo> predefinedSpaces = new HashSet<SpaceInstanceInfo>();
        // for each protocol except the last one, we try to register an empty applications
        for (int i = 0; i < protocolsToTest.size() - 1; i++) {

            namingService.registerApplication(Long.toString(protocolsToTest.get(i).hashCode()), predefinedSpaces);
            System.out.println("******** Disabling protocol " + protocolsToTest.get(i));
            // switch protocol
            ao.disableProtocol(protocolsToTest.get(i));

        }
        namingService.registerApplication(Long.toString(protocolsToTest.get(protocolsToTest.size() - 1).hashCode()),
                                          predefinedSpaces);

        Set<String> registeredApps = namingService.getRegisteredApplications();
        Assert.assertEquals("Number of application registered should match the number of protocols",
                            protocolsToTest.size(),
                            registeredApps.size());

    }

    @AfterClass
    public static void clean() throws Exception {
        router.stop();
    }

}
