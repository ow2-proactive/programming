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
package org.objectweb.proactive.core.body.request;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.reply.ReplyImpl;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RequestImpl extends MessageImpl implements Request, java.io.Serializable {

    private static final long serialVersionUID = 61L;
    public static Logger logger = ProActiveLogger.getLogger(Loggers.REQUESTS);
    private static final Logger oneWayExceptionsLogger = ProActiveLogger
            .getLogger(Loggers.EXCEPTIONS_ONE_WAY);
    protected MethodCall methodCall;

    /**
     * Indicates if the method has been sent through a forwarder
     */
    protected int sendCounter;

    /** transient because we deal with the serialization of this variable
       in a custom manner. see writeObject method*/
    protected transient UniversalBody sender;
    protected String codebase;

    //Non Functional requests
    protected boolean isNFRequest = false;
    protected int nfRequestPriority;
    protected String senderNodeURI;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    public RequestImpl() {
    }

    // Constructor of simple requests
    public RequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID) {
        this(methodCall, sender, isOneWay, nextSequenceID, false, null);
    }

    public RequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID,
            MessageTags tags) {
        this(methodCall, sender, isOneWay, nextSequenceID, false, tags);
    }

    // Constructor of non functional requests without priority
    public RequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID,
            boolean isNFRequest) {
        this(methodCall, sender, isOneWay, nextSequenceID, false, Request.NFREQUEST_NO_PRIORITY, null);
    }

    public RequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID,
            boolean isNFRequest, MessageTags tags) {
        this(methodCall, sender, isOneWay, nextSequenceID, false, Request.NFREQUEST_NO_PRIORITY, tags);
    }

    // Constructor of non functional requests with priority
    public RequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID,
            boolean isNFRequest, int nfRequestPriority, MessageTags tags) {
        super(sender.getID(), nextSequenceID, isOneWay, methodCall.getName(), tags);
        this.methodCall = methodCall;
        this.sender = sender;
        this.isNFRequest = isNFRequest;
        this.nfRequestPriority = nfRequestPriority;
        if (sender != null) {
            this.senderNodeURI = sender.getNodeURL();
        } else {
            this.senderNodeURI = "";
        }

    }

    public RequestImpl(MethodCall methodCall, UniversalBody sender, boolean isOneWay, long nextSequenceID,
            boolean isNFRequest, int nfRequestPriority) {
        this(methodCall, sender, isOneWay, nextSequenceID, isNFRequest, nfRequestPriority, null);
    }

    // Constructor of synchronous requests
    public RequestImpl(MethodCall methodCall, boolean isOneWay, MessageTags tags) {
        super(null, 0, isOneWay, methodCall.getName(), tags);
        this.methodCall = methodCall;
        this.senderNodeURI = "";
    }

    public RequestImpl(MethodCall methodCall, boolean isOneWay) {
        this(methodCall, isOneWay, null);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- Implements Request -----------------------------------------------
    //
    public void send(UniversalBody destinationBody) throws java.io.IOException {
        //System.out.println("RequestSender: sendRequest  " + methodName + " to destination");
        this.sendCounter++;
        sendRequest(destinationBody);
    }

    public UniversalBody getSender() {
        return this.sender;
    }

    public Reply serve(Body targetBody) {
        if (logger.isDebugEnabled()) {
            logger.debug("Serving " + this.getMethodName());
        }
        MethodCallResult result;
        try {
            result = serveInternal(targetBody);
        } catch (ServeException e) {
            /* Non Functional Exception */
            result = new MethodCallResult(null, new ProActiveRuntimeException(e));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("result: " + result);
        }
        if (this.isOneWay) {
            Throwable exception = result.getException();
            if (exception != null) {
                oneWayExceptionsLogger.error(exception, exception);
            }
            return null;
        }
        return createReply(targetBody, result);
    }

    public boolean hasBeenForwarded() {
        return this.sendCounter > 1;
    }

    public void resetSendCounter() {
        this.sendCounter = 0;
    }

    public Object getParameter(int index) {
        return this.methodCall.getParameter(index);
    }

    public MethodCall getMethodCall() {
        return this.methodCall;
    }

    public void notifyReception(UniversalBody bodyReceiver) throws java.io.IOException {
        if (!hasBeenForwarded()) {
            return;
        }

        //System.out.println("the request has been forwarded times");
        //we know c.res is a remoteBody since the call has been forwarded
        //if it is null, this is a one way call
        if (this.sender != null) {
            this.sender.updateLocation(bodyReceiver.getID(), bodyReceiver.getRemoteAdapter());
        }
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected MethodCallResult serveInternal(Body targetBody) throws ServeException {
        Object result = null;
        Throwable exception = null;
        try {
            result = this.methodCall.execute(targetBody.getReifiedObject());
        } catch (MethodCallExecutionFailedException e) {
            throw new ServeException("Error while serving", e);
        } catch (java.lang.reflect.InvocationTargetException e) {
            exception = e.getTargetException();
        }

        return new MethodCallResult(result, exception);
    }

    protected Reply createReply(Body targetBody, MethodCallResult result) {
        // Get request TAGs from current context
        Request currentreq = LocalBodyStore.getInstance().getContext().getCurrentRequest();
        MessageTags tags = null;

        if (currentreq != null)
            tags = currentreq.getTags();

        return new ReplyImpl(targetBody.getID(), this.sequenceNumber, this.methodName, result, tags);
    }

    protected void sendRequest(UniversalBody destinationBody) throws IOException {
        destinationBody.receiveRequest(this);
    }

    //
    // -- PRIVATE METHODS FOR SERIALIZATION
    // -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        writeTheObject(out);
    }

    protected void writeTheObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        if (this.sender != null) {
            out.writeObject(this.sender.getRemoteAdapter());
        } else {
            out.writeObject(this.sender);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        readTheObject(in);
    }

    protected void readTheObject(java.io.ObjectInputStream in) throws java.io.IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        sender = (UniversalBody) in.readObject(); // it is actually a UniversalBody
    }

    //
    // -- METHODS DEALING WITH NON FUNCTIONAL REQUESTS
    //
    public boolean isFunctionalRequest() {
        return this.isNFRequest;
    }

    public void setFunctionalRequest(boolean isFunctionalRequest) {
        this.isNFRequest = isFunctionalRequest;
    }

    public void setNFRequestPriority(int nfReqPriority) {
        this.nfRequestPriority = nfReqPriority;
    }

    public int getNFRequestPriority() {
        return this.nfRequestPriority;
    }

    public String getSenderNodeURL() {
        return this.senderNodeURI;
    }

}
