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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A thread factory with named thread for easier troubleshooting.
 * 
 * The default thread factory with random names like <i>pool-1-thread12</i> to threads.
 * It makes debugging sessions painful since it is hard to guess who is who. This thread
 * factory help to identify threads by giving them a meaningful name.
 * 
 * The name of a thread is <b>poolname #<i>poolNumber</i>-thread-<i>threadNumber</i></b>. Where:
 * <ul>
 *  <li>poolname is the name of the pool</li>
 *  <li>poolNumber is an unique pool number. It helps to distinguish two pool having the same 
 *      name (which is a bad idea anyway). this number is incremented each time a thread factory 
 *      is created</li>
 *  <li>threadNumber is an unique thread number for this pool. This number is incremented each time
 *      a thread is created in this pool</li>
 */
public class NamedThreadFactory implements ThreadFactory {
    /** Number of thread factories created */
    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    /** Number of threads created */
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    /** The group to which the threads belong */
    private final ThreadGroup group;

    /** Names of the thread created start with this prefix */
    private final String namePrefix;

    /** Should thread created be daemon ? */
    private final boolean isDaemon;

    /** Priority of the threads */
    private final int priority;

    /** The class to use to customize created thread */
    private final ThreadCustomizer customizer;

    /**
     * Creates a new thread factory with a pool name. 
     * 
     * Threads are run at normal priority and are not daemon.
     * 
     * @param poolName The name of this pool
     */
    public NamedThreadFactory(String poolName) {
        this(poolName, false);
    }

    /**
     * Creates a new thread factory with a pool name.
     * 
     * Threads are run at normal priority.
     * 
     * @param poolName The name of this pool
     * @param isDaemon Does the created threads are daemons ?
     */
    public NamedThreadFactory(String poolName, boolean isDaemon) {
        this(poolName, isDaemon, Thread.NORM_PRIORITY, null);
    }

    /**
     * Creates a new thread factory with a pool name.
     * 
     * @param poolName The name of this pool.
     * @param isDaemon Does the created threads are daemons ? 
     * @param priority Priority to use to run threads
     */
    public NamedThreadFactory(String poolName, boolean isDaemon, int priority) {
        this(poolName, isDaemon, priority, null);
    }

    /**
     * Creates a new thread factory with a pool name.
     * 
     * @param poolName The name of this pool.
     * @param isDaemon Does the created threads are daemons ? 
     * @param priority Priority to use to run threads
     * @param customizer The class to use to customized the created threads
     */
    public NamedThreadFactory(String poolName, boolean isDaemon, int priority, ThreadCustomizer customizer) {
        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = poolName + " #" + poolNumber.getAndIncrement() + "-thread-";
        this.isDaemon = isDaemon;
        this.priority = priority;
        this.customizer = customizer;
    }

    public Thread newThread(Runnable r) {
        final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(isDaemon);
        t.setPriority(priority);

        if (this.customizer != null) {
            this.customizer.customize(t);
        }

        return t;
    }

    /**
     * Allows the user to customize the threads created by a {@link NamedThreadFactory}.
     * 
     * By providing a {@link ThreadCustomizer} to a {@link NamedThreadFactory} user can applies 
     * all the changes he wanted to the thread before it is being returned to the caller. 
     * 
     * @author ProActive team
     * @since  ProActive 5.0.0
     */
    public interface ThreadCustomizer {
        /**
         * Called by {@link NamedThreadFactory#newThread(Runnable)} just before returning the
         * thread to the caller. 
         * 
         * @param t The thread to be customized
         */
        public void customize(Thread t);
    }
}
