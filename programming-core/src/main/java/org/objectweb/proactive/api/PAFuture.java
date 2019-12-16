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
package org.objectweb.proactive.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FutureMonitoring;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.TimeoutAccounter;


/**
 * This class provides wait primitives on a future or on a Vector of futures. It also provides an
 * event mechanism that allows to associate a method execution with a future update. Finally, it
 * also provides a way to explicitly monitor an active object from which a result is awaited.
 * 
 * @author The ProActive Team
 * @since ProActive 3.9 (December 2007)
 */
@PublicAPI
public class PAFuture {

    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);

    /**
     * This negative value is returned by waitForAny(List futures) if the parameter
     * futures is an empty collection.
     */
    public static final int INVALID_EMPTY_COLLECTION = -1337;

    /**
     * Return the object contains by the future (ie its target). If parameter is not a future, it is
     * returned. A wait-by-necessity occurs if future is not available. This method is recursive,
     * i.e. if result of future is a future too, <CODE>getFutureValue</CODE> is called again on
     * this result, and so on.
     */
    public static <T> T getFutureValue(T future) {
        try {
            return getFutureValue(future, 0);
        } catch (ProActiveTimeoutException e) {
            //Exception above should never be thrown since timeout=0 means no timeout
            logger.error("Timeout while receiving future", e);
            return null;
        }
    }

    /**
     * Return the object contains by the future (ie its target). If parameter is not a future, it is
     * returned. A wait-by-necessity occurs if future is not available. This method is recursive,
     * i.e. if result of future is a future too, <CODE>getFutureValue</CODE> is called again on
     * this result, and so on.
     * @param timeout to wait in ms
     * @throws ProActiveException if the timeout expire
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFutureValue(T future, long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter ta = TimeoutAccounter.getAccounter(timeout);

        ArrayList<StackTraceElement> totalContext = null;
        while (true) {
            // If the object is not reified, it cannot be a future
            if ((MOP.isReifiedObject(future)) == false) {
                return future;
            } else {
                org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

                // If it is reified but its proxy is not of type future, we cannot wait
                if (!(theProxy instanceof Future)) {
                    return future;
                } else {
                    // The total context is the global stack trace created by each recursive future
                    if (totalContext == null) {
                        totalContext = new ArrayList<StackTraceElement>();
                        // we initialize the context with the stack to this point
                        totalContext.addAll(Arrays.asList(new Exception().getStackTrace()));
                        // we update it by inspecting each future proxy received
                        FutureProxy.updateStackTraceContext(totalContext, ((FutureProxy) theProxy), true);
                    } else {
                        FutureProxy.updateStackTraceContext(totalContext, ((FutureProxy) theProxy), false);
                    }
                    future = (T) ((Future) theProxy).getResult(ta.getRemainingTimeout());
                }
            }
        }
    }

    /**
     * Return false if the object <code>future</code> is available. This method is recursive, i.e.
     * if result of future is a future too, <CODE>isAwaited</CODE> is called again on this result,
     * and so on.
     */
    public static boolean isAwaited(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return false;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return false;
            } else {
                if (((Future) theProxy).isAwaited()) {
                    return true;
                } else {
                    return isAwaited(((Future) theProxy).getResult());
                }
            }
        }
    }

    /**
     * Blocks the calling thread until the object <code>future</code> is available.
     * <code>future</code> must be the result object of an asynchronous call. Usually the the wait
     * by necessity model take care of blocking the caller thread asking for a result not yet
     * available. This method allows to block before the result is first used.
     * 
     * @param future
     *            object to wait for
     */
    public static void waitFor(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return;
            } else {
                ((Future) theProxy).waitFor();
            }
        }
    }

    /**
     * Blocks the calling thread until the object <code>future</code> is available or until the
     * timeout expires. <code>future</code> must be the result object of an asynchronous call.
     * Usually the the wait by necessity model take care of blocking the caller thread asking for a
     * result not yet available. This method allows to block before the result is first used.
     * 
     * @param future
     *            object to wait for
     * @param timeout
     *            to wait in ms
     * @throws ProActiveException
     *             if the timeout expire
     */
    public static void waitFor(Object future, long timeout) throws ProActiveTimeoutException {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future, we cannot wait
            if (!(theProxy instanceof Future)) {
                return;
            } else {
                ((Future) theProxy).waitFor(timeout);
            }
        }
    }

    /**
     * Blocks the calling thread until the object <code>future</code> is available.
     * <code>future</code> must be the result object of an asynchronous call. Usually the the wait
     * by necessity model take care of blocking the caller thread asking for a result not yet
     * available. This method allows to block before the result is first used.
     * 
     * @param future
     *            object to wait for
     * @param recursive if true, wait until the updating value is not a future (i.e. if the updating 
     * value is a future, wait for this future).
     */
    public static void waitFor(Object future, boolean recursive) {
        if (recursive) {
            // recursive version
            getFutureValue(future);
        } else {
            // one step version
            waitFor(future);
        }
    }

    /**
     * Blocks the calling thread until the object <code>future</code> is available or until the
     * timeout expires. <code>future</code> must be the result object of an asynchronous call.
     * Usually the the wait by necessity model take care of blocking the caller thread asking for a
     * result not yet available. This method allows to block before the result is first used.
     * 
     * @param future
     *            object to wait for
     * @param timeout
     *            to wait in ms
     * @param recursive if true, wait until the updating value is not a future (i.e. if the updating 
     * value is a future, wait for this future).
     * @throws ProActiveException
     *             if the timeout expire
     */
    public static void waitFor(Object future, long timeout, boolean recursive) throws ProActiveTimeoutException {
        if (recursive) {
            // recursive version
            getFutureValue(future, timeout);
        } else {
            // one step version
            waitFor(future, timeout);
        }
    }

    /**
     * Blocks the calling thread until all futures in the collection are available.
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the collection.
     *
     * @param futures
     *            a collection of futures
     */
    public static void waitForAll(Collection<?> futures) {
        try {
            PAFuture.waitForAll(futures, 0);
        } catch (ProActiveTimeoutException e) {
            //Exception above should never be thrown since timeout=0 means no timeout
            logger.error("Timeout while receiving future", e);
        }
    }

    /**
     * Blocks the calling thread until all futures in the collection are available or until the timeout
     * expires.
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the collection.
     *
     * @param futures
     *            a collection of futures
     * @param timeout
     *            to wait in ms
     * @throws ProActiveException
     *             if the timeout expires
     */
    public static void waitForAll(Collection<?> futures, long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        for (Object future : futures) {
            if (time.isTimeoutElapsed()) {
                throw new ProActiveTimeoutException("Timeout expired while waiting for future update");
            }
            PAFuture.waitFor(future, time.getRemainingTimeout());
        }
    }

    /**
     * Blocks the calling thread until one of the futures in the list is available.
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the collection.
     *
     * @param futures
     *            a list of futures
     * @return index of the available future in the list
     */
    public static int waitForAny(List<?> futures) {
        try {
            return PAFuture.waitForAny(futures, 0);
        } catch (ProActiveException e) {
            //Exception above should never be thrown since timeout=0 means no timeout
            logger.error("Timeout while receiving future", e);
            return -1;
        }
    }

    /**
     * Blocks the calling thread until one of the futures in the list is available or until the
     * timeout expires. 
     *
     *	<b>Warning</b> : this method must be called by either any active object or by the thread that 
     *  performed the method calls corresponding to the futures in the list.
     *
     *
     * @param futures
     *            a list of futures
     * @param timeout
     *            to wait in ms
     * @return index of the available future in the list
     * @throws ProActiveException
     *             if the timeout expires
     */
    public static <E> int waitForAny(List<E> futures, long timeout) throws ProActiveException {
        return waitForAnyImpl(futures, timeout);
    }

    /**
     * Blocks the calling thread until one of the futures in the collection is available or until the
     * timeout expires.
     *
     *  <b>Warning</b> : this method must be called by either any active object or by the thread that
     *  performed the method calls corresponding to the futures in the collection.
     *
     *
     * @param futures
     *            a list of futures
     * @param timeout
     *            to wait in ms
     * @return index of the available future in the list
     * @throws ProActiveException
     *             if the timeout expires
     * @deprecated Replaced by {@link #waitForAny(List, long)}. Since an index is returned, a List should
     * be used instead of a collection.
     */
    @Deprecated
    public static <E> int waitForAny(Collection<E> futures, long timeout) throws ProActiveException {
        return waitForAnyImpl(futures, timeout);
    }

    private static <E> int waitForAnyImpl(Collection<E> futures, long timeout) throws ProActiveException {
        if (futures.isEmpty()) {

            /*
             * Yes, this return value is meaningless but at least we are not permanently blocked
             */
            return PAFuture.INVALID_EMPTY_COLLECTION;
        }
        FuturePool fp = PAActiveObject.getBodyOnThis().getFuturePool();
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);

        for (Object future : futures) {
            if (PAFuture.isAwaited(future)) {
                monitorFuture(future);
            }
        }

        synchronized (fp) {
            while (true) {
                Iterator<E> it = futures.iterator();
                int index = 0;

                while (it.hasNext()) {
                    Object current = it.next();

                    if (!PAFuture.isAwaited(current)) {
                        return index;
                    }

                    index++;
                }
                if (time.isTimeoutElapsed()) {
                    throw new ProActiveException("Timeout expired while waiting for future update");
                }
                fp.waitForReply(time.getRemainingTimeout());
            }
        }
    }

    /**
     * Return <code>false</code> if one object of <code>futures</code> is available.
     * 
     * @param futures
     *            a table with futures.
     * @return <code>true</code> if all futures are awaited, else <code>false
     * </code>.
     */
    public static boolean allAwaited(Collection<?> futures) {
        FuturePool fp = PAActiveObject.getBodyOnThis().getFuturePool();

        synchronized (fp) {
            Iterator<?> it = futures.iterator();

            while (it.hasNext()) {
                Object current = it.next();

                if (!PAFuture.isAwaited(current)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Add a future to the list of monitored future. This is automatically done when waiting a
     * future. If the active object serving the method for this future cannot be pinged, the future
     * is updated with a RuntimeException.
     * 
     * @param future
     *            the future object to monitor
     */
    public static void monitorFuture(Object future) {
        if (!MOP.isReifiedObject(future)) {
            throw new IllegalArgumentException("Parameter is not a future object (actual type is " +
                                               future.getClass().getName() + ")");
        }
        FutureProxy fp = (FutureProxy) ((StubObject) future).getProxy();
        FutureMonitoring.monitorFutureProxy(fp);
    }
}
