/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package functionalTests.activeobject.acontinuation;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.node.Node;
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

        TestThread1(TestAO ao) {
            this.ao = ao;
        }

        public void run() {
            result = ao.asyncCall();
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
                Assert.fail("Unexpected error: " + e);
            }
        }

    }

    @Test
    public void test() throws Exception {
        // test with local active object
        doTest(true);

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

        // thread2 tries to get future result
        TestThread2 thread2 = new TestThread2(thread1.result);
        thread2.setName("TestThread2");
        thread2.start();
        thread2.join();
    }

}
