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

import java.lang.reflect.Method;
import java.util.concurrent.*;


/**
 * Factory and utility methods to create thread pools.
 * 
 * <p>It is a counterpart of the {@link Executors} class.</p>
 * 
 * @see Executors
 * @author ProActive team
 * @since  5.1.0
 */
final public class ThreadPools {

    /**
     * Creates a thread pool that creates up to maxThreads thread.
     * 
     * <p>On Java 6 and later the thread pool creates new threads as needed, 
     * but will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to <tt>execute</tt> will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool until the pool reach its
     * maximum size. Threads that have not been used for sixty seconds are 
     * terminated and removed from the cache. Thus, a pool that remains idle 
     * for long enough will not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.</p>
     *
     * <p><b>Warning</b>Java 5 does not allow to create a such thread pool. As a fall-back
     * a thread with fixed size of maxThreads is created.</p> 
     * 
     * @param maxThreads
     *    the maximum number of thread to create
     * @return
     *    the newly created thread pool
     * @throws IllegalArgumentException 
     *    if <tt>nThreads &lt;= 0</tt>
     */
    public static ThreadPoolExecutor newBoundedThreadPool(int maxThreads) {
        return newBoundedThreadPool(maxThreads, Executors.defaultThreadFactory());
    }

    /**
     * Creates a thread pool that creates up to maxThreads thread.
     * 
     * <p>On Java 6 and later the thread pool creates new threads as needed, 
     * but will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to <tt>execute</tt> will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool until the pool reach its
     * maximum size. Threads that have not been used for sixty seconds are 
     * terminated and removed from the cache. Thus, a pool that remains idle 
     * for long enough will not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.</p>
     * 
     * <p><b>Warning</b>Java 5 does not allow to create a such thread pool. As a fall-back
     * a thread with fixed size of maxThreads is created.</p>
     * 
     * @param maxThreads
     *    the maximum number of thread to create
     * @param threadFactory
     *    the factory to use when creating new threads
     * @return
     *    the newly created thread pool
     * @throws NullPointerException 
     *    if threadFactory is null
     * @throws IllegalArgumentException 
     *    if <tt>nThreads &lt;= 0</tt>
     */
    public static ThreadPoolExecutor newBoundedThreadPool(int maxThreads, ThreadFactory threadFactory) {
        return newBoundedThreadPool(maxThreads, 60L, TimeUnit.SECONDS, threadFactory);
    }

    /**
     * Creates a thread pool that creates up to maxThreads thread.
     * 
     * <p>On Java 6 and later the thread pool creates new threads as needed, 
     * but will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to <tt>execute</tt> will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool until the pool reach its
     * maximum size. Threads that have not been used for a given amount of time are 
     * terminated and removed from the cache. Thus, a pool that remains idle 
     * for long enough will not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.</p>
     * 
     * <p><b>Warning</b>Java 5 does not allow to create a such thread pool. As a fall-back
     * a thread with fixed size of maxThreads is created.</p>
     * 
     * @param maxThreads
     *    the maximum number of thread to create
     * @param threadFactory
     *    the factory to use when creating new threads
     * @param keepAliveTime 
     *    this is the maximum time that idle threads
     *    will wait for new tasks before terminating.
     * @param unit 
     *    the time unit for the {@code keepAliveTime} argument
     * @return
     *    the newly created thread pool
     * @throws NullPointerException 
     *    if threadFactory or unit is null
     * @throws IllegalArgumentException 
     *    if <tt>nThreads &lt;= 0</tt> or <tt>keepAlivetime &lt; 0</tt>
     */
    public static ThreadPoolExecutor newBoundedThreadPool(int maxThreads, long keepAliveTime, TimeUnit unit,
            ThreadFactory threadFactory) {
        ArgCheck.requireStrictlyPostive(maxThreads);
        ArgCheck.requirePostive(keepAliveTime);
        ArgCheck.requireNonNull(unit);
        ArgCheck.requireNonNull(threadFactory);

        final ThreadPoolExecutor tpe;
        tpe = new ThreadPoolExecutor(maxThreads, maxThreads, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>());

        // Enable dynamic sizing by allowing core threads to terminate on timeout
        tpe.allowCoreThreadTimeOut(true);

        return tpe;
    }

    /**
     * Creates a thread pool that reuses a fixed number of threads
     * operating off a shared unbounded queue, using the provided
     * ThreadFactory to create new threads when needed.  At any point,
     * at most <tt>nThreads</tt> threads will be active processing
     * tasks.  If additional tasks are submitted when all threads are
     * active, they will wait in the queue until a thread is
     * available.  If any thread terminates due to a failure during
     * execution prior to shutdown, a new one will take its place if
     * needed to execute subsequent tasks.  The threads in the pool will
     * exist until it is explicitly {@link ExecutorService#shutdown
     * shutdown}.
     * 
     * <p>This method is the same than {@link Executors#newFixedThreadPool(int, ThreadFactory)}
     * but returns a {@link ThreadPoolExecutor} instead of an {@link ExecutorService}.</p>
     *
     * @param nThreads 
     *    the number of threads in the pool
     * @param threadFactory 
     *    the factory to use when creating new threads
     * @return 
     *    the newly created thread pool
     * @throws NullPointerException 
     *    if threadFactory is null
     * @throws IllegalArgumentException 
     *    if <tt>nThreads &lt;= 0</tt>
     */
    public static ThreadPoolExecutor newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        ArgCheck.requireNonNull(threadFactory);
        ArgCheck.requireStrictlyPostive(nThreads);

        return new ThreadPoolExecutor(nThreads,
                                      nThreads,
                                      0L,
                                      TimeUnit.MILLISECONDS,
                                      new LinkedBlockingQueue<Runnable>(),
                                      threadFactory);
    }

    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available, and uses the provided
     * ThreadFactory to create new threads when needed. Unused threads terminate
     * after the specified amount of time
     * 
     * @param keepAliveTime 
     *    this is the maximum time that idle threads
     *    will wait for new tasks before terminating.
     * @param unit 
     *    the time unit for the {@code keepAliveTime} argument
     * @param threadFactory 
     *    the factory to use when creating new threads
     * @return 
     *    the newly created thread pool
     * @throws NullPointerException 
     *    if threadFactory or unit is null
     * @throws IllegalArgumentException
     *    if keepAliveTime < 0
     */
    public static ThreadPoolExecutor newCachedThreadPool(long keepAliveTime, TimeUnit unit,
            ThreadFactory threadFactory) {
        ArgCheck.requireNonNull(unit);
        ArgCheck.requireNonNull(threadFactory);
        ArgCheck.requirePostive(keepAliveTime);

        return new ThreadPoolExecutor(0,
                                      Integer.MAX_VALUE,
                                      keepAliveTime,
                                      unit,
                                      new SynchronousQueue<Runnable>(),
                                      threadFactory);
    }

    /**
     * Similar to Executors.newCachedThreadPool but allows a maximum pool size.
     * Cached thread pool are interesting in the sense that they can grow and shrink at will.
     * Default cachedThreadPool implementation does not allow to have a maximum capacity
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAliveTime when the number of threads is greater than the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param timeUnit the time unit for the {@code keepAliveTime} argument
     * @param threadFactory the factory to use when the executor
     *        creates a new thread
     * @return the newly created thread pool
     */
    public static ExecutorService newCachedBoundedThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
            TimeUnit timeUnit, ThreadFactory threadFactory) {
        ArgCheck.requirePostive(corePoolSize);
        ArgCheck.requireStrictlyPostive(maximumPoolSize);
        ArgCheck.requireNonNull(timeUnit);
        ArgCheck.requireNonNull(threadFactory);
        ArgCheck.requirePostive(keepAliveTime);
        BlockingQueue<Runnable> queue = new LinkedTransferQueue<Runnable>() {
            @Override
            public boolean offer(Runnable e) {
                return tryTransfer(e);
            }
        };
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(corePoolSize,
                                                               maximumPoolSize,
                                                               keepAliveTime,
                                                               timeUnit,
                                                               queue,
                                                               threadFactory);
        threadPool.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    executor.getQueue().put(r);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        return threadPool;
    }
}
