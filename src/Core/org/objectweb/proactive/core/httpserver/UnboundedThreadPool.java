/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.httpserver;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mortbay.thread.ThreadPool;


/** An unbounded ThreadPool using Java 5 ThreadPoolExecutor
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
 * 
 */
class UnboundedThreadPool implements ThreadPool {

    private final ThreadPoolExecutor exec;

    public UnboundedThreadPool() {
        this.exec = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 1L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
    }

    public boolean dispatch(Runnable job) {
        exec.execute(job);
        return true;
    }

    /* Never used in Jetty 6.0 */
    public int getIdleThreads() {
        return exec.getPoolSize() - exec.getActiveCount();
    }

    /* Never used in Jetty 6.0 */
    public int getThreads() {
        return exec.getPoolSize();
    }

    /* Used by Jetty to close idle client when the server is low on resources.
     * 
     * Since maxPoolSize == 2^31 this method will always return false. 
     * A side effect could be that Jetty will never close idle connections.
     */
    public boolean isLowOnThreads() {
        return exec.getActiveCount() >= exec.getMaximumPoolSize();
    }

    public void join() throws InterruptedException {
        exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}
