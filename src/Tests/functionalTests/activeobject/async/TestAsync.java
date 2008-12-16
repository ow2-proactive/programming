/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.activeobject.async;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.TimeoutAccounter;

import functionalTests.FunctionalTest;


public class TestAsync extends FunctionalTest {
    final static int TIMEOUT = 500;
    final static int ESPYLON = (int) (TIMEOUT / 10);

    static AO ao;

    public TestAsync() {
    }

    @BeforeClass
    static public void createAO() throws ActiveObjectCreationException, NodeException {
        ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] { TIMEOUT });
    }

    @Before
    public void waitServiceQueueIsEmpty() {
        // For async test we have to wait the end of the previous service
        try {
            ao.waitEndOfService();
        } catch (Exception e) {
            logger.warn(e.getCause(), e);
        }
        logger.info("Service queue is empty");
    }

    // async
    @Test
    public void testVoid() throws ActiveObjectCreationException, NodeException {
        long before = System.currentTimeMillis();
        ao.m_void();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be synchronous but should be async",
                after - before < TIMEOUT + ESPYLON);
    }

    // sync
    @Test
    public void testVoidWithException() throws Exception {
        long before = System.currentTimeMillis();
        ao.m_void_exception();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);
    }

    // sync
    @Test
    public void testChar() {
        long before = System.currentTimeMillis();
        ao.m_char();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);
    }

    // sync
    @Test
    public void testShort() {
        long before = System.currentTimeMillis();
        ao.m_short();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);

    }

    // sync
    @Test
    public void testInt() {
        long before = System.currentTimeMillis();
        ao.m_int();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);

    }

    // sync
    @Test
    public void testLong() {
        long before = System.currentTimeMillis();
        ao.m_long();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);

    }

    // sync
    @Test
    public void testFinal() {
        long before = System.currentTimeMillis();
        ao.m_final();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);
    }

    // async
    @Test
    public void testNonFinal() {
        long before = System.currentTimeMillis();
        ao.m_non_final();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue(ao instanceof StubObject);
        Assert.assertTrue("Method call seems to be synchronous but should be async",
                after - before < TIMEOUT + ESPYLON);
    }

    // sync
    @Test
    public void testNonFinalWithException() throws Exception {
        long before = System.currentTimeMillis();
        ao.m_non_final_exception();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue(ao instanceof StubObject);
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);
    }

    // sync
    @Test
    public void testStaticFinal() {
        long before = System.currentTimeMillis();
        ao.m_static_final();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);
    }

    // async
    @Test
    public void testStaticNonFinal() {
        long before = System.currentTimeMillis();
        ao.m_static_non_final();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue(ao instanceof StubObject);
        Assert.assertTrue("Method call seems to be synchronous but should be async",
                after - before < TIMEOUT + ESPYLON);
    }

    // sync
    @Test
    public void testStaticNonFinalWithException() throws Exception {
        long before = System.currentTimeMillis();
        ao.m_static_non_final_exception();
        long after = System.currentTimeMillis();

        logger.info("Waited " + (after - before) + "ms");
        Assert.assertTrue(ao instanceof StubObject);
        Assert.assertTrue("Method call seems to be async but should be sync", after - before > TIMEOUT -
            ESPYLON);
    }

    public static class AO {
        int timeout = -1;

        public AO() {
        }

        public AO(int timeout) {
            this.timeout = timeout;
        }

        // Async
        public void m_void() {
            sleep();
        }

        // Sync
        public void m_void_exception() throws Exception {
            sleep();
        }

        public char m_char() {
            sleep();
            return 'c';
        }

        public short m_short() {
            sleep();
            return 1;
        }

        public int m_int() {
            sleep();
            return 1;
        }

        public long m_long() {
            sleep();
            return 1;
        }

        public FinalClass m_final() {
            sleep();
            return new FinalClass();
        }

        public NonFinalClass m_non_final() {
            sleep();
            return new NonFinalClass();
        }

        public NonFinalClass m_non_final_exception() throws Exception {
            sleep();
            return new NonFinalClass();
        }

        public StaticFinalClass m_static_final() {
            sleep();
            return new StaticFinalClass();
        }

        public StaticNonFinalClass m_static_non_final() {
            sleep();
            return new StaticNonFinalClass();
        }

        public StaticNonFinalClass m_static_non_final_exception() throws Exception {
            sleep();
            return new StaticNonFinalClass();
        }

        private void sleep() {
            TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
            do {
                try {
                    Thread.sleep(time.getRemainingTimeout());
                } catch (InterruptedException e) {
                    // miam miam miam
                }
            } while (!time.isTimeoutElapsed());
        }

        public int waitEndOfService() throws Exception {
            // Assume that is call is synchronous
            return 0;
        }
    }

    @SuppressWarnings("serial")
    final static public class StaticFinalClass extends Object implements Serializable {
    }

    @SuppressWarnings("serial")
    static public class StaticNonFinalClass extends Object implements Serializable {
    }
}
