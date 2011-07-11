/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.util.concurrent.TimeUnit;


/**
 * Utility class to write a stoppable thread.
 * 
 * A stoppable is a thread repeatedly calling the same action method until it gets stopped by another thread.
 * The {@link #action()} method is called in a <code>while(true)</code> loop until {@link #terminate(int, TimeUnit)}
 * is invoked. If {@link #action()} throws an exception, the while loop exits and the exception is stored.
 * 
 * @author ProActive team
 * @since  ProActive 5.0.0
 */
public abstract class StoppableThread extends Thread {
    /** Flag: set to true when {@link #terminate(int, TimeUnit)} is invoked */
    volatile private boolean stopped;
    /** {@link #stop()} will not return before this latch is open */
    final private SweetCountDownLatch latch;
    /** The exception thrown by {@link #action()} */
    volatile private Throwable t;

    public StoppableThread() {
        this.stopped = false;
        this.latch = new SweetCountDownLatch(1);
        this.t = null;
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            while (!stopped) {
                action();
            }
        } catch (Throwable t) {
            this.t = t;
        } finally {
            this.latch.countDown();
        }
    }

    /**
     * This method is repeatedly called by a while loop  in {@link #run()}
     * 
     * If a exception is thrown by this method, the while loop exits and the
     * exception is stored. 
     * 
     * This method should not block too long
     */
    public abstract void action();

    /** 
     * Stop the thread 
     * 
     * The while loop in {@link #run()} will exits as soon as the current call to {@link #action()} 
     * returns. If the timeout is exceeded a {@link NotStoppedException} is thrown. The while loop will 
     * eventually exits when {@link #action()} will return.  
     * 
     * @param timeout   the maximum time to wait
     * @param unit      the time unit of the timeout argument
     * @throws NotStoppedException
     *                  If the thread has not stopped within the specified timeout.
     */
    synchronized public void terminate(final int timeout, final TimeUnit unit) throws NotStoppedException {
        this.stopped = true;
        this.notifyAll();

        if (!this.latch.await(timeout, unit)) {
            throw new NotStoppedException(this, unit.toMillis(timeout));
        }
    }

    public boolean isStopped() {
        return this.latch.getCount() == 0;
    }

    public boolean exitedOnError() {
        return isStopped() && this.t != null;
    }

    public Throwable getError() {
        return this.t;
    }

    /**
     * Signals that the thread could not be stopped within the specified timeout. 
     * 
     * The stack trace of the still running thread in embedded as <i>cause by</i> to help troubleshooting.
     * 
     * @author ProActive team
     * @since  ProActive 5.0.0
     */
    final public static class NotStoppedException extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 51L;

        /**
         * @param t The unresponsive thread
         * @param timeout the exceeded timeout in milliseconds
         */
        private NotStoppedException(final Thread t, final long timeout) {
            super("Failed to stop thread " + t.getName() + ". Timeout " + timeout + " ms exceeded",
                    new ThreadIsBlockedOnException(t.getStackTrace()));
        }
    }

    /**
     * Dumps the stack trace of the unresponsive thread. 
     * 
     * @author ProActive team
     * @since  ProActive 5.0.0
     */
    final public static class ThreadIsBlockedOnException extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = 51L;

        private ThreadIsBlockedOnException(final StackTraceElement[] ste) {
            super("Thread is blocked on:");
            this.setStackTrace(ste);

        }
    }
}
