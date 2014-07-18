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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author ProActive team
 * @since  ProActive 5.1.0
 */
public class TestSafeTimeTask {

    static final private Logger logger = Logger.getLogger(TestSafeTimeTask.class.getName());

    @Before
    public void setUp() throws Exception {
        logger.setLevel(Level.DEBUG);
    }

    @Test
    public void testThreadIsNotKilled() {
        class Task extends SafeTimerTask {
            final SweetCountDownLatch latch;

            public Task(SweetCountDownLatch latch) {
                this.latch = latch;
            }

            @Override
            public void safeRun() {
                this.latch.countDown();
                throw new NullPointerException();
            }
        }

        SweetCountDownLatch latch = new SweetCountDownLatch(2, logger);
        TimerTask tt = new Task(latch);
        Timer timer = new Timer();
        timer.schedule(tt, 0, 10);

        Assert.assertTrue(latch.await(10, TimeUnit.SECONDS));
        timer.cancel();
    }

    @Test(expected = NullPointerException.class)
    public void testLoggerNull() {
        class Task extends SafeTimerTask {

            public Task() {
                super(null);
            }

            @Override
            public void safeRun() {
            }
        }
        new Task();
    }

    @Test(expected = NullPointerException.class)
    public void testLevelNull() {
        class Task extends SafeTimerTask {

            public Task() {
                super(Logger.getLogger("logger"), null);
            }

            @Override
            public void safeRun() {
            }
        }
        new Task();
    }

}
