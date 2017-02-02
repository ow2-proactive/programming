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

import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.remoteobject.benchmark.ThroughputBenchmark;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;

import functionalTests.FunctionalTest;


/**
 * TestMultiProtocolSwitch tests the automatic switching between protocols in the RemoteObjectSet. The switching is triggered by disabling a given protocol
 * on the remote remote object.
 *
 * The test is designed to test all possible switching from a given set of protocols. This is mainly to test robustness
 * of the switching
 *
 * @author The ProActive Team
 */
@Ignore("unstable, mostly on Windows")
public class TestMultiProtocolSwitch extends FunctionalTest {

    URL gcma = TestMultiProtocolSwitch.class.getResource("TestMultiProtocol.xml");

    // remote protocols that will be used, the local protocol will always be the protocol used in the test suite
    ArrayList<String> protocolsToTest = new ArrayList<String>(Arrays.asList(new String[] { "rmi", "pnp", "pamr" }));

    HashSet<List<String>> permutations = new HashSet<List<String>>();

    // pamr router
    static Router router;

    static {
        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).setLevel(Level.DEBUG);
        ProActiveLogger.getLogger(Loggers.PAPROXY).setLevel(Level.DEBUG);
        PAMRConfig.PA_NET_ROUTER_ADDRESS.setValue("localhost");
        PAMRConfig.PA_NET_ROUTER_PORT.setValue(0);
    }

    public TestMultiProtocolSwitch() {
    }

    @BeforeClass
    public static void prepare() throws Exception {
        RouterConfig config = new RouterConfig();
        config.setPort(PAMRConfig.PA_NET_ROUTER_PORT.getValue());
        router = Router.createAndStart(config);
    }

    /**
     * Testing the multi-protocol switching but without benchmarking the protocols
     * @throws Exception
     */
    @Test
    public void testMultiProtocolSwitch() throws Exception {

        CentralPAPropertyRepository.PA_BENCHMARK_ACTIVATE.setValue(false);

        genericTestSwitch();

    }

    /**
     * Testing the multi-protocol switching with concurrent benchmarking of the protocols
     * @throws Exception
     */
    @Test
    public void testMultiProtocolSwitchWithBenchmark() throws Exception {

        CentralPAPropertyRepository.PA_BENCHMARK_ACTIVATE.setValue(true);
        CentralPAPropertyRepository.PA_BENCHMARK_CLASS.setValue(ThroughputBenchmark.class.getName());
        CentralPAPropertyRepository.PA_BENCHMARK_PARAMETER.setValue("" + 500);

        genericTestSwitch();

    }

    @AfterClass
    public static void clean() throws Exception {
        router.stop();
    }

    private void genericTestSwitch() throws Exception {

        MultiProtocolHelper.permute(permutations, (ArrayList<String>) protocolsToTest.clone(), 0);

        for (List<String> proto : permutations) {

            System.out.println("**************** Testing Switching with protocol list : " + proto);

            // we use the facility of the FuntionalTest class to deploy the remote jvm :

            // Here we need to clone the variable contract received from the super class in order to be able to use a new
            // VC at each loop iteration
            VariableContractImpl variableContract = (VariableContractImpl) super.getVariableContract().clone();

            // we remove the value of the proactive.communication.protocol set by the FuntionalTest
            List<String> jvmParameters = super.getJvmParameters();

            Node node = MultiProtocolHelper.deployANodeWithProtocols(proto, gcma, variableContract, jvmParameters);

            AOMultiProtocolSwitch ao = PAActiveObject.newActive(AOMultiProtocolSwitch.class, new Object[0], node);
            String[] aouris = PAActiveObject.getUrls(ao);

            // Ensure that the remote active object is deployed using the correct protocols
            Assert.assertEquals("Number of uris match protocol list size", proto.size(), aouris.length);
            for (int i = 0; i < aouris.length; i++) {
                Assert.assertEquals(aouris[i] + " is protocol " + proto.get(i),
                                    proto.get(i),
                                    (new URI(aouris[i])).getScheme());
            }

            // for each protocol except the last one, run a suite of calls to the test ActiveObject then disable the protocol
            for (int i = 0; i < proto.size() - 1; i++) {
                // wait by necessity
                System.out.println("Async result with wait by necessity : " + ao.foo().getBooleanValue());

                // sync call
                System.out.println("Sync result : " + ao.bar());

                // future 1 already arrived
                BooleanWrapper future1 = ao.foo();

                // longer method call
                System.out.println("Wait call : " + ao.waitPlease());

                // call with exception
                try {
                    ao.throwException();
                } catch (Exception e) {
                    System.out.println("Received SERVER:");
                    e.printStackTrace();
                }
                // sync call after exception
                System.out.println("Sync result 2 : " + ao.bar());

                // future 2 (probaly not arrived yet)
                BooleanWrapper future2 = ao.waitPlease2();

                // automatic continuations
                BooleanWrapper autocont1 = ao.autocont();
                BooleanWrapper autocont2 = ao.autocont2();

                System.out.println("******** Disabling protocol " + proto.get(i));
                // we force the usage of the last protocol in the list, to avoid switching during the disabling process
                PAActiveObject.forceProtocol(ao, proto.get(proto.size() - 1));
                ao.disableProtocol(proto.get(i));
                PAActiveObject.unforceProtocol(ao);
                // switch protocol

                // get the future after switch
                System.out.println("Future 1 already arrived before switch: " + future1.getBooleanValue());
                System.out.println("Future 2 probably not arrived before switch : " + future2.getBooleanValue());
                System.out.println("Automatic continuation 1 (short) " + autocont1.getBooleanValue());
                System.out.println("Automatic continuation 2 (long) " + autocont2.getBooleanValue());
            }
        }
        // the test pass if there are no exceptions
    }
}
