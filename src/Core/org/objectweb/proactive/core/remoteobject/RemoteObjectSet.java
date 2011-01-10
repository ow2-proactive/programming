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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.rmi.dgc.VMID;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.remoteobject.benchmark.RemoteObjectBenchmark;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RemoteObjectSet implements Serializable, Observer {
    /**
     * 
     */
    private static final long serialVersionUID = 500L;

    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

    // *transient * Each RRO need a special marshalling processing
    // * Use LinkedHashMap for keeping the insertion-order
    private transient LinkedHashMap<URI, RemoteRemoteObject> rros;
    private HashSet<RemoteRemoteObject> unreliables;
    private transient RemoteRemoteObject defaultRO;
    private static Method getURI;
    private String[] order = new String[] {};
    private String remoteRuntimeName;
    private RemoteRemoteObject forcedProtocol = null;
    private VMID vmid = null;
    private transient URI defaultURI = null;

    static {
        try {
            getURI = InternalRemoteRemoteObject.class.getDeclaredMethod("getURI", new Class<?>[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public RemoteObjectSet(RemoteRemoteObject defaultRRO, Collection<RemoteRemoteObject> rros)
            throws IOException {
        this.rros = new LinkedHashMap<URI, RemoteRemoteObject>();
        this.unreliables = new HashSet<RemoteRemoteObject>();
        for (RemoteRemoteObject rro : rros) {
            this.add(rro);
        }
        try {
            this.defaultRO = defaultRRO;
            this.remoteRuntimeName = getPARuntimeName(defaultRO);
            this.defaultURI = getURI(defaultRRO);
        } catch (RemoteRemoteObjectException e) {
            throw new IOException("Cannot access the remoteObject " + defaultRRO + " : " + e.getMessage());
        }
    }

    /**
     * Select the best suited RemoteRemoteObject (protocol related), and send it the Request
     * Fallback to default (according to the PA_COMMUNICATION_PROTOCOL property) if necessary
     */
    public Reply receiveMessage(Request message) throws ProActiveException, RenegotiateSessionException,
            IOException {
        if (forcedProtocol != null) {
            return forcedProtocol.receiveMessage(message);
        }
        RemoteRemoteObject rro = null;
        // For each protocol already selected and sorted
        for (String protocol : order) {
            // * Find the corresponding RemoteRemoteObject
            // * Selection order is store for runtime so, the uri could not be used
            // * Use Iterator for removing during iteration
            for (Iterator<Entry<URI, RemoteRemoteObject>> it = rros.entrySet().iterator(); it.hasNext();) {
                try {
                    Entry<URI, RemoteRemoteObject> entry = it.next();
                    if (entry.getKey().getScheme().equalsIgnoreCase(protocol)) {
                        rro = entry.getValue();
                        Reply rep = rro.receiveMessage(message);
                        // The Exception is thrown on server side
                        // So it is encapsulate to be delivered on client side
                        Throwable t = rep.getResult().getException();
                        if (t != null) {
                            it.remove();
                            this.unreliables.add(rro);
                            continue;
                        }
                        return rep;
                    }
                    // These Exceptions happened on client side
                    // RMI doesn't act as others protocols and Exceptions aren't
                    // encapsulate, so they are catched here.
                } catch (ProActiveException pae) {
                    it.remove();
                    this.unreliables.add(rro);
                    continue;
                } catch (IOException io) {
                    it.remove();
                    this.unreliables.add(rro);
                    continue;
                } catch (RenegotiateSessionException rse) {
                    it.remove();
                    this.unreliables.add(rro);
                    continue;
                }

            }
        }
        // All RemoteRemoteObject lead to Exception, try with the default one
        return defaultRO.receiveMessage(message);
    }

    /**
     * Force the specified protocol to be used for all communication. Null value avoid the forcing.
     *
     * @throws UnknownProtocolException
     *          The protocol specified isn't known
     * @throws NotYetExposedException
     *          The Object isn't already exposed with this protocol
     */
    public void forceProtocol(String protocol) throws UnknownProtocolException, NotYetExposedException {
        if (protocol == null || protocol.length() == 0) {
            this.forcedProtocol = null;
            return;
        }
        // Protocols factories can be added dynamically, so the only way to
        // check a protocol existence is to check if it have a factory
        if (RemoteObjectProtocolFactoryRegistry.get(protocol) != null) {
            boolean exposed = false;
            for (URI uri : rros.keySet()) {
                if (uri.getScheme().equalsIgnoreCase(protocol)) {
                    this.forcedProtocol = rros.get(uri);
                    exposed = true;
                }
            }
            if (!exposed) {
                throw new NotYetExposedException("The object isn't exposed on protocol " + protocol);
            }
        } else {
            throw new UnknownProtocolException("\"" + protocol + "\"" + " isn't a valid protocol.");
        }
    }

    /**
     * Return the default RemoteRemoteObject
     */
    public RemoteRemoteObject getDefault() {
        return this.defaultRO;
    }

    /**
     * Return the URI of the default RemoteRemoteObject
     */
    public URI getDefaultURI() throws ProActiveException {
        return this.defaultURI;
    }

    /**
     * Add a RemoteRemoteObject (protocol specific) to the RemoteObjectSet
     * If it is unreliable, keep it aside for later possible use
     */
    public void add(RemoteRemoteObject rro) {
        try {
            this.rros.put(getURI(rro), rro);
        } catch (RemoteRemoteObjectException e) {
            this.unreliables.add(rro);
        }
    }

    /**
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectSet#add(RemoteRemoteObject)
     */
    public void add(Collection<RemoteRemoteObject> rros) {
        // If an older same rro is present, it will be updated
        for (RemoteRemoteObject rro : rros) {
            this.add(rro);
        }
    }

    /**
     * Send a non-functional internal request to get the URI of the RemoteRemoteObject
     */
    private URI getURI(RemoteRemoteObject rro) throws RemoteRemoteObjectException {
        try {
            MethodCall mc = MethodCall.getMethodCall(getURI, new Object[0],
                    new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new InternalRemoteRemoteObjectRequest(mc);
            Reply rep = rro.receiveMessage(r);
            return (URI) rep.getResult().getResult();
        } catch (ProActiveException e) {
            throw new RemoteRemoteObjectException(
                "RemoteObjectSet: can't access RemoteObject through " + rro, e);
        } catch (IOException e) {
            throw new RemoteRemoteObjectException(
                "RemoteObjectSet: can't access RemoteObject through " + rro, e);
        } catch (ProActiveRuntimeException e) {
            throw new RemoteRemoteObjectException(
                "RemoteObjectSet: can't access RemoteObject through " + rro, e);
        } catch (RenegotiateSessionException e) {
            e.printStackTrace();
            throw new RemoteRemoteObjectException(e);
        }
    }

    /**
     * Send a non-functional internal request to get the name of the remote ProActiveRuntime
     */
    private String getPARuntimeName(RemoteRemoteObject rro) throws RemoteRemoteObjectException {
        try {
            Request r = new PARuntimeNameRequest();
            Reply rep = rro.receiveMessage(r);
            return (String) rep.getResult().getResult();
        } catch (ProActiveException e) {
            throw new RemoteRemoteObjectException("RemoteObjectSet: can't get ProActiveRuntime urls from " +
                rro, e);
        } catch (IOException e) {
            throw new RemoteRemoteObjectException("RemoteObjectSet: can't get ProActiveRuntime urls from " +
                rro, e);
        } catch (RenegotiateSessionException e) {
            e.printStackTrace();
            throw new RemoteRemoteObjectException(e);
        }
    }

    /**
     * Exception thrown an communication error, internal use only
     */
    public class RemoteRemoteObjectException extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 500L;

        RemoteRemoteObjectException(Exception e) {
            super(e);
        }

        RemoteRemoteObjectException(String m) {
            super(m);
        }

        RemoteRemoteObjectException(String m, Exception e) {
            super(m, e);
        }
    }

    /**
     * The Object isn't already exposed with this protocol
     */
    public class NotYetExposedException extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 500L;

        public NotYetExposedException(Exception e) {
            super(e);
        }

        public NotYetExposedException(String m) {
            super(m);
        }

        public NotYetExposedException(String m, Exception e) {
            super(m, e);
        }
    }

    public int size() {
        return this.rros.size();
    }

    /**
     * Update the protocol order from the new ProActive Runtime
     * when the remote remote object is reified
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int size = in.readInt();
        this.rros = new LinkedHashMap<URI, RemoteRemoteObject>(size);
        ObjectInputStream ois = null;
        byte[] buf = null;

        for (int i = 0; i < size; i++) {
            try {
                URI uri = (URI) in.readObject();
                RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(uri.getScheme());
                buf = (byte[]) in.readObject();
                ois = rof.getProtocolObjectInputStream(new ByteArrayInputStream(buf));
                RemoteRemoteObject rro = (RemoteRemoteObject) ois.readObject();
                this.rros.put(uri, rro);
            } catch (UnknownProtocolException e) {
                ProActiveLogger.logImpossibleException(LOGGER_RO, e);
            } finally {
                if (ois != null)
                    ois.close();
            }
        }

        try {
            this.defaultURI = (URI) in.readObject();
            RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(this.defaultURI
                    .getScheme());
            buf = (byte[]) in.readObject();
            ois = rof.getProtocolObjectInputStream(new ByteArrayInputStream(buf));
            RemoteRemoteObject rro = (RemoteRemoteObject) ois.readObject();
            this.defaultRO = rro;
        } catch (UnknownProtocolException e) {
            ProActiveLogger.logImpossibleException(LOGGER_RO, e);
        } finally {
            if (ois != null)
                ois.close();
        }

        VMID testLocal = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        if (!vmid.equals(testLocal)) {
            this.vmid = testLocal;
            this.updateUnreliable();
            this.updateOrder();
        }
    }

    /**
     * Check if now some RemoteRemoteObject becomes accessible
     */
    private void updateUnreliable() {
        if (unreliables.size() != 0) {
            HashSet<RemoteRemoteObject> copy = new HashSet<RemoteRemoteObject>(this.unreliables);
            for (RemoteRemoteObject rro : copy) {
                this.unreliables.remove(rro);
                this.add(rro);
            }
        }
    }

    /**
     * Network topology could have change, change the order
     */
    private void updateOrder() {
        // The update of the order is done asynchronously, so we need to erase previous values
        // It's not a good idea to set order as transient, because of the local serialization case
        this.order = new String[0];
        if (rros.size() > 1)
            RemoteObjectBenchmark.getInstance().subscribeAsObserver(this, rros, this.remoteRuntimeName);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // Almost same as in term of speed UniqueID.getCurrentVMID() but more readable
        this.vmid = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        out.defaultWriteObject();
        out.writeInt(rros.size());
        ObjectOutputStream oos = null;
        for (URI uri : rros.keySet()) {
            try {
                out.writeObject(uri);
                RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(uri.getScheme());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                oos = rof.getProtocolObjectOutputStream(baos);
                oos.writeObject(rros.get(uri));
                oos.flush();
                out.writeObject(baos.toByteArray());
            } catch (UnknownProtocolException e) {
                ProActiveLogger.logImpossibleException(LOGGER_RO, e);
            } finally {
                if (oos != null)
                    oos.close();
            }
        }

        try {
            out.writeObject(this.defaultURI);
            RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(this.defaultURI
                    .getScheme());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = rof.getProtocolObjectOutputStream(baos);
            oos.writeObject(this.defaultRO);
            oos.flush();
            out.writeObject(baos.toByteArray());
        } catch (UnknownProtocolException e) {
            ProActiveLogger.logImpossibleException(LOGGER_RO, e);
        } finally {
            if (oos != null)
                oos.close();
        }

    }

    /**
     * Notification from a BenchmarkMonitorThread Object
     */
    public void update(Observable o, Object arg) {
        order = (String[]) arg;
        if (LOGGER_RO.isDebugEnabled())
            LOGGER_RO.debug("[Multi-Protocol] " + URIBuilder.getNameFromURI(defaultURI) +
                " received protocol order: " + Arrays.toString(order));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (URI uri : rros.keySet()) {
            sb.append(uri.toString());
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        sb.append("]");
        return sb.toString();
    }
}
