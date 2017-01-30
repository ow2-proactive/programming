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
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;

import functionalTests.FunctionalTest;


/**
 * TestMultiExposeActiveObject tests exposing an active object in multi-protocol
 *
 * It focuses on problems which can arise with wrong PAMR configurations or unreachability of the PAMR router
 * It makes sure that, if such problem appears (wrong configuration or unreachability), the framework is able
 * to switch to another protocol
 *
 * @author The ProActive Team
 */
public class TestMultiExposeActiveObjectPAMR extends FunctionalTest {

    URL gcma = TestMultiExposeActiveObjectPAMR.class.getResource("TestMultiProtocol.xml");

    // remote protocols that will be used, the local protocol will always be the protocol used in the test suite
    ArrayList<String> protocolsToTest = new ArrayList<String>(Arrays.asList(new String[] { "rmi", "pamr", "pnp" }));

    // pamr router
    static Router router;

    public TestMultiExposeActiveObjectPAMR() {
    }

    private static void unsetPAMRConfig() {
        if (PAMRConfig.PA_NET_ROUTER_ADDRESS.isSet()) {
            PAMRConfig.PA_NET_ROUTER_ADDRESS.unset();
        }
        if (PAMRConfig.PA_NET_ROUTER_PORT.isSet()) {
            PAMRConfig.PA_NET_ROUTER_PORT.unset();
        }
    }

    private static void startPAMRRouter() throws Exception {
        RouterConfig config = new RouterConfig();
        config.setPort(0); // let him find a free port

        // reduce heartbeat timeout value to improve execution time of
        // test testMultiExposePAMRRouterUnreacheableOnAOClient
        config.setHeartbeatTimeout(3000);

        router = Router.createAndStart(config);
        PAMRConfig.PA_NET_ROUTER_PORT.setValue(router.getPort());
        PAMRConfig.PA_NET_ROUTER_ADDRESS.setValue("localhost");
    }

    @After
    public void stopPAMRRouter() {
        if (router != null) {
            try {
                router.stop();
            } catch (Exception e) {

            }
        }
    }

    /**
     * Testing the multi-protocol exposure with a valid configuration and router access
     * @throws Exception
     */
    @Test
    public void testMultiExposeOK() throws Exception {
        logger.info("************ Multiple Exposure");
        startPAMRRouter();
        Node node = deployANode();
        AOMultiProtocolSwitch ao = genericMultiExpose(node);

        String[] aouris = PAActiveObject.getUrls(ao);

        Assert.assertEquals("Number of uris should match number of protocols", aouris.length, protocolsToTest.size());

        disableProtocol(ao);

        logger.info("***** Calling bar");
        boolean value = ao.bar();
        Assert.assertTrue("Received answer", value);
        stopPAMRRouter();
    }

    /**
     * Testing the multi-protocol exposure with a bad PAMR configuration when trying to expose the AO
     * @throws Exception
     */
    @Test
    public void testMultiExposeBadPAMRConfigOnServerAO() throws Exception {
        logger.info("*********** Multiple Exposure with Bad PAMRConfig occurring on server Active Object");
        unsetPAMRConfig();
        Node node = deployANode();
        AOMultiProtocolSwitch ao = genericMultiExpose(node);
        String[] aouris = PAActiveObject.getUrls(ao);
        ensureURIListHasNoPAMR(Arrays.asList(aouris));

        disableProtocol(ao);

        // the bar method should be called with PAMR
        logger.info("***** Calling bar");
        boolean value = ao.bar();
        Assert.assertTrue("Received answer", value);
    }

    /**
     * Testing the multi-protocol exposure with a PAMR router unreachable when trying to expose the AO
     * @throws Exception
     */
    @Test
    public void testMultiExposePAMRRouterUnreacheableFromServerAO() throws Exception {
        logger.info("************ Multiple Exposure with Router unreachable for server Active Object");
        Node node = deployANode();
        AOMultiProtocolSwitch ao = genericMultiExpose(node);
        String[] aouris = PAActiveObject.getUrls(ao);
        ensureURIListHasNoPAMR(Arrays.asList(aouris));

        disableProtocol(ao);

        // the bar method should be called with the last protocol available
        logger.info("***** Calling bar");
        boolean value = ao.bar();
        Assert.assertTrue("Received answer", value);
    }

    /**
     * Testing the multi-protocol exposure with a bad PAMR configuration on the client side (the AO is exposed normally)
     * @throws Exception
     */
    @Test
    public void testMultiExposeBadPAMRConfigOnAOClient() throws Exception {
        logger.info("*********** Multiple Exposure with Bad PAMRConfig occurring on client Active Object");
        // we first need to set the config and start the router so the remote ao can start properly
        startPAMRRouter();
        Node node = deployANode();
        AOMultiProtocolSwitch ao = genericMultiExpose(node);

        // after the ao is started, we unset the config locally
        unsetPAMRConfig();

        disableProtocol(ao);

        // the bar method should be called with the last protocol available
        logger.info("***** Calling bar");
        boolean value = ao.bar();
        Assert.assertTrue("Received answer", value);
    }

    /**
     * Testing the multi-protocol exposure with a PAMR router unreachable on the client side (the AO is exposed normally)
     * @throws Exception
     */
    @Test
    public void testMultiExposePAMRRouterUnreacheableOnAOClient() throws Exception {
        logger.info("************ Multiple Exposure with Router unreachable for client Active Object");
        startPAMRRouter();
        Node node = deployANode();
        AOMultiProtocolSwitch ao = genericMultiExpose(node);
        stopPAMRRouter();

        disableProtocol(ao);

        logger.info("***** Calling bar");
        boolean value = ao.bar();
        Assert.assertTrue("Received answer", value);
    }

    /**
     * helper function which makes sure that there is no PAMR protocol in the given uri list
     * @param inputList
     * @throws Exception
     */
    private void ensureURIListHasNoPAMR(List<String> inputList) throws Exception {
        for (String uriString : inputList) {
            URI uri = new URI(uriString);
            Assert.assertFalse("PAMR protocol not found in uri : " + uriString, "pamr".equals(uri.getScheme()));
        }
    }

    /**
     * Deploys a remote ProActive Node
     * @return
     * @throws Exception
     */
    private Node deployANode() throws Exception {
        // we use the facility of the FuntionalTest class to deploy the remote jvm :

        // Here we need to clone the variable contract received from the super class in order to be able to use a new
        // VC at each loop iteration
        VariableContractImpl variableContract = (VariableContractImpl) super.getVariableContract().clone();

        // we remove the value of the proactive.communication.protocol set by the FuntionalTest
        List<String> jvmParameters = super.getJvmParameters();

        return MultiProtocolHelper.deployANodeWithProtocols(protocolsToTest, gcma, variableContract, jvmParameters);
    }

    /**
     * Creates the active object
     * @param node
     * @return
     * @throws Exception
     */
    private AOMultiProtocolSwitch genericMultiExpose(Node node) throws Exception {
        return PAActiveObject.newActive(AOMultiProtocolSwitch.class, new Object[0], node);
    }

    /**
     * Disable the default protocol
     * @param ao
     * @throws Exception
     */
    private void disableProtocol(AOMultiProtocolSwitch ao) throws Exception {
        // In order to avoid that switching occurs during the disabling process,
        // we do the disabling via the last protocol in the list
        PAActiveObject.forceProtocol(ao, protocolsToTest.get(protocolsToTest.size() - 1));
        ao.disableProtocol(protocolsToTest.get(0));
        PAActiveObject.unforceProtocol(ao);
    }

}
