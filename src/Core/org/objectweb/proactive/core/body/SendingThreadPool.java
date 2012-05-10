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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.body;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.core.body.proxy.RequestToSend;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.ThreadPools;


/**
 * There is one instance of this class per SendingQueue. Each instance is Runnable. Thus, it can be
 * started to poll the SendingQueue for sending the RequestToSend through a ThreadPoolExecutor.
 */
public class SendingThreadPool implements Runnable {

    private static final int MAX_THREAD_POOL_CORE_SIZE = 10;
    private static final int THREAD_KEEP_ALIVE_TIME = 10; // In Seconds

    /* The ThreadPoolExecutor which handles the RTS */
    private ThreadPoolExecutor threadPool;

    /* The SendingQueue this SendingThreadPool is attached to */
    private SendingQueue sendingQueue;

    /* A reference to the running thread */
    private Thread myThread;

    /* A boolean to terminate the thread (see stop()) */
    private boolean continueRunning;

    /**
     * Creates a {@link SendingThreadPool} instance without starting it. This should be done through
     * the <i>wakeup()</i> method which would be invoked when trying to send a FOS request.
     * 
     * @param sendingQueue
     */
    public SendingThreadPool(SendingQueue sendingQueue) {
        ThreadFactory tf = new NamedThreadFactory("ProActive sending");
        this.threadPool = ThreadPools.newBoundedThreadPool(MAX_THREAD_POOL_CORE_SIZE, THREAD_KEEP_ALIVE_TIME,
                TimeUnit.SECONDS, tf);
        this.sendingQueue = sendingQueue;
        this.continueRunning = true;
    }

    /**
     * Starts the thread if not already started. This will cause the {@link SendingThreadPool} poll
     * the {@link SendingQueue} to get and send some {@link RequestToSend}.
     */
    public void wakeUp() {
        if (myThread == null) {
            myThread = new Thread(this);
            myThread.start();
        }
    }

    /**
     * Invoking this method will cause the running thread (if it's running) to terminate ASAP. ASAP
     * means that it will waits until the {@link SendingQueue} is empty, to ensure that all pending
     * {@link RequestToSend} would be sent.
     */
    public void stop() {
        this.continueRunning = false;
    }

    /**
     * While the body is active, retrieve a {@link RequestToSend} from the {@link SendingQueue} and
     * send it to the Thread Pool for immediate execution (ie. sending the request).
     */
    public void run() {
        try {
            RequestToSend rts = null;
            do {
                rts = sendingQueue.poll(1, TimeUnit.SECONDS);
                if (rts != null) {
                    threadPool.execute(rts);
                }

            } while (continueRunning || rts != null);

            threadPool.shutdown();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (java.util.concurrent.RejectedExecutionException e) {
            e.printStackTrace();
        }
    }
}
