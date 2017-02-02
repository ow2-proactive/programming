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
package org.objectweb.proactive.core.body.future;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.proxy.AbstractProxy;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.exceptions.ExceptionHandler;
import org.objectweb.proactive.core.exceptions.ExceptionMaskLevel;
import org.objectweb.proactive.core.group.DispatchMonitor;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.FutureNotificationData;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.TimeoutAccounter;


/**
 * This proxy class manages the semantic of future objects
 *
 * @author The ProActive Team
 * @see org.objectweb.proactive.core.mop.Proxy
 *
 */
public class FutureProxy implements Future, Proxy, java.io.Serializable {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //
    final static protected Logger logger = ProActiveLogger.getLogger(Loggers.BODY);

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /**
     *        The object the proxy sends calls to
     */
    protected MethodCallResult target;

    /**
     * True if this proxy has to be copied for migration or local copy.
     * If true, the serialization of this future does not register an automatic continuation.
     */
    protected transient boolean copyMode;

    /**
     * Unique ID (not a UniqueID) of the future
     */
    private FutureID id;

    /**
     * Unique ID of the sender (in case of automatic continuation).
     */
    protected UniqueID senderID;

    /**
     * To monitor this future, this body will be pinged.
     * transient to explicitly document that the serialization of
     * this attribute is custom: in case of automatic continuation,
     * it references the previous element in the chain
     */
    private transient UniversalBody updater;

    /**
     * The exception level in the stack in which this future is
     * registered
     */
    private transient ExceptionMaskLevel exceptionLevel;

    /**
     * The proxy that created this future. Set as transient to avoid
     * adding remote references when sending the future. Migration is
     * thus not supported.
     */
    private transient AbstractProxy originatingProxy;

    /**
     * The methods to call when this future is updated
     */
    private transient LocalFutureUpdateCallbacks callbacks;

    // returns future update info used during dynamic dispatch for groups
    private transient DispatchMonitor dispatchMonitor;

    // the context stack when this future was created, the context stack is only filled when the property
    // proactive.stack_trace is set
    protected StackTraceElement[] callerContext;

    // the "light" version of the above stack trace, it contains only one line with the server main method call
    // e.g. future = ao.foo() , the main line will be the line in the server stack containing the call foo()
    // it is set even if the property proactive.stack_trace is not set
    protected StackTraceElement currentMainStackElement;

    // In automatic continuations, (i.e. a future1 containing a future2, containing a future3, etc), the originalMainStackElement contains the original
    // main call (top-level future). It is stored in the Thread as a ThreadLocal variable. This makes the originalMainStackElement
    // accessible by sub-futures (future2, future3, etc)
    private static transient ThreadLocal<StackTraceElement> originalMainStackElement = new ThreadLocal<StackTraceElement>();

    protected static boolean enableStack = CentralPAPropertyRepository.PA_STACKTRACE.getValue();

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * As this proxy does not create a reified object (as opposed to
     * BodyProxy for example), it is the noargs constructor that
     * is usually called.
     */
    public FutureProxy() throws ConstructionOfReifiedObjectFailedException {
    }

    /**
     * This constructor is provided for compatibility with other proxies.
     * More precisely, this permits proxy instantiation via the Meta.newMeta
     * method.
     */
    public FutureProxy(ConstructorCall c, Object[] p) throws ConstructionOfReifiedObjectFailedException {
        // we don't care what the arguments are
        this();
    }

    //
    // -- PUBLIC STATIC METHODS -----------------------------------------------
    //

    /**
     * Tests if the object <code>obj</code> is awaited or not. Always returns
     * <code>false</code> if <code>obj</code> is not a future object.
     */
    public static boolean isAwaited(Object obj) {
        return PAFuture.isAwaited(obj);
    }

    public synchronized static FutureProxy getFutureProxy() {
        FutureProxy result;
        try {
            result = new FutureProxy();
        } catch (ConstructionOfReifiedObjectFailedException e) {
            result = null;
        }
        return result;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FutureProxy) {
            return this.id.equals(((FutureProxy) obj).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    //
    // -- Implements Future -----------------------------------------------
    //

    /**
     * Invoked by a thread of the skeleton that performed the service in order
     * to tie the result object to the proxy.
     *
     * If the execution of the call raised an exception, this exception is put
     * into an object of class InvocationTargetException and returned, just like
     * for any returned object
     */
    public synchronized void receiveReply(MethodCallResult obj) {
        if (isAvailable()) {
            throw new IllegalStateException("FutureProxy receives a reply and the current target field is not null. Current target is " +
                                            this.target + " while reply's target is " + obj);
        }
        if (dispatchMonitor != null) {
            dispatchMonitor.updatedResult(originatingProxy);
        }
        target = obj;
        ExceptionHandler.addResult(this);
        FutureMonitoring.removeFuture(this);

        if (this.callbacks != null) {
            this.callbacks.run();
            this.callbacks = null;
        }

        this.notifyAll();
    }

    /**
     * Returns the result this future is for as an exception if an exception has been raised
     * or null if the result is not an exception. The method blocks until the result is available.
     * @return the exception raised once available or null if no exception.
     */
    public synchronized Throwable getRaisedException() {
        waitFor();
        return target.getException();
    }

    /**
     * @return true iff the future has arrived.
     */
    public boolean isAvailable() {
        return target != null;
    }

    /**
     * Returns a MethodCallResult containing the awaited result, or the exception that occurred if any.
     * The method blocks until the future is available
     * @return the result of this future object once available.
     */
    public synchronized MethodCallResult getMethodCallResult() {
        waitFor();
        return target;
    }

    /**
     * Returns the result this future is for. The method blocks until the future is available
     * @return the result of this future object once available.
     */
    public synchronized Object getResult() {
        waitFor();
        updateContext();
        return target.getResult();
    }

    /**
     * Returns the result this future is for. The method blocks until the future is available
     * @return the result of this future object once available.
     * @throws ProActiveException if the timeout expires
     */
    public synchronized Object getResult(long timeout) throws ProActiveTimeoutException {
        waitFor(timeout);
        updateContext();
        return target.getResult();
    }

    /**
     * Tests the status of the returned object
     * @return <code>true</code> if the future object is NOT yet available, <code>false</code> if it is.
     */
    public synchronized boolean isAwaited() {
        return !isAvailable();
    }

    /**
     * Blocks the calling thread until the future object is available.
     */
    public synchronized void waitFor() {
        try {
            waitFor(0);
        } catch (ProActiveTimeoutException e) {
            throw new IllegalStateException("Cannot happen");
        }
    }

    /**
     * Blocks the calling thread until the future object is available or the timeout expires
     * @param timeout
     * @throws ProActiveException if the timeout expires
     */
    public synchronized void waitFor(long timeout) throws ProActiveTimeoutException {
        if (isAvailable()) {
            return;
        }

        FutureMonitoring.monitorFutureProxy(this);

        // JMX Notification
        BodyWrapperMBean mbean = null;
        UniqueID bodyId = PAActiveObject.getBodyOnThis().getID();
        Body body = LocalBodyStore.getInstance().getLocalBody(bodyId);

        // Send notification only if ActiveObject, not for HalfBodies
        if (body != null) {
            mbean = body.getMBean();
            if (mbean != null) {
                mbean.sendNotification(NotificationType.waitByNecessity,
                                       new FutureNotificationData(bodyId, getCreatorID()));
            }
        }

        // END JMX Notification
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        while (!isAvailable()) {
            if (time.isTimeoutElapsed()) {
                throw new ProActiveTimeoutException("Timeout expired while waiting for the future update");
            }
            try {
                this.wait(time.getRemainingTimeout());
            } catch (InterruptedException e) {
                logger.debug(e);
            }
        }

        // JMX Notification
        if (mbean != null) {
            mbean.sendNotification(NotificationType.receivedFutureResult,
                                   new FutureNotificationData(bodyId, getCreatorID()));
        }

        // END JMX Notification
    }

    public long getID() {
        return id.getID();
    }

    public void setID(long l) {
        if (id == null) {
            id = new FutureID();
        }
        id.setID(l);
    }

    public FutureID getFutureID() {
        return this.id;
    }

    public void setCreatorID(UniqueID creatorID) {
        if (id == null) {
            id = new FutureID();
        }
        id.setCreatorID(creatorID);
    }

    public void setCreatorStackTraceElement(final StackTraceElement stackElement) {
        this.currentMainStackElement = stackElement;
        // the top-level future main stack element in an automatic continuation
        this.originalMainStackElement.set(stackElement);
    }

    public UniqueID getCreatorID() {
        return id.getCreatorID();
    }

    public void setUpdater(UniversalBody updater) {
        if (this.updater != null) {
            new IllegalStateException("Updater already set to: " + this.updater).printStackTrace();
        }
        this.updater = updater;
    }

    public UniversalBody getUpdater() {
        return this.updater;
    }

    public void setCallerContext(StackTraceElement[] context) {
        this.callerContext = context;
    }

    /**
     * This method transforms the received method call result by adding the stack trace context
     * the level on information contained in the stack depends on the property proactive.stack_trace
     */
    public void updateContext() {
        if (target != null) {
            target = new ContextAwareMethodCallResult(target.getResultObjet(), target.getException(), callerContext);
        }
    }

    public StackTraceElement[] getCallerContext() {
        return this.callerContext;
    }

    public void setSenderID(UniqueID i) {
        senderID = i;
    }

    public void setOriginatingProxy(AbstractProxy p) {
        originatingProxy = p;
    }

    //
    // -- Implements Proxy -----------------------------------------------
    //

    /**
     * Blocks until the future object is available, then executes Call <code>c</code> on the now-available object.
     *
     *  As future and process behaviors are mutually exclusive, we know that
     * the invocation of a method on a future objects cannot lead to wait-by
     * necessity. Thus, we can propagate all exceptions raised by this invocation
     *
     * @exception InvocationTargetException If the invokation of the method represented by the
     * <code>Call</code> object <code>c</code> on the reified object
     * throws an exception, this exception is thrown as-is here. The stub then
     * throws this exception to the calling thread after checking that it is
     * declared in the throws clause of the reified method. Otherwise, the stub
     * does nothing except print a message on System.err (or out ?).
     */
    public Object reify(MethodCall c) throws InvocationTargetException {
        Object result = null;
        waitFor();

        // Now that the object is available, execute the call
        Object resultObject = target.getResult();
        try {
            result = c.execute(resultObject);
        } catch (MethodCallExecutionFailedException e) {
            throw new ProActiveRuntimeException("FutureProxy: Illegal arguments in call " + c.getName());
        }

        // If target of this future is another future, make a shortcut !
        if (resultObject instanceof StubObject) {
            Proxy p = ((StubObject) resultObject).getProxy();
            if (p instanceof FutureProxy) {
                target = ((FutureProxy) p).target;
            }
        }

        return result;
    }

    /**
     * This method updates the given total stack trace using the current stack found in the future proxy
     * @param totalContext the total stack trace computed so far
     * @param proxy the proxy on which the context will be updated
     * @param origin if we are at the origin (bottom of the total stack)
     */
    public static void updateStackTraceContext(ArrayList<StackTraceElement> totalContext, FutureProxy proxy,
            boolean origin) {
        StackTraceElement[] currentContext = proxy.getCallerContext();
        StackTraceElement emptyline = new StackTraceElement("(..", ")", null, -1);

        if (origin) {
            // if we are at the origin (bottom) of the stack, we add the original main call element, received by the original FutureProxy
            // via the ThreadLocal context
            totalContext.add(0, proxy.originalMainStackElement.get());
            totalContext.add(1, emptyline);
        }
        // if the current full stack trace is available we add it
        if (currentContext != null) {
            totalContext.addAll(0, Arrays.asList(currentContext));
        }
        // in any case we add the current main call element
        totalContext.add(0, proxy.currentMainStackElement);
        totalContext.add(1, emptyline);

        // we update the caller context in the proxy
        proxy.setCallerContext(totalContext.toArray(new StackTraceElement[0]));
    }

    // -- PROTECTED METHODS -----------------------------------------------
    //
    public void setCopyMode(boolean mode) {
        copyMode = mode;
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION -----------------------------------------------
    //
    private synchronized void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        UniversalBody writtenUpdater = this.updater;

        if (!FuturePool.isInsideABodyForwarder()) {
            // if copy mode, no need for registering AC
            if (this.isAwaited() && !this.copyMode) {
                boolean continuation = (FuturePool.getBodiesDestination() != null);

                // if continuation=false, no destination is registred:
                // - either ac are disabled,
                // - or this future is serialized in a migration forwarder.

                // identify the sender for regsitering continuation and determine if we are in a migration formwarder
                Body sender = LocalBodyStore.getInstance().getLocalBody(senderID);

                // it's a halfbody...
                if (sender == null) {
                    sender = LocalBodyStore.getInstance().getLocalHalfBody(senderID);
                }
                if (sender != null) { // else we are in a migration forwarder
                    if (continuation) {
                        /* The written future will be updated by the writing body */
                        writtenUpdater = PAActiveObject.getBodyOnThis();
                        for (UniversalBody dest : FuturePool.getBodiesDestination()) {
                            sender.getFuturePool().addAutomaticContinuation(id, dest);
                        }
                    } else {
                        // its not a copy and not a continuation: wait for the result
                        this.waitFor();
                    }
                }
            }
        } else {
            // Maybe this FutureProxy has been added into FuturePool by readObject
            // Remove it and restore continuation
            ArrayList<Future> futures = FuturePool.getIncomingFutures();
            if (futures != null) {
                for (int i = 0; i < futures.size(); i++) {
                    Future fp = futures.get(i);
                    if (fp.getFutureID().equals(this.getFutureID())) {
                        FuturePool.removeIncomingFutures();
                    }
                }
            }
        }

        // for future that are deepcopied then not registered in any futurepool
        out.writeObject(senderID);
        // Pass the result
        out.writeObject(target);
        // Pass the id
        out.writeObject(id);
        // Pass a reference to the updater
        out.writeObject(writtenUpdater.getRemoteAdapter());

        // serialize the stack context or null
        if (enableStack) {
            out.writeObject(callerContext);
        } else {
            out.writeObject(null);
        }
        out.writeObject(currentMainStackElement);
    }

    /**
     * the use of the synchronized keyword in readObject is meant to prevent race conditions on
     * futures -- do not remove it.
     */
    private synchronized void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        senderID = (UniqueID) in.readObject();
        target = (MethodCallResult) in.readObject();
        id = (FutureID) in.readObject();
        updater = (UniversalBody) in.readObject();
        callerContext = (StackTraceElement[]) in.readObject();
        currentMainStackElement = (StackTraceElement) in.readObject();
        // register all incoming futures, even for migration or checkpointing
        if (this.isAwaited()) {
            FuturePool.registerIncomingFuture(this);
        }
        copyMode = false;
    }

    //
    // -- PRIVATE STATIC METHODS -----------------------------------------------
    //
    private static boolean isFutureObject(Object obj) {
        // If obj is not reified, it cannot be a future
        if (!(MOP.isReifiedObject(obj))) {
            return false;
        }

        // Being a future object is equivalent to have a stub/proxy pair
        // where the proxy object implements the interface FUTURE_PROXY_INTERFACE
        // if the proxy does not inherit from FUTURE_PROXY_ROOT_CLASS
        // it is not a future
        Class<?> proxyclass = ((StubObject) obj).getProxy().getClass();
        Class<?>[] ints = proxyclass.getInterfaces();
        for (int i = 0; i < ints.length; i++) {
            if (Constants.FUTURE_PROXY_INTERFACE.isAssignableFrom(ints[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return Returns the exceptionLevel.
     */
    public ExceptionMaskLevel getExceptionLevel() {
        return exceptionLevel;
    }

    /**
     * @param exceptionLevel The exceptionLevel to set.
     */
    public void setExceptionLevel(ExceptionMaskLevel exceptionLevel) {
        this.exceptionLevel = exceptionLevel;
    }

    /**
     * Add a method to call when the future is arrived, or call it now if the
     * future is already arrived.
     */
    public synchronized void addCallback(String methodName) throws NoSuchMethodException {
        if (this.callbacks == null) {
            this.callbacks = new LocalFutureUpdateCallbacks(this);
        }

        this.callbacks.add(methodName);

        if (this.isAvailable()) {
            this.callbacks.run();
            this.callbacks = null;
        }
    }

    //////////////////////////
    //////////////////////////
    ////FOR DEBUG PURPOSE/////
    //////////////////////////
    //////////////////////////
    public synchronized static int futureLength(Object future) {
        int res = 0;
        if ((MOP.isReifiedObject(future)) && ((((StubObject) future).getProxy()) instanceof Future)) {
            res++;
            Future f = (Future) (((StubObject) future).getProxy());
            Object gna = f.getResult();
            while ((MOP.isReifiedObject(gna)) && ((((StubObject) gna).getProxy()) instanceof Future)) {
                f = (Future) (((StubObject) gna).getProxy());
                gna = f.getResult();
                res++;
            }
        }
        return res;
    }

    public synchronized void setDispatchMonitor(DispatchMonitor dispatchMonitor) {
        this.dispatchMonitor = dispatchMonitor;
    }
}
