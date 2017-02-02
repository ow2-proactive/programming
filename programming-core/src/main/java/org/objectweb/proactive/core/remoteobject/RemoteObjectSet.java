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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.ProtocolException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.remoteobject.benchmark.RemoteObjectBenchmark;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class RemoteObjectSet implements Serializable, Observer {

    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

    public static final int UNREACHABLE_VALUE = Integer.MIN_VALUE;

    public static final int NOCHANGE_VALUE = 0;

    private ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();

    /**
     * Protocol order received from the proactive.communication.protocols.order property
     */
    private static List<String> defaultProtocolOrder;

    /**
     * Order received when reading a stub or when creating the RemoteObjectSet locally.
     * The order will be used each time this RemoteObjectSet will serialized
     */
    private List<URI> initialorder;

    private static Method getURI;

    /**
     * Results of the benchmarks, or if no benchmark is done, stores the natural order & the fact that some protocols are
     * not reachable
     */
    private transient ConcurrentHashMap<URI, Integer> lastBenchmarkResults;

    // The following can be modified only atomically
    // *transient * Each RRO need a special marshalling processing
    /**
     * Map of all RRO, indexed by the rro uri
     */
    private transient HashMap<URI, RemoteRemoteObject> rros;

    /**
     * Sorted list of RRO uris, according to natural order, benchmark or reachability
     */
    private transient ArrayList<URI> sortedrros;

    /**
     * The default protocol of this remote object set
     */
    private transient RemoteRemoteObject defaultRO;

    private transient URI defaultURI = null;

    private String remoteRuntimeName;

    private VMID vmid = null;

    /**
     * A forced protocol, if any
     */
    private RemoteRemoteObject forcedProtocol = null;

    static {
        if (CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOLS_ORDER.isSet()) {
            defaultProtocolOrder = new ArrayList<String>(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOLS_ORDER.getValue());
        } else {
            defaultProtocolOrder = Collections.emptyList();
        }
        try {
            getURI = InternalRemoteRemoteObject.class.getDeclaredMethod("getURI", new Class<?>[0]);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public RemoteObjectSet(RemoteRemoteObject defaultRRO, Collection<RemoteRemoteObject> rros) throws IOException {
        this.rros = new LinkedHashMap<URI, RemoteRemoteObject>();

        try {
            this.defaultRO = defaultRRO;
            this.remoteRuntimeName = getPARuntimeName(defaultRO);
            this.defaultURI = getURI(defaultRO);
            this.rros.put(defaultURI, defaultRO);
            this.sortedrros = new ArrayList<URI>();
            this.sortedrros.add(defaultURI);
            this.initialorder = new ArrayList<URI>();
            this.initialorder.add(defaultURI);
            this.lastBenchmarkResults = new ConcurrentHashMap<URI, Integer>();
            for (RemoteRemoteObject rro : rros) {
                this.add(rro);
            }
            sortProtocolsInternal();
            if (LOGGER_RO.isDebugEnabled()) {
                LOGGER_RO.debug("[ROAdapter] created RemoteObjectSet : " + sortedrros);
            }

        } catch (RemoteRemoteObjectException e) {
            throw new IOException("[ROAdapter] Cannot access the remoteObject " + defaultRRO + " : " + e.getMessage());
        }
    }

    /**
     * Select the best suited RemoteRemoteObject (protocol related), and send it the Request
     * Fallback to default (according to the PA_COMMUNICATION_PROTOCOL property) if necessary
     */
    @SuppressWarnings("unchecked")
    public Reply receiveMessage(Request message) throws ProActiveException, IOException {
        if (forcedProtocol != null) {
            return forcedProtocol.receiveMessage(message);
        }
        RemoteRemoteObject rro = null;
        // the order is cloned to allow asynchronous updates by the benchmark threads
        ReentrantReadWriteLock.ReadLock rl = rwlock.readLock();

        rl.lock();
        ArrayList<URI> cloned = (ArrayList<URI>) sortedrros.clone();
        rl.unlock();
        // For each protocol already selected and sorted

        Throwable defaultProtocolException = null;

        boolean anyException = false;

        Reply reply = null;
        for (URI uri : cloned) {
            rro = rros.get(uri);
            if (LOGGER_RO.isDebugEnabled()) {
                LOGGER_RO.debug("[ROAdapter] Sending message " + message + " to " + uri);
            }
            try {
                reply = rro.receiveMessage(message);
                // These Exceptions happened on client side
                // RMI doesn't act as others protocols and Exceptions aren't
                // encapsulated, so they are caught here.
            } catch (ProtocolException pae) {
                anyException = true;
                defaultProtocolException = handleProtocolException(pae, uri, cloned.size() > 1);
            } catch (IOException io) {
                anyException = true;
                defaultProtocolException = handleProtocolException(io, uri, cloned.size() > 1);
            }

            if (reply != null) {
                // The Exception is thrown on server side
                // So it is encapsulated to be delivered on client side
                Throwable t = reply.getResult().getException();
                if (t != null && (t instanceof ProtocolException || t instanceof IOException)) {
                    anyException = true;
                    defaultProtocolException = handleProtocolException(t, uri, cloned.size() > 1);
                    continue;
                }
                break;
            }
        }

        // if we arrive to this point either a reply has been received or all protocols sent exceptions

        // if there has been any exception we sort the uri list before sending back the result
        if (anyException) {
            sortProtocolsInternal();
        }

        // In case all protocols led to Exception, simply throw the Exception sent by the default protocol
        if (reply == null && defaultProtocolException != null) {
            if (defaultProtocolException instanceof ProtocolException) {
                throw (ProtocolException) defaultProtocolException;
            } else if (defaultProtocolException instanceof IOException) {
                throw (IOException) defaultProtocolException;
            }
        }
        // otherwise, we received a reply
        return reply;
    }

    // Handles the Exceptions received in the receiveMessage method, doing a special treatment for the default protocol
    private Throwable handleProtocolException(Throwable e, URI uri, boolean multiProtocol) {
        if (!uri.equals(defaultURI)) {
            LOGGER_RO.warn("[ROAdapter] Disabling protocol " + uri.getScheme() + " because of received exception", e);
            lastBenchmarkResults.put(uri, UNREACHABLE_VALUE);
            return null;
        } else {
            if (multiProtocol) {
                LOGGER_RO.warn("[ROAdapter] Skipping default protocol " + uri.getScheme() +
                               " because of received exception", e);
            }
            lastBenchmarkResults.put(uri, UNREACHABLE_VALUE);
            return e;
        }
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
     * Sort the list of rro uris, using locks to prevent concurrent modification
     */
    private void sortProtocolsInternal() {
        ReentrantReadWriteLock.WriteLock wl = rwlock.writeLock();
        wl.lock();
        sortedrros = sortProtocols(rros.keySet(), defaultProtocolOrder, lastBenchmarkResults, defaultURI);
        wl.unlock();
    }

    /**
     * Helper method used to sort the list of protocols
     * @param input
     * @param defOrder
     * @param benchmarkRes
     * @param defUri
     * @return
     */
    public static ArrayList<URI> sortProtocols(Collection<URI> input, final List<String> defOrder,
            final ConcurrentHashMap<URI, Integer> benchmarkRes, final URI defUri) {

        ArrayList<URI> output = new ArrayList<URI>();
        output.addAll(input);

        Collections.sort(output, new Comparator<URI>() {
            @Override
            public int compare(URI o1, URI o2) {

                // unreachable uri, they are put at the end of the list
                if (benchmarkRes.containsKey(o1) && benchmarkRes.get(o1) == UNREACHABLE_VALUE) {
                    return 1;
                }
                if (benchmarkRes.containsKey(o2) && benchmarkRes.get(o2) == UNREACHABLE_VALUE) {
                    return -1;
                }

                // sort accordingly to fixed order
                if (defOrder.contains(o1.getScheme()) && defOrder.contains(o2.getScheme())) {
                    return defOrder.indexOf(o1.getScheme()) - defOrder.indexOf(o2.getScheme());
                }
                // the following code means that any protocol present in the default order
                // is preferred to any other protocol, currently this behavior is deactivated

                if (defOrder.contains(o1.getScheme())) {
                    return -1;
                }
                if (defOrder.contains(o2.getScheme())) {
                    return 1;
                }
                if (benchmarkRes.containsKey(o1) && benchmarkRes.containsKey(o2)) {
                    // sort accordingly to benchmark results
                    if (benchmarkRes.get(o1) > benchmarkRes.get(o2)) {
                        return -1;
                    } else if (benchmarkRes.get(o2) > benchmarkRes.get(o1)) {
                        return 1;
                    }
                    return 0;
                }

                // undetermined, we have no info
                return 0;
            }
        });

        // finally remove unreachable protocols
        for (ListIterator<URI> it = output.listIterator(output.size()); it.hasPrevious();) {
            URI reachableOrNot = it.previous();
            if (benchmarkRes.containsKey(reachableOrNot) && benchmarkRes.get(reachableOrNot) == UNREACHABLE_VALUE) {
                if (!reachableOrNot.equals(defUri)) {
                    it.remove();
                }
            } else {
                // we exit the loop at the first reachable protocol
                break;
            }
        }
        return output;
    }

    /**
     * Add a RemoteRemoteObject (protocol specific) to the RemoteObjectSet
     * If it is unreliable, keep it aside for later possible use
     */
    public void add(RemoteRemoteObject rro) {
        try {
            URI uri = getURI(rro);
            this.rros.put(uri, rro);
            this.sortedrros.add(uri);
            this.initialorder.add(uri);
        } catch (RemoteRemoteObjectException e) {
            LOGGER_RO.warn(e);
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
            MethodCall mc = MethodCall.getMethodCall(getURI, new Object[0], new HashMap<TypeVariable<?>, Class<?>>());
            Request r = new InternalRemoteRemoteObjectRequest(mc);
            Reply rep = rro.receiveMessage(r);
            return (URI) rep.getResult().getResult();
        } catch (ProActiveException e) {
            throw new RemoteRemoteObjectException("RemoteObjectSet: can't access RemoteObject through " + rro, e);
        } catch (IOException e) {
            throw new RemoteRemoteObjectException("RemoteObjectSet: can't access RemoteObject through " + rro, e);
        } catch (ProActiveRuntimeException e) {
            throw new RemoteRemoteObjectException("RemoteObjectSet: can't access RemoteObject through " + rro, e);
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
            throw new RemoteRemoteObjectException("RemoteObjectSet: can't get ProActiveRuntime urls from " + rro, e);
        } catch (IOException e) {
            throw new RemoteRemoteObjectException("RemoteObjectSet: can't get ProActiveRuntime urls from " + rro, e);
        }
    }

    /**
     * Exception thrown an communication error, internal use only
     */
    public class RemoteRemoteObjectException extends Exception {
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
     * Network topology could have change, change the order
     */
    private void startBenchmark() {
        // The update of the order is done asynchronously
        if (CentralPAPropertyRepository.PA_BENCHMARK_ACTIVATE.isTrue()) {
            if (rros.size() > 1)
                RemoteObjectBenchmark.getInstance()
                                     .subscribeAsObserver(this, rros, this.remoteRuntimeName, lastBenchmarkResults);
        }
    }

    /**
     * Update the protocol order from the new ProActive Runtime
     * when the remote remote object is reified
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int size = in.readInt();
        ReentrantReadWriteLock.WriteLock wl = rwlock.writeLock();
        wl.lock();
        this.rros = new LinkedHashMap<URI, RemoteRemoteObject>(size);
        this.sortedrros = new ArrayList<URI>();
        this.lastBenchmarkResults = new ConcurrentHashMap<URI, Integer>();

        // read protocols
        for (int i = 0; i < size; i++) {
            Map.Entry<URI, RemoteRemoteObject> entry = readProtocol(in);
            if (entry != null) {
                URI uri = entry.getKey();
                RemoteRemoteObject rro = entry.getValue();
                if (i == 0) {
                    // default protocol is the first one
                    this.defaultURI = uri;
                    this.defaultRO = rro;
                }
                this.rros.put(uri, rro);
                sortedrros.add(uri);
                lastBenchmarkResults.put(uri, size - i);
            }
        }
        wl.unlock();
        sortProtocolsInternal();

        if (LOGGER_RO.isDebugEnabled()) {
            LOGGER_RO.debug("[ROAdapter] read RemoteObjectSet " + sortedrros);
        }

        VMID testLocal = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        if (!vmid.equals(testLocal)) {
            this.vmid = testLocal;
            this.startBenchmark();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        // Almost same as in term of speed UniqueID.getCurrentVMID() but more readable
        this.vmid = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        out.defaultWriteObject();
        out.writeInt(rros.size());

        // write the default protocol
        writeProtocol(out, defaultURI, defaultRO);

        // write all other protocols
        for (URI uri : initialorder) {
            if (!uri.equals(defaultURI)) {
                writeProtocol(out, uri, rros.get(uri));
            }
        }
    }

    private Map.Entry<URI, RemoteRemoteObject> readProtocol(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;

        URI uri;
        RemoteRemoteObject rro;
        try {
            // Read the data before calling any method throwing an exception to avoid stream corruption
            uri = (URI) in.readObject();
            byte[] buf = (byte[]) in.readObject();
            if (buf != null) {
                RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(uri.getScheme());
                ois = rof.getProtocolObjectInputStream(new ByteArrayInputStream(buf));
                rro = (RemoteRemoteObject) ois.readObject();
            } else {
                LOGGER_RO.debug("Sender was unable to serialize RemoteRemoteObject for " + uri);
                return null;
            }
        } catch (UnknownProtocolException e) {
            LOGGER_RO.debug("Failed to instanciate a ROF when receiving a RemoteObjectset", e);
            return null;
        } finally {
            if (ois != null)
                ois.close();
        }
        return new AbstractMap.SimpleEntry<URI, RemoteRemoteObject>(uri, rro);
    }

    private void writeProtocol(ObjectOutputStream out, URI uri, RemoteRemoteObject rro) throws IOException {
        String scheme = uri.getScheme();
        byte[] buf = null;
        ObjectOutputStream oos = null;
        try {
            RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(scheme);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos = rof.getProtocolObjectOutputStream(baos);
            oos.writeObject(rro);
            oos.flush();
            buf = baos.toByteArray();
        } catch (UnknownProtocolException e) {
            LOGGER_RO.warn("[ROAdapter] Failed to serialize the RemoteRemoteObject for " + uri);
        } finally {
            if (oos != null)
                oos.close();
        }
        out.writeObject(uri);
        out.writeObject(buf); // null if serialization failed
    }

    /**
     * Notification from a BenchmarkMonitorThread Object
     */
    @SuppressWarnings("unchecked")
    public void update(Observable o, Object arg) {
        ReentrantReadWriteLock.WriteLock wl = rwlock.writeLock();
        wl.lock();

        lastBenchmarkResults.putAll((Map<URI, Integer>) arg);
        sortProtocolsInternal();

        if (LOGGER_RO.isDebugEnabled()) {
            LOGGER_RO.debug("[Multi-Protocol] " + URIBuilder.getNameFromURI(defaultURI) + " received protocol order: " +
                            sortedrros);
        }
        wl.unlock();
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
