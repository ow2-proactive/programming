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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.util.*;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.annotation.ImmediateService;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.exceptions.InactiveBodyException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.*;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.body.tags.Tag;
import org.objectweb.proactive.core.body.tags.tag.DsiTag;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.jmx.mbean.BodyWrapper;
import org.objectweb.proactive.core.jmx.naming.FactoryName;
import org.objectweb.proactive.core.jmx.notification.NotificationType;
import org.objectweb.proactive.core.jmx.notification.RequestNotificationData;
import org.objectweb.proactive.core.jmx.server.ServerConnector;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.ObjectReferenceReplacer;
import org.objectweb.proactive.core.mop.ObjectReplacer;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class gives a common implementation of the Body interface. It provides
 * all the non specific behavior allowing sub-class to write the detail
 * implementation.
 * </p>
 * <p>
 * Each body is identify by an unique identifier.
 * </p>
 * <p>
 * All active bodies that get created in one JVM register themselves into a
 * table that allows to tack them done. The registering and deregistering is
 * done by the AbstractBody and the table is managed here as well using some
 * static methods.
 * </p>
 * <p>
 * In order to let somebody customize the body of an active object without
 * subclassing it, AbstractBody delegates lot of tasks to satellite objects that
 * implements a given interface. Abstract protected methods instantiate those
 * objects allowing subclasses to create them as they want (using customizable
 * factories or instance).
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0, 2001/10/23
 * @see org.objectweb.proactive.Body
 * @see UniqueID
 * @since ProActive 0.9
 */
public abstract class BodyImpl extends AbstractBody implements java.io.Serializable, BodyImplMBean {
    //
    // -- STATIC MEMBERS -----------------------------------------------
    //

    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /**
     * The component in charge of receiving reply
     */
    protected ReplyReceiver replyReceiver;

    /**
     * The component in charge of receiving request
     */
    protected RequestReceiver requestReceiver;

    // already checked methods
    private HashMap<String, HashSet<List<Class<?>>>> checkedMethodNames;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody. Used for serialization.
     */
    public BodyImpl() {
    }

    /**
     * Creates a new AbstractBody for an active object attached to a given node.
     *
     * @param reifiedObject the active object that body is for
     * @param nodeURL       the URL of the node that body is attached to
     * @param factory       the factory able to construct new factories for each type of meta objects
     *                      needed by this body
     */
    public BodyImpl(Object reifiedObject, String nodeURL, MetaObjectFactory factory)
            throws ActiveObjectCreationException {
        super(reifiedObject, nodeURL, factory);

        super.isProActiveInternalObject = reifiedObject instanceof ProActiveInternalObject;

        this.checkedMethodNames = new HashMap<String, HashSet<List<Class<?>>>>();

        this.requestReceiver = factory.newRequestReceiverFactory().newRequestReceiver();
        this.replyReceiver = factory.newReplyReceiverFactory().newReplyReceiver();

        setLocalBodyImpl(new ActiveLocalBodyStrategy(reifiedObject, factory.newRequestQueueFactory()
                .newRequestQueue(this.bodyID), factory.newRequestFactory()));
        this.localBodyStrategy.getFuturePool().setOwnerBody(this);

        // JMX registration
        if (!super.isProActiveInternalObject) {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName oname = FactoryName.createActiveObjectName(this.bodyID);
            if (!mbs.isRegistered(oname)) {
                super.mbean = new BodyWrapper(oname, this);
                try {
                    mbs.registerMBean(mbean, oname);
                } catch (InstanceAlreadyExistsException e) {
                    bodyLogger.error("A MBean with the object name " + oname + " already exists", e);
                } catch (MBeanRegistrationException e) {
                    bodyLogger.error("Can't register the MBean of the body", e);
                } catch (NotCompliantMBeanException e) {
                    bodyLogger.error("The MBean of the body is not JMX compliant", e);
                }
            }
        }

        // ImmediateService 
        initializeImmediateService(reifiedObject);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Receives a request for later processing. The call to this method is non blocking unless the
     * body cannot temporary receive the request.
     *
     * @param request the request to process
     * @throws java.io.IOException if the request cannot be accepted
     */
    @Override
    protected void internalReceiveRequest(Request request) throws java.io.IOException {
        // JMX Notification
        if (!isProActiveInternalObject && (this.mbean != null)) {
            String tagNotification = createTagNotification(request.getTags());
            RequestNotificationData requestNotificationData = new RequestNotificationData(
                request.getSourceBodyID(), request.getSenderNodeURL(), this.bodyID, this.nodeURL,
                request.getMethodName(), getRequestQueue().size() + 1, request.getSequenceNumber(),
                tagNotification);
            this.mbean.sendNotification(NotificationType.requestReceived, requestNotificationData);
        }

        // END JMX Notification

        // request queue length = number of requests in queue
        // + the one to add now

        this.requestReceiver.receiveRequest(request, this);
    }

    /**
     * Receives a reply in response to a former request.
     *
     * @param reply the reply received
     * @throws java.io.IOException if the reply cannot be accepted
     */
    @Override
    protected void internalReceiveReply(Reply reply) throws java.io.IOException {
        // JMX Notification
        if (!isProActiveInternalObject && (this.mbean != null) && reply.getResult().getException() == null) {
            String tagNotification = createTagNotification(reply.getTags());
            RequestNotificationData requestNotificationData = new RequestNotificationData(
                BodyImpl.this.bodyID, BodyImpl.this.getNodeURL(), reply.getSourceBodyID(), this.nodeURL,
                reply.getMethodName(), getRequestQueue().size() + 1, reply.getSequenceNumber(),
                tagNotification);
            this.mbean.sendNotification(NotificationType.replyReceived, requestNotificationData);
        }

        // END JMX Notification
        replyReceiver.receiveReply(reply, this, getFuturePool());
    }

    /**
     * Signals that the activity of this body, managed by the active thread has just stopped.
     *
     * @param completeACs if true, and if there are remaining AC in the futurepool, the AC thread is
     *                    not killed now; it will be killed after the sending of the last remaining AC.
     */
    @Override
    protected void activityStopped(boolean completeACs) {
        super.activityStopped(completeACs);

        // Copies the requests to the remaining request queue of inactive body
        // because they are removed once the localBodyStrategy is changed
        RequestQueue queue = new RequestQueueImpl(getID());
        Iterator<Request> it = localBodyStrategy.getRequestQueue().iterator();
        while (it.hasNext()) {
            queue.add(it.next());
        }

        InactiveLocalBodyStrategy inactiveLocalBodyStrategy = new InactiveLocalBodyStrategy();
        inactiveLocalBodyStrategy.setRemainingRequests(queue);

        try {
            this.localBodyStrategy.getRequestQueue().destroy();
        } catch (ProActiveRuntimeException e) {
            // this method can be called twos times if the automatic
            // continuation thread
            // is killed *after* the activity thread.
            bodyLogger.debug("Terminating already terminated body " + this.getID());
        }

        this.getFuturePool().terminateAC(completeACs);

        if (!completeACs) {
            setLocalBodyImpl(inactiveLocalBodyStrategy);
        } else {
            // the futurepool is still needed for remaining ACs
            inactiveLocalBodyStrategy.setFuturePool(this.getFuturePool());
            setLocalBodyImpl(inactiveLocalBodyStrategy);
        }

        // terminate request receiver
        this.requestReceiver.terminate();
    }

    public boolean checkMethod(String methodName) {
        return checkMethod(methodName, null);
    }

    @Deprecated
    public void setImmediateService(String methodName) {
        setImmediateService(methodName, false);
    }

    public void setImmediateService(String methodName, boolean uniqueThread) {
        checkImmediateServiceMode(methodName, null, uniqueThread);
        ((RequestReceiverImpl) this.requestReceiver).setImmediateService(methodName, uniqueThread);
    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes, boolean uniqueThread) {
        checkImmediateServiceMode(methodName, parametersTypes, uniqueThread);
        ((RequestReceiverImpl) this.requestReceiver).setImmediateService(methodName, parametersTypes,
                uniqueThread);
    }

    protected void initializeImmediateService(Object reifiedObject) {
        Method[] methods = reifiedObject.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            ImmediateService is = m.getAnnotation(ImmediateService.class);
            if (is != null) {
                setImmediateService(m.getName(), m.getParameterTypes(), is.uniqueThread());
            }
        }
    }

    private void checkImmediateServiceMode(String methodName, Class<?>[] parametersTypes, boolean uniqueThread) {
        if (parametersTypes == null) { // all args               
            if (!checkMethod(methodName)) {
                throw new NoSuchMethodError(methodName + " is not defined in " +
                    getReifiedObject().getClass().getName());
            }
        } else { // args are specified
            if (!checkMethod(methodName, parametersTypes)) {
                String signature = methodName + "(";
                for (int i = 0; i < parametersTypes.length; i++) {
                    signature += parametersTypes[i] + ((i < parametersTypes.length - 1) ? "," : "");
                }
                signature += " is not defined in " + getReifiedObject().getClass().getName();
                throw new NoSuchMethodError(signature);
            }
        }
    }

    public void removeImmediateService(String methodName) {
        ((RequestReceiverImpl) this.requestReceiver).removeImmediateService(methodName);
    }

    public void removeImmediateService(String methodName, Class<?>[] parametersTypes) {
        ((RequestReceiverImpl) this.requestReceiver).removeImmediateService(methodName, parametersTypes);
    }

    public void updateNodeURL(String newNodeURL) {
        this.nodeURL = newNodeURL;
    }

    @Override
    public boolean isInImmediateService() throws IOException {
        return this.requestReceiver.isInImmediateService();
    }

    public boolean checkMethod(String methodName, Class<?>[] parametersTypes) {
        if (this.checkedMethodNames.containsKey(methodName)) {
            if (parametersTypes != null) {
                // the method name with the right signature has already been
                // checked
                List<Class<?>> parameterTlist = Arrays.asList(parametersTypes);
                HashSet<List<Class<?>>> signatures = this.checkedMethodNames.get(methodName);

                if (signatures.contains(parameterTlist)) {
                    return true;
                }
            } else {
                // the method name has already been checked
                return true;
            }
        }

        // check if the method is defined as public
        Class<?> reifiedClass = getReifiedObject().getClass();
        boolean exists = org.objectweb.proactive.core.mop.Utils.checkMethodExistence(reifiedClass,
                methodName, parametersTypes);

        if (exists) {
            storeInMethodCache(methodName, parametersTypes);

            return true;
        }

        return false;
    }

    /**
     * Stores the given method name with the given parameters types inside our method signature
     * cache to avoid re-testing them
     *
     * @param methodName      name of the method
     * @param parametersTypes parameter type list
     */
    private void storeInMethodCache(String methodName, Class<?>[] parametersTypes) {
        List<Class<?>> parameterTlist = null;

        if (parametersTypes != null) {
            parameterTlist = Arrays.asList(parametersTypes);
        }

        // if we already know a version of this method, we store the new version
        // in the existing set
        if (this.checkedMethodNames.containsKey(methodName) && (parameterTlist != null)) {
            HashSet<List<Class<?>>> signatures = this.checkedMethodNames.get(methodName);
            signatures.add(parameterTlist);
        }
        // otherwise, we create a set containing a single element
        else {
            HashSet<List<Class<?>>> signatures = new HashSet<List<Class<?>>>();

            if (parameterTlist != null) {
                signatures.add(parameterTlist);
            }

            checkedMethodNames.put(methodName, signatures);
        }
    }

    // Create the string from tag data for the notification
    private String createTagNotification(MessageTags tags) {
        String result = "";
        if (tags != null) {
            for (Tag tag : tags.getTags()) {
                result += tag.getNotificationMessage();
            }
        }
        return result;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    //
    // -- inner classes -----------------------------------------------
    //
    private class ActiveLocalBodyStrategy implements LocalBodyStrategy, java.io.Serializable {
        /**
         * A pool future that contains the pending future objects
         */
        protected FuturePool futures;

        /**
         * The reified object target of the request processed by this body
         */
        protected Object reifiedObject;
        protected BlockingRequestQueue requestQueue;
        protected RequestFactory internalRequestFactory;
        private long absoluteSequenceID;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public ActiveLocalBodyStrategy(Object reifiedObject, BlockingRequestQueue requestQueue,
                RequestFactory requestFactory) {
            this.reifiedObject = reifiedObject;
            this.futures = new FuturePool();
            this.requestQueue = requestQueue;
            this.internalRequestFactory = requestFactory;
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        //
        // -- implements LocalBody
        // -----------------------------------------------
        //
        public FuturePool getFuturePool() {
            return this.futures;
        }

        public BlockingRequestQueue getRequestQueue() {
            return this.requestQueue;
        }

        public Object getReifiedObject() {
            return this.reifiedObject;
        }

        /**
         * Serves the request. The request should be removed from the request queue before serving,
         * which is correctly done by all methods of the Service class. However, this condition is
         * not ensured for custom calls on serve.
         */
        public void serve(Request request) {
            // push the new context
            LocalBodyStore.getInstance().pushContext(new Context(BodyImpl.this, request));

            try {
                serveInternal(request, null);
            } finally {
                LocalBodyStore.getInstance().popContext();
            }
        }

        /**
         * Serves the request with the given exception as result instead of the normal execution.
         * The request should be removed from the request queue before serving,
         * which is correctly done by all methods of the Service class. However, this condition is
         * not ensured for custom calls on serve.
         */
        public void serveWithException(Request request, Throwable exception) {
            // push the new context
            LocalBodyStore.getInstance().pushContext(new Context(BodyImpl.this, request));

            try {
                serveInternal(request, exception);
            } finally {
                LocalBodyStore.getInstance().popContext();
            }
        }

        private void serveInternal(Request request, Throwable exception) {
            if (request == null) {
                return;
            }

            // JMX Notification
            if (!isProActiveInternalObject && (mbean != null)) {
                String tagNotification = createTagNotification(request.getTags());
                RequestNotificationData data = new RequestNotificationData(request.getSourceBodyID(),
                    request.getSenderNodeURL(), BodyImpl.this.bodyID, BodyImpl.this.nodeURL,
                    request.getMethodName(), getRequestQueue().size(), request.getSequenceNumber(),
                    tagNotification);
                mbean.sendNotification(NotificationType.servingStarted, data);
            }

            // END JMX Notification
            Reply reply = null;

            // If the request is not a "terminate Active Object" request,
            // it is served normally.
            if (!isTerminateAORequest(request)) {
                if (exception != null) {

                    if ((exception instanceof Exception) && !(exception instanceof RuntimeException)) {
                        // if the exception is a checked exception, the method must declare in its throws statement, otherwise
                        // the future sent to the user will be invalid
                        boolean thrownFound = false;
                        for (Class<?> exptype : request.getMethodCall().getReifiedMethod()
                                .getExceptionTypes()) {
                            thrownFound = thrownFound || exptype.isAssignableFrom(exception.getClass());
                        }
                        if (!thrownFound) {
                            throw new IllegalArgumentException("Invalid Exception " + exception.getClass() +
                                ". The method " + request.getMethodCall().getReifiedMethod() +
                                " don't declare it to be thrown.");
                        }
                        reply = new ReplyImpl(BodyImpl.this.getID(), request.getSequenceNumber(),
                            request.getMethodName(), new MethodCallResult(null, exception));
                    } else {
                        reply = new ReplyImpl(BodyImpl.this.getID(), request.getSequenceNumber(),
                            request.getMethodName(), new MethodCallResult(null, exception));
                    }

                } else {
                    reply = request.serve(BodyImpl.this);
                }
            }

            if (reply == null) {
                if (!isActive()) {
                    return; // test if active in case of terminate() method
                    // otherwise eventProducer would be null
                }

                // JMX Notification
                if (!isProActiveInternalObject && (mbean != null)) {
                    String tagNotification = createTagNotification(request.getTags());
                    RequestNotificationData data = new RequestNotificationData(request.getSourceBodyID(),
                        request.getSenderNodeURL(), BodyImpl.this.bodyID, BodyImpl.this.nodeURL,
                        request.getMethodName(), getRequestQueue().size(), request.getSequenceNumber(),
                        tagNotification);
                    mbean.sendNotification(NotificationType.voidRequestServed, data);
                }

                // END JMX Notification
                return;
            }

            // JMX Notification
            if (!isProActiveInternalObject && (mbean != null) && reply.getResult().getException() == null) {
                String tagNotification = createTagNotification(request.getTags());
                RequestNotificationData data = new RequestNotificationData(request.getSourceBodyID(),
                    request.getSenderNodeURL(), BodyImpl.this.bodyID, BodyImpl.this.nodeURL,
                    request.getMethodName(), getRequestQueue().size(), request.getSequenceNumber(),
                    tagNotification);
                mbean.sendNotification(NotificationType.replySent, data);
            }

            // END JMX Notification
            ArrayList<UniversalBody> destinations = new ArrayList<UniversalBody>();
            destinations.add(request.getSender());
            this.getFuturePool().registerDestinations(destinations);

            // Modify result object
            Object initialObject = null;
            Object stubOnActiveObject = null;
            Object modifiedObject = null;
            ObjectReplacer objectReplacer = null;
            if (CentralPAPropertyRepository.PA_IMPLICITGETSTUBONTHIS.isTrue()) {
                initialObject = reply.getResult().getResultObjet();
                try {
                    PAActiveObject.getStubOnThis();
                    stubOnActiveObject = (Object) MOP.createStubObject(BodyImpl.this.getReifiedObject()
                            .getClass().getName(), BodyImpl.this.getRemoteAdapter());
                    objectReplacer = new ObjectReferenceReplacer(BodyImpl.this.getReifiedObject(),
                        stubOnActiveObject);
                    modifiedObject = objectReplacer.replaceObject(initialObject);
                    reply.getResult().setResult(modifiedObject);
                } catch (InactiveBodyException e) {
                    e.printStackTrace();
                } catch (MOPException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            // if the reply cannot be sent, try to sent the thrown exception
            // as result
            // Useful if the exception is due to the content of the result
            // (e.g. InvalidClassException)
            try {
                reply.send(request.getSender());
            } catch (Throwable e1) {
                // see PROACTIVE-1172
                // previously only IOException were caught but now that new communication protocols
                // can be added dynamically (remote objects) we can no longer suppose that only IOException
                // will be thrown i.e. a runtime exception sent by the protocol can go through the stack and
                // kill the service thread if not caught here.
                // We do not want the AO to be killed if he cannot send the result.
                try {
                    // trying to send the exception as result to fill the future.
                    // we want to inform the caller that the result cannot be set in
                    // the future for any reason. let's see if we can put the exception instead.
                    // works only if the exception is not due to a communication issue.
                    this.retrySendReplyWithException(reply, e1, request.getSender());
                } catch (Throwable retryException1) {
                    // log the issue on the AO side for debugging purpose
                    // the initial exception must be the one to appear in the log.
                    sendReplyExceptionsLogger.error(shortString() + " : Failed to send reply to method:" +
                        request.getMethodName() + " sequence: " + request.getSequenceNumber() + " by " +
                        request.getSenderNodeURL() + "/" + request.getSender(), e1);
                }
            }

            this.getFuturePool().removeDestinations();

            // Restore Result Object
            if (CentralPAPropertyRepository.PA_IMPLICITGETSTUBONTHIS.isTrue() && (objectReplacer != null)) {
                try {
                    objectReplacer.restoreObject();
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        // If a reply sending has failed, try to send the exception as reply
        private void retrySendReplyWithException(Reply reply, Throwable e, UniversalBody destination)
                throws Exception {

            //            Get current request TAGs from current context
            //            Request currentreq = LocalBodyStore.getInstance().getContext().getCurrentRequest();
            //            MessageTags tags = null;
            //
            //            if (currentreq != null)
            //                tags = currentreq.getTags();

            Reply exceptionReply = new ReplyImpl(reply.getSourceBodyID(), reply.getSequenceNumber(),
                reply.getMethodName(), new MethodCallResult(null, e));
            exceptionReply.send(destination);
        }

        public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody)
                throws IOException {
            long sequenceID = getNextSequenceID();

            MessageTags tags = applyTags(sequenceID);

            Request request = this.internalRequestFactory.newRequest(methodCall, BodyImpl.this,
                    future == null, sequenceID, tags);

            if (future != null) {
                future.setID(sequenceID);
                this.futures.receiveFuture(future);
            }

            // JMX Notification
            // TODO Write this section, after the commit of Arnaud
            // TODO Send a notification only if the destination doesn't
            // implement ProActiveInternalObject
            if (!isProActiveInternalObject && (mbean != null)) {
                ServerConnector serverConnector = ProActiveRuntimeImpl.getProActiveRuntime()
                        .getJMXServerConnector();

                // If the connector server is not active the connectorID can be
                // null
                if ((serverConnector != null) && serverConnector.getConnectorServer().isActive()) {
                    UniqueID connectorID = serverConnector.getUniqueID();

                    if (!connectorID.equals(destinationBody.getID())) {
                        String tagNotification = createTagNotification(tags);

                        mbean.sendNotification(
                                NotificationType.requestSent,
                                new RequestNotificationData(BodyImpl.this.bodyID, BodyImpl.this.getNodeURL(),
                                    destinationBody.getID(), destinationBody.getNodeURL(), methodCall
                                            .getName(), -1, request.getSequenceNumber(), tagNotification));
                    }
                }
            }

            // END JMX Notification

            request.send(destinationBody);
        }

        /**
         * Returns a unique identifier that can be used to tag a future, a request
         *
         * @return a unique identifier that can be used to tag a future, a request.
         */
        public synchronized long getNextSequenceID() {
            return BodyImpl.this.bodyID.hashCode() + ++this.absoluteSequenceID;
        }

        //
        // -- PROTECTED METHODS -----------------------------------------------
        //

        /**
         * Test if the MethodName of the request is "terminateAO" or "terminateAOImmediately". If
         * true, AbstractBody.terminate() is called
         *
         * @param request The request to serve
         * @return true if the name of the method is "terminateAO" or "terminateAOImmediately".
         */
        private boolean isTerminateAORequest(Request request) {
            boolean terminateRequest = (request.getMethodName()).startsWith("_terminateAO");

            if (terminateRequest) {
                terminate();
            }

            return terminateRequest;
        }

        /**
         * Propagate all tags attached to the current served request.
         *
         * @return The MessageTags for the propagation
         */
        private MessageTags applyTags(long sequenceID) {
            // apply the code of all message TAGs from current context
            Request currentreq = LocalBodyStore.getInstance().getContext().getCurrentRequest();
            MessageTags currentMessagetags = null;
            MessageTags nextTags = messageTagsFactory.newMessageTags();

            if (currentreq != null && (currentMessagetags = currentreq.getTags()) != null) {
                // there is a request with a MessageTags object in the current context
                for (Tag t : currentMessagetags.getTags()) {
                    Tag newTag = t.apply();
                    if (newTag != null)
                        nextTags.addTag(newTag);
                }
            }
            // Check the presence of the DSI Tag if enabled
            // Ohterwise add it
            if (CentralPAPropertyRepository.PA_TAG_DSF.isTrue()) {
                if (!nextTags.check(DsiTag.IDENTIFIER)) {
                    nextTags.addTag(new DsiTag(bodyID, sequenceID));
                }
            }
            return nextTags;
        }
    }

    // end inner class LocalBodyImpl
    protected class InactiveLocalBodyStrategy implements LocalBodyStrategy, java.io.Serializable {
        // An inactive body strategy can have a futurepool if some ACs to do
        // remain after the termination of the active object
        private FuturePool futures;

        // Used to keep track of old requests that were still in the request queue
        // before to apply this new inactive local body strategy
        private RequestQueue remainingRequests;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public InactiveLocalBodyStrategy() {

        }

        public InactiveLocalBodyStrategy(FuturePool remainingsACs) {
            this.futures = remainingsACs;
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        //
        // -- implements LocalBody
        // -----------------------------------------------
        //
        public FuturePool getFuturePool() {
            return this.futures;
        }

        public BlockingRequestQueue getRequestQueue() {
            throw new InactiveBodyException(BodyImpl.this);
        }

        public RequestQueue getHighPriorityRequestQueue() {
            throw new InactiveBodyException(BodyImpl.this);
        }

        public Object getReifiedObject() {
            throw new InactiveBodyException(BodyImpl.this);
        }

        public void serve(Request request) {
            throw new InactiveBodyException(BodyImpl.this, (request != null) ? request.getMethodName()
                    : "null request");
        }

        @Override
        public void serveWithException(Request request, Throwable exception) {
            throw new InactiveBodyException(BodyImpl.this, (request != null) ? request.getMethodName()
                    : "null request");
        }

        public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody)
                throws java.io.IOException {
            throw new InactiveBodyException(BodyImpl.this, destinationBody.getNodeURL(),
                destinationBody.getID(), methodCall.getName());
        }

        /*
         * @see org.objectweb.proactive.core.body.LocalBodyStrategy#getNextSequenceID()
         */
        public long getNextSequenceID() {
            return 0;
        }

        public RequestQueue getRemainingRequests() {
            return remainingRequests;
        }

        public void setFuturePool(FuturePool pool) {
            futures = pool;
        }

        public void setRemainingRequests(RequestQueue requestQueue) {
            remainingRequests = requestQueue;
        }

    }

    // end inner class InactiveBodyException
}
