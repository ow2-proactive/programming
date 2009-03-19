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

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.Sleeper;

import unitTests.UnitTests;


public class TestSleeper extends UnitTests {
    static final long SLEEP_TIME = 1000;

    @Test
    public void test() {
        Sleeper sleeper = new Sleeper(SLEEP_TIME);

        T t = new T(Thread.currentThread());
        Thread thread = new Thread(t);
        thread.setDaemon(true);
        thread.start();

        long before = System.currentTimeMillis();
        sleeper.sleep();
        long after = System.currentTimeMillis();

        logger.info("Spleeped " + (after - before) + " expected " + SLEEP_TIME);
        // -1 is here because System.nanoTime() is more accurate
        // than System.currentTimeMillis(). Rouding errors can leads to
        // after - before == SLEEP_TIME - 1
        Assert.assertTrue(after - before >= SLEEP_TIME - 1);
    }

    private class T implements Runnable {
        private Thread sleeper;

        public T(Thread sleeper) {
            this.sleeper = sleeper;
        }

        public void run() {
            while (true) {
                this.sleeper.interrupt();
            }
        }

    }
}
