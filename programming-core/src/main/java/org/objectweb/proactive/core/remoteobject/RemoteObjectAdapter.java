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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet.NotYetExposedException;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team The RemoteObjectAdapter is used to hide the fact
 *         that the remote object called is distant.
 */
public class RemoteObjectAdapter implements RemoteObject, Serializable {

    private static final long serialVersionUID = 62L;
    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

    static final String UNKNOWN = "[unknown]";

    protected RemoteObjectSet remoteObjectSet;

    /**
     * a stub on the object reified by the remote object
     */
    protected Object stub;

    /**
     * the URI where the remote remote is bound
     */
    protected String uri;

    protected String displayROURI = UNKNOWN;
    protected String displayCaller = UNKNOWN;

    /**
     * Array of methods belonging to the RemoteObject class. These methods are
     * reified using the RemoteObjectRequest class
     */
    protected static Method[] methods;

    /**
     * Array of methods belonging to the InternalRemoteRemoteObject class. These
     * methods are reified using the InternalRemoteRemoteObjectRequest class
     */
    protected static Method[] internalRROMethods;

    static {
        try {
            methods = new Method[20];
            methods[0] = RemoteObject.class.getDeclaredMethod("getObjectProxy", new Class<?>[0]);
            methods[1] = RemoteObject.class.getDeclaredMethod("getObjectProxy",
                    new Class<?>[] { RemoteRemoteObject.class });
            methods[2] = RemoteObject.class.getDeclaredMethod("getClassName", new Class<?>[0]);
            methods[3] = RemoteObject.class.getDeclaredMethod("getTargetClass", new Class<?>[0]);
            methods[4] = RemoteObject.class.getDeclaredMethod("getProxyName", new Class<?>[0]);
            methods[5] = RemoteObject.class.getDeclaredMethod("getAdapterClass", new Class<?>[0]);
            // methods[6] =
            // RemoteObject.class.getDeclaredMethod("getRemoteObjectProperties",
            // new Class<?>[0]);
            methods[7] = RemoteObject.class.getDeclaredMethod("getAdapter", new Class<?>[0]);
            methods[8] = RemoteObject.class.getDeclaredMethod("getName", new Class<?>[0]);

            internalRROMethods = new Method[20];
            internalRROMethods[0] = InternalRemoteRemoteObject.class.getDeclaredMethod("getObjectProxy",
                    new Class<?>[] {});
            internalRROMethods[2] = InternalRemoteRemoteObject.class.getDeclaredMethod("getURI",
                    new Class<?>[0]);
            internalRROMethods[3] = InternalRemoteRemoteObject.class.getDeclaredMethod("getRemoteObjectSet",
                    new Class<?>[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public RemoteObjectAdapter() {
    }

    public RemoteObjectAdapter(RemoteRemoteObject ro) throws ProActiveException {

        if (this.displayCaller.equals(UNKNOWN)) {
            this.displayCaller = Thread.currentThread().getName();
        }

        // Retrieve from the RemoteRemoteObject (client-side) gave as parameter, all the exposed protocols on server-side
        this.remoteObjectSet = this.getRemoteObjectSet(ro);

        // At initialization of the adapter, we use the RemoteObjectSet, which will contain a list of url (one for each used protocol)
        displayROURI = this.remoteObjectSet.toString();
    }

    public void forceProtocol(String protocol) throws UnknownProtocolException, NotYetExposedException {
        this.remoteObjectSet.forceProtocol(protocol);
    }

    public Reply receiveMessage(Request message) throws ProActiveException, IOException {
        try {

            // for each RRO ordered from faster to slower
            return remoteObjectSet.receiveMessage(message);
        } catch (ProActiveException e) {
            throw new IOException6("Exception received when trying to contact remote object " + displayROURI,
                e);
        } catch (IOException e) {
            // Log for keeping a trace
            String methodName = message.getMethodName();
            if (!methodName.equals("killRT")) {
                LOGGER_RO.warn(displayCaller + " : unable to contact remote object " + displayROURI +
                    " when calling method " + message.getMethodName(), e);
            } else if (LOGGER_RO.isDebugEnabled()) {
                LOGGER_RO.debug(displayCaller + " : unable to contact remote object " + displayROURI +
                    " when calling method " + message.getMethodName(), e);
            }
            return new SynchronousReplyImpl(new MethodCallResult(null, e));
        }
    }

    // RemoteObjects
    public Object getObjectProxy() throws ProActiveException {
        if (this.stub == null) {
            // this.stub = this.remoteObject.getObjectProxy();
            try {
                MethodCall mc = MethodCall.getMethodCall(internalRROMethods[0], new Object[0],
                        new HashMap<TypeVariable<?>, Class<?>>());
                Request r = new InternalRemoteRemoteObjectRequest(mc);

                SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

                this.stub = reply.getResult().getResult();
                ((StubObject) this.stub).setProxy(new SynchronousProxy(null, new Object[] { this }));
            } catch (SecurityException e) {
                LOGGER_RO.info(displayCaller +
                    " : exception in remote object adapter while forwarding the method call to " +
                    displayROURI, e);
            } catch (IOException e) {
                LOGGER_RO.info(displayCaller +
                    " : exception in remote object adapter while forwarding the method call to " +
                    displayROURI, e);
            }
        }
        return this.stub;
    }

    public Object getObjectProxy(RemoteRemoteObject rmo) throws ProActiveException {
        if (this.stub == null) {
            this.getObjectProxy();
        }
        return this.stub;
    }

    public String getName() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[8], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

            String answer = (String) reply.getResult().getResult();
            // We keep locally the result of this call as the name can be used later when identifying the remote object
            return answer;
        } catch (SecurityException e) {
            LOGGER_RO.info("exception in remote object adapter while forwarding the method call to " +
                displayROURI, e);
        } catch (ProActiveException e) {
            LOGGER_RO.info("exception in remote object adapter while forwarding the method call to " +
                displayROURI, e);
        } catch (IOException e) {
            LOGGER_RO.info("exception in remote object adapter while forwarding the method call to " +
                displayROURI, e);
        }

        return null;
    }

    public String getClassName() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[2], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

            return (String) reply.getResult().getResult();
        } catch (SecurityException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        } catch (ProActiveException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        } catch (IOException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        }

        return null;
    }

    public String getProxyName() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[4], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

            return (String) reply.getResult().getResult();
        } catch (ProActiveException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        } catch (IOException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        }
        return null;
    }

    private RemoteObjectSet getRemoteObjectSet(RemoteRemoteObject rro) throws ProActiveException {
        try {
            MethodCall mc = MethodCall.getMethodCall(internalRROMethods[3], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new InternalRemoteRemoteObjectRequest(mc);

            // Use the first created RRO for finding all possible others
            SynchronousReplyImpl reply = (SynchronousReplyImpl) rro.receiveMessage(r);

            RemoteObjectSet ros = (RemoteObjectSet) reply.getResult().getResult();
            return ros;
        } catch (ProActiveException pae) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    pae);
            throw pae;
        } catch (IOException ioe) {
            throw new ProActiveException(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                ioe);
        }

    }

    @Override
    public int hashCode() {
        return this.remoteObjectSet.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // Self reference
        if (this == obj) {
            return true;
        }
        // Null reference
        if (obj == null) {
            return false;
        }
        // Class mismatch
        if (getClass() != obj.getClass()) {
            return false;
        }

        // Check only the equality of the default RRO
        return this.remoteObjectSet.getDefault().equals(
                ((RemoteObjectAdapter) obj).remoteObjectSet.getDefault());
    }

    public Class<?> getTargetClass() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[3], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

            return (Class<?>) reply.getResult().getResult();
        } catch (ProActiveException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        } catch (IOException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        }
        return null;
    }

    public Class<?> getAdapterClass() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[5], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new RemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

            return (Class<?>) reply.getResult().getResult();
        } catch (Exception e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        }
        return null;
    }

    // TODO: write a public method which does't throw exception.
    public URI getURI() throws ProActiveException {
        try {
            MethodCall mc = MethodCall.getMethodCall(internalRROMethods[2], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());

            Request r = new InternalRemoteRemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

            URI uri = (URI) reply.getResult().getResult();

            return uri;
        } catch (IOException e) {
            throw new ProActiveException(e);
        } catch (SecurityException e) {
            throw new ProActiveException(e);
        }
    }

    protected URI getURI(RemoteRemoteObject rro) throws ProActiveException {
        try {
            MethodCall mc = MethodCall.getMethodCall(internalRROMethods[2], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());

            Request r = new InternalRemoteRemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) rro.receiveMessage(r);

            return (URI) reply.getResult().getResult();
        } catch (IOException e) {
            throw new ProActiveException(e);
        } catch (SecurityException e) {
            throw new ProActiveException(e);
        }
    }

    public RemoteObjectProperties getRemoteObjectProperties() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[6], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());

            Request r = new InternalRemoteRemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);

            return (RemoteObjectProperties) reply.getResult().getResult();
        } catch (ProActiveException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        } catch (IOException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public Adapter getAdapter() {
        try {
            MethodCall mc = MethodCall.getMethodCall(methods[7], new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());

            Request r = new InternalRemoteRemoteObjectRequest(mc);

            SynchronousReplyImpl reply = (SynchronousReplyImpl) this.receiveMessage(r);
            return (Adapter) reply.getResult().getResult();
        } catch (ProActiveException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        } catch (IOException e) {
            LOGGER_RO.info(displayCaller +
                " : exception in remote object adapter while forwarding the method call to " + displayROURI,
                    e);
        }
        return null;
    }

    // This could lead to unwanted behaviour
    public RemoteObjectExposer getRemoteObjectExposer() {
        throw new ProActiveRuntimeException(
            "There is no reason to get the RemoteObjectExposer from the client side");
    }

    // This could lead to unwanted behaviour
    public void setRemoteObjectExposer(RemoteObjectExposer roe) {
        throw new ProActiveRuntimeException(
            "There is no reason to set the RemoteObjectExposer from the client side");
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
}
