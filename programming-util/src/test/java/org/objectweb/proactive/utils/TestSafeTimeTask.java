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
