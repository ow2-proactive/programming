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
package org.objectweb.proactive.core.httpserver;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.thread.ThreadPool;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.ThreadPools;


/**
 * An unbounded ThreadPool using Java 5 ThreadPoolExecutor
 * 
 * Set the maximum number of worker to a GINORMOUS value to 
 * mimic the RMI behavior of spawning thread without birth control.
 * 
 * If all the workers of the thread pool are in use, a deadlock can occur
 * due to the HttpTransportServlet.
 * 
 * Each time a message arrives, it is handled by a task submitted to 
 * this thread pool. Each task can perform local or remote calls.
 * If all the workers of the thread pool are in use, a deadlock can occur
 * 
 * Reentrant calls is the most obvious case of deadlock.
 */
public class UnboundedThreadPool implements ThreadPool {

    private final ThreadPoolExecutor threadPool;

    public UnboundedThreadPool() {
        ThreadFactory threadFactory = new NamedThreadFactory("ProActive Http Server Thread", false);
        threadPool = ThreadPools.newCachedThreadPool(1L, TimeUnit.SECONDS, threadFactory);
    }

    @Override
    public int getIdleThreads() {
        return threadPool.getPoolSize() - threadPool.getActiveCount();
    }

    @Override
    public int getThreads() {
        return threadPool.getPoolSize();
    }

    /*
     * Used by Jetty to close idle client when the server is low on resources.
     * 
     * Since maxPoolSize == 2^31 this method will always return false.
     * A side effect could be that Jetty will never close idle connections.
     */
    @Override
    public boolean isLowOnThreads() {
        return threadPool.getActiveCount() >= threadPool.getMaximumPoolSize();
    }

    @Override
    public void join() throws InterruptedException {
        threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    @Override
    public void execute(Runnable command) {
        threadPool.execute(command);
    }

}
