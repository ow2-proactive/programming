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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * @author ProActive team
 * @since  5.1.0
 */
public class TestThreadPools {

    static final private Logger logger = Logger.getLogger(TestThreadPools.class.getName());

    @Before
    public void setUp() throws Exception {
        logger.setLevel(Level.DEBUG);
    }

    @Test
    public void testMaxJobsThanWorkers() {
        final int maxThreads = 10;
        ThreadFactory tf = new NamedThreadFactory("pool");
        ExecutorService tpe = ThreadPools.newBoundedThreadPool(maxThreads, 10L, TimeUnit.MILLISECONDS, tf);
        for (int i = 0; i < maxThreads * 10; i++) {
            tpe.execute(new Runnable() {

                public void run() {
                    new Sleeper(10, Logger.getLogger(Sleeper.class.getName())).sleep();
                }
            });
        }

        tpe.shutdownNow();
    }

    @Test
    public void testConcurrency() {
        final int taskLength = 500;
        final int maxThreads = 10;
        final SweetCountDownLatch latch = new SweetCountDownLatch(maxThreads, logger);
        ThreadFactory tf = new NamedThreadFactory("pool");
        ExecutorService tpe = ThreadPools.newBoundedThreadPool(maxThreads, 10L, TimeUnit.MILLISECONDS, tf);
        for (int i = 0; i < maxThreads; i++) {
            tpe.execute(new Runnable() {

                public void run() {
                    latch.countDown();
                    new Sleeper(taskLength, Logger.getLogger(Sleeper.class.getName())).sleep();
                }
            });
        }

        Assert.assertTrue(latch.await(taskLength / 2, TimeUnit.MILLISECONDS));
        for (Thread t : Thread.getAllStackTraces().keySet()) {
            System.out.println(t);
        }

        tpe.shutdownNow();
    }

}
