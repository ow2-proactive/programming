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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.exceptions.HalfBodyException;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyReceiver;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFactory;
import org.objectweb.proactive.core.body.request.RequestQueue;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.body.tags.tag.DsiTag;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class HalfBody extends AbstractBody {

    private static final long serialVersionUID = 62L;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //

    /** The component in charge of receiving reply */
    private ReplyReceiver replyReceiver;

    public synchronized static HalfBody getHalfBody(MetaObjectFactory factory) {
        try {
            return new HalfBody(factory);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        return null;
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    private HalfBody(MetaObjectFactory factory) throws ActiveObjectCreationException, NodeException {
        super(initializeHalfBodyName(), NodeFactory.getHalfBodiesNode().getNodeInformation().getURL(),
                factory);

        this.replyReceiver = factory.newReplyReceiverFactory().newReplyReceiver();
        setLocalBodyImpl(new HalfLocalBodyStrategy(factory.newRequestFactory()));
        this.localBodyStrategy.getFuturePool().setOwnerBody(this);
    }

    /**
     * This method infers the name of the class and method responsible for the half body creation
     * @return a dummy object containing
     */
    private static Object initializeHalfBodyName() {
        StackTraceElement[] elems = new Throwable().getStackTrace();
        for (StackTraceElement elem : elems) {
            String className = elem.getClassName();
            if (!(className.startsWith(UniqueID.class.getPackage().getName()) ||
                className.startsWith(PAVersion.class.getPackage().getName()) || className
                    .startsWith(Throwable.class.getPackage().getName()))) {
                return elem;
            }
        }
        // if we didn't find anything we send the last element
        return elems[elems.length - 1];
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Receives a request for later processing. The call to this method is non blocking
     * unless the body cannot temporary receive the request.
     * @param request the request to process
     * @exception java.io.IOException if the request cannot be accepted
     */
    @Override
    protected void internalReceiveRequest(Request request) throws java.io.IOException {
        throw new ProActiveRuntimeException(
            "The method 'receiveRequest' is not implemented in class HalfBody.");
    }

    /**
     * Receives a reply in response to a former request.
     * @param reply the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    @Override
    protected void internalReceiveReply(Reply reply) throws java.io.IOException {
        this.replyReceiver.receiveReply(reply, this, getFuturePool());
    }

    @Deprecated
    public void setImmediateService(String methodName) {
        setImmediateService(methodName, false);
    }

    public void setImmediateService(String methodName, boolean uniqueThread) {
        throw new HalfBodyException();
    }

    public void removeImmediateService(String methodName) {
        throw new HalfBodyException();
    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes, boolean uniqueThread) {
        throw new HalfBodyException();
    }

    public void removeImmediateService(String methodName, Class<?>[] parametersTypes) {
        throw new HalfBodyException();
    }

    @Override
    public boolean isInImmediateService() {
        throw new HalfBodyException();
    }

    public void updateNodeURL(String newNodeURL) {
        throw new HalfBodyException();
    }

    //
    // -- inner classes -----------------------------------------------
    //
    private class HalfLocalBodyStrategy implements LocalBodyStrategy, java.io.Serializable {

    private static final long serialVersionUID = 62L;

        /** A pool future that contains the pending future objects */
        protected FuturePool futures;
        protected RequestFactory internalRequestFactory;
        private long absoluteSequenceID;

        //
        // -- CONSTRUCTORS -----------------------------------------------
        //
        public HalfLocalBodyStrategy(RequestFactory requestFactory) {
            this.futures = new FuturePool();
            this.internalRequestFactory = requestFactory;
        }

        //
        // -- PUBLIC METHODS -----------------------------------------------
        //
        //
        // -- implements LocalBody -----------------------------------------------
        //
        public FuturePool getFuturePool() {
            return this.futures;
        }

        public BlockingRequestQueue getRequestQueue() {
            throw new HalfBodyException();
        }

        public RequestQueue getHighPriorityRequestQueue() {
            throw new HalfBodyException();
        }

        public Object getReifiedObject() {
            throw new HalfBodyException();
        }

        public void serve(Request request) {
            throw new HalfBodyException();
        }

        @Override
        public void serveWithException(Request request, Throwable exception) {
            throw new HalfBodyException();
        }

        public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody)
                throws java.io.IOException {
            long sequenceID = getNextSequenceID();

            // Create DSI MessageTag
            MessageTags tags = null;
            if (CentralPAPropertyRepository.PA_TAG_DSF.isTrue()) {
                tags = messageTagsFactory.newMessageTags();
                tags.addTag(new DsiTag(bodyID, sequenceID));
            }

            Request request = this.internalRequestFactory.newRequest(methodCall, HalfBody.this,
                    future == null, sequenceID, tags);

            if (future != null) {
                future.setID(sequenceID);
                this.futures.receiveFuture(future);
            }

            request.send(destinationBody);
        }

        //
        // -- PROTECTED METHODS -----------------------------------------------
        //

        /**
         * Returns a unique identifier that can be used to tag a future, a request
         * @return a unique identifier that can be used to tag a future, a request.
         */
        public synchronized long getNextSequenceID() {
            return HalfBody.this.bodyID.hashCode() + ++this.absoluteSequenceID;
        }
    }

    public long getNextSequenceID() {
        return this.localBodyStrategy.getNextSequenceID();
    }

    public boolean checkMethod(String methodName, Class<?>[] parametersTypes) {
        throw new HalfBodyException();
    }

    public boolean checkMethod(String methodName) {
        throw new HalfBodyException();
    }

    //    @Override
    //    protected RemoteRemoteObject register(URI uri)
    //        throws UnknownProtocolException {
    //        try {
    //            return RemoteObjectHelper.getFactoryFromURL(uri)
    //                                     .newRemoteObject(this.roe.getRemoteObject());
    //        } catch (ProActiveException e) {
    //            // TODO Auto-generated catch block
    //            e.printStackTrace();
    //        }
    //        return null;
    //    }
}
