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
package unitTests.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.Sleeper;
import org.objectweb.proactive.core.util.SweetCountDownLatch;

import unitTests.UnitTests;


public class TestSweetCountDownLatch extends UnitTests {

    @Test
    public void test() throws InterruptedException {
        CountDownLatch latch = new SweetCountDownLatch(1);

        T t = new T(latch, Thread.currentThread(), 0);
        new Thread(t).start();
        latch.await();

        Assert.assertEquals(0, latch.getCount());
    }

    @Test
    public void testTimeoutExpired() {
        SweetCountDownLatch latch = new SweetCountDownLatch(1);

        T t = new T(latch, Thread.currentThread(), 10000);
        new Thread(t).start();
        boolean b = latch.await(50, TimeUnit.MILLISECONDS);

        Assert.assertFalse(b);
    }

    @Test
    public void testTimeout() {
        SweetCountDownLatch latch = new SweetCountDownLatch(1);

        T t = new T(latch, Thread.currentThread(), 0);
        new Thread(t).start();
        boolean b = latch.await(10000, TimeUnit.MILLISECONDS);

        Assert.assertTrue(b);
    }

    class T implements Runnable {
        private CountDownLatch latch;
        private Thread waiter;
        private long sleepms;

        public T(CountDownLatch latch, Thread waiter, long sleepms) {
            this.latch = latch;
            this.waiter = waiter;
            this.sleepms = sleepms;
        }

        public void run() {
            new Sleeper(this.sleepms).sleep();

            this.waiter.interrupt();
            Thread.yield();
            this.latch.countDown();

        }
    }
}
