/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.pnp;

import functionalTests.FunctionalTest;
import functionalTests.GCMFunctionalTest;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet.NotYetExposedException;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.pnp.PNPConfig;


public class TestPNPTimeout extends GCMFunctionalTest {

    static final int TEST_HEART_BEAT = 1000;

    static final int TEST_FACTOR = 3;

    static final long WAIT_TIME = 60000;

    @BeforeClass
    static public void prepareForTest() throws Exception {
        CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.setValue("pnp");
        PNPConfig.PA_PNP_DEFAULT_HEARTBEAT.setValue(TEST_HEART_BEAT);
        PNPConfig.PA_PNP_HEARTBEAT_FACTOR.setValue(TEST_FACTOR);
        ProActiveLogger.getLogger(PNPConfig.Loggers.PNP).setLevel(Level.TRACE);
        FunctionalTest.prepareForTest();
    }

    public TestPNPTimeout() throws ProActiveException {
        super(1, 1);

        super.setOptionalJvmParamters(PNPConfig.PA_PNP_DEFAULT_HEARTBEAT.getCmdLine() + TEST_HEART_BEAT +
            " " + PNPConfig.PA_PNP_HEARTBEAT_FACTOR.getCmdLine() + TEST_FACTOR);
        super.startDeployment();
    }

    /**
     * This test ensures that:
     * - no timeout exception is raised when adding a progressively growing delay to an active object pnp server (heartbeat timeouts raise proportionally)
     * - no timeout exception is raised with the same delay on a second active object server on the same proactive node
     * - after removing this delay, a serie of method calls work and it can be observed in the logs that the pnp heartbeat timeouts have decreased.
     * (this last bit can be observed in the log, but it seems not possible to check this via assertions).
     */
    @Test(timeout = 300000)
    public void testGrowingDelay() throws ActiveObjectCreationException, NodeException,
            UnknownProtocolException, NotYetExposedException, InterruptedException {

        System.out.println("Starting test: growing delay");
        Node node = super.getANode();

        GrowingDelayAO remote = PAActiveObject.newActive(GrowingDelayAO.class, new Object[0], node);

        System.out.println("Start growing delay");
        // call to this method will generate a growing delay on the server
        BooleanWrapper future = remote.growingDelay();

        try {
            Assert.assertTrue(PAFuture.getFutureValue(future).getBooleanValue());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("After these growing delays were created, no timeout exception should be raised when receiving the method result.");
        }

        // the server delay should be maximum
        GrowingDelayAO remote2 = PAActiveObject.newActive(GrowingDelayAO.class, new Object[0], node);

        Assert.assertTrue("Call to the second active object, with a big delay should not raise exceptions",
                remote2.simpleMessage());

        remote.removeDelay();
        // the server delay is now set to 0
        System.out.println("End growing delay");

        for (int i = 0; i < 20; i++) {
            Thread.sleep(1000);
            Assert.assertTrue(
                    "Subsequent calls on the first active object with delay reset should not raise exceptions",
                    remote.simpleMessage());
            Assert.assertTrue(
                    "Subsequent calls on the second active object with delay reset should not raise exceptions",
                    remote2.simpleMessage());
        }
        System.out.println("End test: growing delay");
    }

    @After
    public void releaseNodes() throws Throwable {
        killDeployment();
    }

    /**
     * This test ensures that a timeout exception is raised when a big delay is suddenly added to an active object's pnp server.
     */
    @Test(timeout = 120000)
    public void testBigDelay() throws ActiveObjectCreationException, NodeException, UnknownProtocolException,
            NotYetExposedException, InterruptedException {
        System.out.println("Starting test: big delay");
        Node node = super.getANode();

        GrowingDelayAO remote = PAActiveObject.newActive(GrowingDelayAO.class, new Object[0], node);

        BooleanWrapper future = remote.addBigDelay();

        try {
            PAFuture.getFutureValue(future).getBooleanValue();
            Assert.fail("Due to the big delay, an expection should have been raised.");
        } catch (Exception e) {
            System.out.println("The following exception is expected:");
            e.printStackTrace(System.out);
        }
        System.out.println("End test: big delay");
    }

    private static void simulateServerGrowingDelay() {

        long beginTime = System.currentTimeMillis();
        int startperiod = TEST_HEART_BEAT;
        try {
            int i = 0;
            while (System.currentTimeMillis() - beginTime < WAIT_TIME) {
                PNPConfig.PA_PNP_TEST_RANDOMDELAY.setValue(startperiod / 4 * i);
                Thread.sleep(startperiod / 4 * i * TEST_FACTOR);
                i++;
            }
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    @ActiveObject
    public static class GrowingDelayAO {

        public GrowingDelayAO() {
            // Do nothing
        }

        public boolean simpleMessage() {
            return true;
        }

        public boolean removeDelay() {
            PNPConfig.PA_PNP_TEST_RANDOMDELAY.setValue(0);
            logger.info("Removed delay");
            return true;
        }

        public BooleanWrapper addBigDelay() {
            PNPConfig.PA_PNP_TEST_RANDOMDELAY.setValue(30000);
            logger.info("Added big delay");
            try {
                Thread.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                logger.info("", e);
            }
            return new BooleanWrapper(true);
        }

        public BooleanWrapper growingDelay() {
            logger.info("Start growing delay");
            simulateServerGrowingDelay();
            logger.info("Maximum delay");
            return new BooleanWrapper(true);
        }
    }
}
