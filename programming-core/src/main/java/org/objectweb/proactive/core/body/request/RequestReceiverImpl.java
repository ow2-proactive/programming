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
package org.objectweb.proactive.core.body.request;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.exceptions.InactiveBodyException;
import org.objectweb.proactive.core.util.HeartbeatResponse;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RequestReceiverImpl implements RequestReceiver, java.io.Serializable {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.REQUESTS);

    // time of inactivity after which a thread for caller is stopped (in sec)
    public final static int THREAD_FOR_IS_PING_PERIOD = 21;

    // Classwrapper that characterizes method with any parameters
    private static final ClassArrayWrapper ANY_PARAMETERS;
    static {
        ANY_PARAMETERS = new ClassArrayWrapper(new Class[] { AnyParametersClass.class });
    }

    private static class AnyParametersClass implements Serializable {
    }

    /**
     * This class wraps arrays of class to redefine hashcode(), so method params can
     * be used as key in a Map.
     */
    private static final class ClassArrayWrapper implements Serializable {
        private final Class<?>[] wrappedClassArray;

        // cached hashcode since CAW is final
        private final int myHashcode;

        public ClassArrayWrapper(Class<?>[] classArray) {
            this.wrappedClassArray = classArray;
            this.myHashcode = Arrays.toString(this.wrappedClassArray).hashCode();
        }

        public int hashCode() {
            return this.myHashcode;
        }

        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ClassArrayWrapper other = (ClassArrayWrapper) obj;
            if (!Arrays.equals(wrappedClassArray, other.wrappedClassArray))
                return false;
            return true;
        }

    }

    /**
     * Defines the service mode, i.e. the way the incoming request has to be served.
     * It could be normal service (request in put in queue), immediate service multi-thread 
     * (request is served by the incoming thread) or immediate service unique thread (request
     * id served by a dedicated thread, which is the same for a given caller).
     */
    public enum ServiceMode {
        NORMAL_SERVICE,
        IMMEDIATE_MULTI_THREAD,
        IMMEDIATE_UNIQUE_THREAD;
    }

    // Map of service mode per method name and method parameters
    private final Map<String, Map<ClassArrayWrapper, ServiceMode>> serviceModes;

    // immediate services are currently executed if != 0
    private AtomicInteger inImmediateService;

    // Thread associated to a given caller for immediate service with unique thread
    private transient Map<UniqueID, ThreadForImmediateService> threadsForCallers;

    public RequestReceiverImpl() {
        serviceModes = new Hashtable<String, Map<ClassArrayWrapper, ServiceMode>>(3);
        // Set by default immediate services
        final Hashtable<ClassArrayWrapper, ServiceMode> ts = new Hashtable<ClassArrayWrapper, ServiceMode>(1);
        ts.put(ANY_PARAMETERS, ServiceMode.IMMEDIATE_MULTI_THREAD);
        serviceModes.put("toString", ts);
        final Hashtable<ClassArrayWrapper, ServiceMode> hc = new Hashtable<ClassArrayWrapper, ServiceMode>(1);
        hc.put(ANY_PARAMETERS, ServiceMode.IMMEDIATE_MULTI_THREAD);
        serviceModes.put("hashCode", hc);
        final Hashtable<ClassArrayWrapper, ServiceMode> ti = new Hashtable<ClassArrayWrapper, ServiceMode>(1);
        ti.put(ANY_PARAMETERS, ServiceMode.IMMEDIATE_MULTI_THREAD);
        serviceModes.put("_terminateAOImmediately", ti);
        this.inImmediateService = new AtomicInteger(0);
        this.threadsForCallers = new Hashtable<UniqueID, ThreadForImmediateService>();
    }

    public void receiveRequest(Request request, Body bodyReceiver) {
        try {
            final ServiceMode mode = this.getServiceMode(request);
            if (!mode.equals(ServiceMode.NORMAL_SERVICE)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("immediately serving " +
                                 request.getMethodName() + (mode.equals(ServiceMode.IMMEDIATE_UNIQUE_THREAD)
                                                                                                             ? " with unique thread for caller " +
                                                                                                               request.getSourceBodyID()
                                                                                                             : ""));
                }
                this.inImmediateService.incrementAndGet();
                try {
                    if (mode.equals(ServiceMode.IMMEDIATE_UNIQUE_THREAD)) {
                        final UniversalBody caller = request.getSender();
                        ThreadForImmediateService serviceThread = this.threadsForCallers.get(caller.getID());
                        if (serviceThread == null) {
                            serviceThread = new ThreadForImmediateService(caller);
                            serviceThread.start();
                            this.threadsForCallers.put(caller.getID(), serviceThread);
                        }
                        serviceThread.deleguateServe(request, bodyReceiver);
                    } else {
                        bodyReceiver.serve(request);
                    }
                } finally {
                    this.inImmediateService.decrementAndGet();
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("end of service for " + request.getMethodName());
                }
            } else {
                request.notifyReception(bodyReceiver);
                RequestQueue queue = null;
                try {
                    queue = bodyReceiver.getRequestQueue();
                } catch (InactiveBodyException e) {
                    throw new InactiveBodyException("Cannot add request \"" + request.getMethodName() +
                                                    "\" because this body is inactive", e);
                }
                queue.add(request);
            }
        } catch (Exception e) {
            logger.info("Error while receiving request " + (request == null ? "null" : request.getMethodName()), e);
        }
    }

    /**
     * Return the service mode for the incoming request
     */
    private ServiceMode getServiceMode(Request request) {
        if ((request == null) || (request.getMethodCall() == null) ||
            (request.getMethodCall().getReifiedMethod() == null)) {
            // default value
            return ServiceMode.NORMAL_SERVICE;
        } else {
            final String methodName = request.getMethodName();
            if (serviceModes.containsKey(methodName)) {
                final Map<ClassArrayWrapper, ServiceMode> params = serviceModes.get(methodName);
                final ClassArrayWrapper parameterTypes = new ClassArrayWrapper(request.getMethodCall()
                                                                                      .getReifiedMethod()
                                                                                      .getParameterTypes());
                if (params.containsKey(parameterTypes)) {
                    // return the most specific result 
                    return params.get(parameterTypes);
                } else if (params.containsKey(ANY_PARAMETERS)) {
                    // method was registered using method name only
                    return params.get(ANY_PARAMETERS);
                } else {
                    // no entry for this parameters
                    return ServiceMode.NORMAL_SERVICE;
                }
            } else {
                // no entry for this method name
                return ServiceMode.NORMAL_SERVICE;
            }
        }
    }

    /**
     * Return true if current service mode for the request is immediate, false otherwise.
     * @return true if current service mode for the request is immediate, false otherwise.
     */
    public boolean immediateExecution(Request request) {
        return this.getServiceMode(request) != ServiceMode.NORMAL_SERVICE;
    }

    public void setImmediateService(String methodName, boolean uniqueThread) {
        final Hashtable<ClassArrayWrapper, ServiceMode> t = new Hashtable<ClassArrayWrapper, ServiceMode>(1);
        t.put(ANY_PARAMETERS, uniqueThread ? ServiceMode.IMMEDIATE_UNIQUE_THREAD : ServiceMode.IMMEDIATE_MULTI_THREAD);
        this.serviceModes.put(methodName, t);
    }

    public void removeImmediateService(String methodName) {
        // remove any registered service mode
        this.serviceModes.remove(methodName);
    }

    public void removeImmediateService(String methodName, Class<?>[] parametersTypes) {
        Map<ClassArrayWrapper, ServiceMode> params = serviceModes.get(methodName);
        if (params != null) {
            if (params.containsKey(ANY_PARAMETERS)) {
                // "all-params" is registered : must explicitely register this method as "normal service"
                params.put(new ClassArrayWrapper(parametersTypes), ServiceMode.NORMAL_SERVICE);
            } else {
                params.remove(new ClassArrayWrapper(parametersTypes));
            }
        }
    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes, boolean uniqueThread) {
        final Map<ClassArrayWrapper, ServiceMode> params = serviceModes.get(methodName);
        final ClassArrayWrapper wrappedParams = new ClassArrayWrapper(parametersTypes);
        if (params != null) {
            if (params.containsKey(ANY_PARAMETERS)) {
                // check if the "all-params" mode is the awaited one. If not, add a specific entry
                if (!(params.get(ANY_PARAMETERS).equals((uniqueThread ? ServiceMode.IMMEDIATE_UNIQUE_THREAD
                                                                      : ServiceMode.IMMEDIATE_MULTI_THREAD)))) {
                    params.put(wrappedParams,
                               uniqueThread ? ServiceMode.IMMEDIATE_UNIQUE_THREAD : ServiceMode.IMMEDIATE_MULTI_THREAD);
                }
            } else {
                // no "all-params" entry, add specific one
                params.put(wrappedParams,
                           uniqueThread ? ServiceMode.IMMEDIATE_UNIQUE_THREAD : ServiceMode.IMMEDIATE_MULTI_THREAD);
            }
        } else {
            // no entry for this method name, add one
            final Hashtable<ClassArrayWrapper, ServiceMode> t = new Hashtable<ClassArrayWrapper, ServiceMode>(1);
            t.put(wrappedParams,
                  uniqueThread ? ServiceMode.IMMEDIATE_UNIQUE_THREAD : ServiceMode.IMMEDIATE_MULTI_THREAD);
            this.serviceModes.put(methodName, t);
        }
    }

    /**
     * Return true if an immediate service is currently served.
     */
    public boolean isInImmediateService() throws IOException {
        return this.inImmediateService.intValue() > 0;
    }

    public boolean hasThreadsForImmediateService() {
        return this.threadsForCallers.size() != 0;
    }

    /**
     * Terminate all the ThreadForImmediateService.
     */
    public void terminate() {
        // must get a snapshot of threadForCallers, as t.kill() modifies it.
        Set<ThreadForImmediateService> allThreads = new HashSet<ThreadForImmediateService>();
        for (ThreadForImmediateService t : this.threadsForCallers.values()) {
            allThreads.add(t);
        }
        // kill all threads for caller
        for (ThreadForImmediateService t : allThreads) {
            t.kill();
        }
        this.threadsForCallers.clear();
    }

    /**
     * Thread for serving "immediate service with unique thread". 
     * This thread is associated to a given caller.
     */
    private class ThreadForImmediateService extends Thread {

        private final UniversalBody associatedCaller;

        private final Semaphore callerLock;

        private final Semaphore localLock;

        private Request currentRequest;

        private Body currentReceiver;

        private final AtomicBoolean isActive;

        private final AtomicBoolean isInService;

        /**
         * Create a new ThreadForImmediateService that serve request coming from the specified caller only.
         * @param caller a reference to the specified caller.
         */
        public ThreadForImmediateService(UniversalBody caller) {
            this.associatedCaller = caller;
            this.setName("Immediate Service Thread for caller " + this.associatedCaller.getID());
            this.isActive = new AtomicBoolean(true);
            this.isInService = new AtomicBoolean(false);
            this.callerLock = new Semaphore(0);
            this.localLock = new Semaphore(0);
        }

        /**
         * Execute the request on the receiver body. The thread calling this method is blocked until the service
         * of the request ends.
         * Note that this method cannot be called concurrently as it is dedicated to a single caller, which is blocked 
         * until the current service ends.
         * @param request the request to serve
         * @param receiver the body on which the request is served
         * @throws ProActiveException if this ThreadForImmediateService is no more active
         */
        public void deleguateServe(Request request, Body receiver) throws ProActiveException {
            if (isActive.get()) {
                // sanity check
                if (!this.isInService.compareAndSet(false, true)) {
                    throw new ProActiveRuntimeException("Deleguate services cannot be concurrent.");
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Serving IS with unique thread : " + request.getMethodName() + " for caller " +
                                 this.associatedCaller);
                }
                this.currentRequest = request;
                this.currentReceiver = receiver;
                this.localLock.release();
                // wait for the completion of the call
                this.callerLock.acquireUninterruptibly();
                this.isInService.set(false);
            } else {
                throw new ProActiveException("" + this.getName() + " is no more active. Request " +
                                             request.getMethodName() + " from " + request.getSourceBodyID() +
                                             " cannot be served");
            }
        }

        public void run() {
            boolean didCall = false;
            while (isActive.get()) {
                try {
                    didCall = this.localLock.tryAcquire(RequestReceiverImpl.THREAD_FOR_IS_PING_PERIOD,
                                                        TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    // interruption is ignored
                    if (isActive.get()) {
                        continue;
                    } else {
                        this.kill();
                        break;
                    }
                }
                // test if a service is required
                if (didCall && isActive.get()) {
                    try {
                        this.currentReceiver.serve(this.currentRequest);
                    } catch (Throwable e) {
                        logger.error("An exception occured in the service of " + this.currentRequest.getMethodName() +
                                     " by " + this, e);
                    }
                    // the following set do not have to be synchronized since only one deleguateServe call at a time is possible 
                    this.currentRequest = null;
                    this.currentReceiver = null;
                    this.callerLock.release();
                } else {
                    // if didCall is false, timeout occurs : check caller livness
                    if (!this.pingAssociatedCaller()) {
                        this.kill();
                    }
                }
            }
        }

        /**
         * Stop this ThreadForImmediateService. Calling doCall() after calling kill() will throw 
         * a ProActiveException.
         */
        public void kill() {
            if (isActive.compareAndSet(true, false)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("" + this + " is terminating...");
                }
                this.interrupt();
                RequestReceiverImpl.this.threadsForCallers.remove(this.associatedCaller.getID());
                this.callerLock.release();
            }
        }

        /**
         * Return true is the associated caller is alive, false otherwise
         * @return true is the associated caller is alive, false otherwise
         */
        private boolean pingAssociatedCaller() {
            try {
                // PROACTIVE-267 : should not use explicitly FT package.
                Object ping = this.associatedCaller.receiveHeartbeat();
                return HeartbeatResponse.OK.equals(ping);
            } catch (IOException e) {
                return false;
            }
        }

    }

    // Serilization methods are redefined because of threads for caller

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.threadsForCallers = new Hashtable<UniqueID, ThreadForImmediateService>();
    }

}
