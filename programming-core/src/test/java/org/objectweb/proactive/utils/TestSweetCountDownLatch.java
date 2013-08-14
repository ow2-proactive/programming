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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;
import org.junit.Test;


public class TestSweetCountDownLatch {

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
