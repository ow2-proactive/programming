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

import org.apache.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.*;


public class TestSleeper {
    static final long SLEEP_TIME = 1000;

    @Test
    public void test() throws InterruptedException {
        final Sleeper sleeper = new Sleeper(SLEEP_TIME, Logger.getLogger(Sleeper.class));
        final long[] times = { 0, 0 };
        Thread sleepyThread = new Thread() {
            @Override
            public void run() {
                times[0] = System.currentTimeMillis();
                sleeper.sleep();
                times[1] = System.currentTimeMillis();
            }
        };
        T t = new T(sleepyThread);
        Thread thread = new Thread(t);
        thread.setDaemon(true);
        thread.start();

        sleepyThread.start();
        sleepyThread.join();
        // -1 is here because System.nanoTime() is more accurate
        // than System.currentTimeMillis(). Rounding errors and multiple interrupts can leads to
        // after - before >= SLEEP_TIME - 100
        assertTrue(times[1] - times[0] >= SLEEP_TIME - 100);
        thread.interrupt();
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
