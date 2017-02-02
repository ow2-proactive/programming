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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;


public class BodyRequest extends MessageImpl implements Request, java.io.Serializable {
    protected MethodCall methodCall;

    protected boolean isPriority;

    protected boolean isNFRequest;

    protected int nfRequestPriority;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    protected BodyRequest(Body targetBody, MessageTags tags, String methodName, Class<?>[] paramClasses,
            Object[] params) throws NoSuchMethodException {
        super(null, 0, true, methodName, tags);
        if (paramClasses == null) {
            paramClasses = new Class<?>[params.length];
            for (int i = 0; i < params.length; i++) {
                paramClasses[i] = params[i].getClass();
            }
        }
        methodCall = MethodCall.getMethodCall(targetBody.getClass().getMethod(methodName, paramClasses),
                                              params,
                                              (Map<TypeVariable<?>, Class<?>>) null);
    }

    public BodyRequest(Body targetBody, String methodName, Class<?>[] paramClasses, Object[] params, boolean isPriority,
            MessageTags tags) throws NoSuchMethodException {
        this(targetBody, tags, methodName, paramClasses, params);
        this.isPriority = isPriority;
    }

    public BodyRequest(Body targetBody, String methodName, Class<?>[] paramClasses, Object[] params, boolean isPriority)
            throws NoSuchMethodException {
        this(targetBody, null, methodName, paramClasses, params);
        this.isPriority = isPriority;
    }

    //Non functional BodyRequests constructor
    public BodyRequest(Body targetBody, MessageTags tags, String methodName, Class<?>[] paramClasses, Object[] params,
            boolean isNFRequest, int nfRequestPriority) throws NoSuchMethodException {
        this(targetBody, tags, methodName, paramClasses, params);
        this.isNFRequest = isNFRequest;
        this.nfRequestPriority = nfRequestPriority;
    }

    public BodyRequest(Body targetBody, String methodName, Class<?>[] paramClasses, Object[] params,
            boolean isNFRequest, int nfRequestPriority) throws NoSuchMethodException {
        this(targetBody, null, methodName, paramClasses, params);
        this.isNFRequest = isNFRequest;
        this.nfRequestPriority = nfRequestPriority;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- Implements Request -----------------------------------------------
    //
    public void send(UniversalBody destinationBody) throws java.io.IOException {
        if (!(destinationBody instanceof Body)) {
            throw new java.io.IOException("The destination body is not a local body");
        }
        if (!isPriority) {
            ((Body) destinationBody).getRequestQueue().add(this);
        } else {
            ((Body) destinationBody).getRequestQueue().addToFront(this);
        }
    }

    public Reply serve(Body targetBody) {
        serveInternal(targetBody);
        return null;
    }

    @Override
    public boolean isOneWay() {
        return true;
    }

    public boolean hasBeenForwarded() {
        return false;
    }

    public void resetSendCounter() {
    }

    public UniversalBody getSender() {
        return null;
    }

    public Object getParameter(int index) {
        return methodCall.getParameter(index);
    }

    public MethodCall getMethodCall() {
        return methodCall;
    }

    public void notifyReception(UniversalBody bodyReceiver) throws java.io.IOException {
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    protected void serveInternal(Body targetBody) {
        try {
            methodCall.execute(targetBody);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (MethodCallExecutionFailedException e) {
            e.printStackTrace();
        }
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

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.core.body.request.Request#getSenderNodeURI()
     */
    public String getSenderNodeURL() {
        return null;
    }
}
