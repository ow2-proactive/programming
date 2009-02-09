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
        this.exec = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
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
