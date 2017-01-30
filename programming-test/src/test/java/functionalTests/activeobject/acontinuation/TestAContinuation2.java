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
package functionalTests.activeobject.acontinuation;

import org.apache.log4j.Level;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.GCMFunctionalTest;


/**
 * Test checks that after fix for the "PROACTIVE-1226  Need to automatically cleanup unused HalfBodies"
 * it is still possible to get future value after thread created this future finished and its HalfBody
 * was removed.
 *  
 * @author ProActive team
 *
 */
public class TestAContinuation2 extends GCMFunctionalTest {

    static {
        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).setLevel(Level.DEBUG);
        ProActiveLogger.getLogger(Loggers.PAPROXY).setLevel(Level.DEBUG);
    }

    public TestAContinuation2() throws Exception {
        super(1, 1);
        super.startDeployment();
    }

    public static class TestAO {

        public StringWrapper asyncCall() {
            try {
                Thread.sleep(5000);
                return new StringWrapper("futureResult");
            } catch (Exception e) {
                Assert.fail("Unexpected error: " + e);
                return null;
            }
        }
    }

    static class TestThread1 extends Thread {

        private final TestAO ao;

        private StringWrapper result;

        private UniqueID id;

        TestThread1(TestAO ao) {
            this.ao = ao;
        }

        public void run() {
            result = ao.asyncCall();

            id = PAActiveObject.getBodyOnThis().getID();
        }

    }

    static class TestThread2 extends Thread {

        private final StringWrapper stringFuture;

        TestThread2(StringWrapper stringFuture) {
            this.stringFuture = stringFuture;
        }

        public void run() {
            try {
                // trigger removal of the half body for the TestThread1
                LocalBodyStore.getInstance().getContext();

                Assert.assertTrue("Result should be awaited", PAFuture.isAwaited(stringFuture));
                Assert.assertEquals("futureResult", stringFuture.getStringValue());
            } catch (Exception e) {
                e.printStackTrace();
                Assert.fail("Unexpected error: " + e);
            }
        }

    }

    @Test
    public void testLocalAO() throws Exception {
        // test with local active object
        doTest(true);
    }

    @Test
    public void testRemoteAO() throws Exception {
        // test with remote active object
        doTest(false);
    }

    private void doTest(boolean localAO) throws Exception {
        TestAO ao;
        if (localAO) {
            ao = PAActiveObject.newActive(TestAO.class, new Object[] {});
        } else {
            Node node = getANode();
            ao = PAActiveObject.newActive(TestAO.class, new Object[] {}, node);
        }

        // thread1 creates future result and terminates
        TestThread1 thread1 = new TestThread1(ao);
        thread1.setName("TestThread1");
        thread1.start();
        thread1.join();

        Assert.assertNotNull(thread1.result);
        Assert.assertNotNull(thread1.id);

        // thread2 tries to get future result
        TestThread2 thread2 = new TestThread2(thread1.result);
        thread2.setName("TestThread2");
        thread2.start();
        thread2.join();

        // make sure that the body store doesn't contain the half body of thread1
        Assert.assertNull(LocalBodyStore.getInstance().getLocalHalfBodies().getBody(thread1.id));
    }

}
