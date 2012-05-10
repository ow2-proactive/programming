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
package unitTests;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.utils.Sleeper;
import org.objectweb.proactive.utils.StoppableThread;
import org.objectweb.proactive.utils.SweetCountDownLatch;
import org.objectweb.proactive.utils.StoppableThread.NotStoppedException;


/**
 * @author ProActive team
 * @since  ProActive 5.0.0
 */
public class TestStoppableThread {

    @Test
    public void fine() throws NotStoppedException {
        StoppableThread t = new StoppableThread() {
            @Override
            public void action() {

            }
        };

        t.start();
        Assert.assertFalse(t.isStopped());
        Assert.assertNull(t.getError());
        Assert.assertFalse(t.exitedOnError());
        t.terminate(1, TimeUnit.SECONDS);
        Assert.assertTrue(t.isStopped());
        Assert.assertFalse(t.exitedOnError());
        Assert.assertNull(t.getError());
    }

    @Test
    public void withException() throws NotStoppedException {
        final SweetCountDownLatch latch = new SweetCountDownLatch(1);

        StoppableThread t = new StoppableThread() {
            @Override
            public void action() {
                RuntimeException e = new RuntimeException("Oh noooooooooo");
                latch.countDown();
                throw e;
            }
        };

        Assert.assertFalse(t.isStopped());
        Assert.assertNull(t.getError());
        Assert.assertFalse(t.exitedOnError());

        t.start();
        new Sleeper(10).sleep(); // avoid race condition between countdown and exception

        Assert.assertTrue(t.isStopped());
        Assert.assertTrue(t.exitedOnError());
        Assert.assertNotNull(t.getError());

        t.terminate(1, TimeUnit.SECONDS);
    }

    @Test
    public void unresponsive() throws NotStoppedException {
        final SweetCountDownLatch latch = new SweetCountDownLatch(1);

        StoppableThread t = new StoppableThread() {
            @Override
            public void action() {
                latch.countDown();
                new Sleeper(500).sleep();
            }
        };

        Assert.assertFalse(t.isStopped());
        Assert.assertNull(t.getError());
        Assert.assertFalse(t.exitedOnError());

        t.start();
        new Sleeper(10).sleep(); // avoid race condition between countdown and sleep

        try {
            t.terminate(100, TimeUnit.MILLISECONDS);
            Assert.fail("Thread is unresponsive. An exception should be thrown.");
        } catch (NotStoppedException e) {
            e.printStackTrace();
        }

        Assert.assertFalse(t.isStopped());
        Assert.assertNull(t.getError());
        Assert.assertFalse(t.exitedOnError());

        new Sleeper(500).sleep();

        t.terminate(100, TimeUnit.MILLISECONDS);
        Assert.assertTrue(t.isStopped());
        Assert.assertFalse(t.exitedOnError());
        Assert.assertNull(t.getError());
        t.terminate(100, TimeUnit.MILLISECONDS);
    }

}
