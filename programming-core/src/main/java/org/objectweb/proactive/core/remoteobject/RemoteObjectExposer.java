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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.ProtocolException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 * The RemoteObjectExposer is in charge of exposing an object as a remote object.
 * It allows the exposition of the object it represents on one or multiple protocols, keeps
 * references on already activated protocols, allows to unregister and unexport one or more protocols.
 */
public class RemoteObjectExposer<T> {
    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);

    // Use LinkedHashMap for keeping the insertion-order
    protected LinkedHashMap<URI, InternalRemoteRemoteObject> activeRemoteRemoteObjects;

    private String className;

    private RemoteObjectImpl<T> remoteObject;

    public RemoteObjectExposer() {
    }

    public RemoteObjectExposer(String className, T target) {
        this(new UniqueID(className + "_").shortString(), className, target, null);
    }

    public RemoteObjectExposer(String className, Object target, Class<? extends Adapter<T>> targetRemoteObjectAdapter) {
        this(new UniqueID(className + "_").shortString(), className, target, targetRemoteObjectAdapter);
    }

    /**
     *
     * @param className
     *            the classname of the stub for the remote object
     * @param target
     *            the object to turn into a remote object
     * @param targetRemoteObjectAdapter
     *            the adapter object that allows to implement specific behaviour
     *            like cache mechanism
     */
    @SuppressWarnings("unchecked")
    public RemoteObjectExposer(String name, String className, Object target,
            Class<? extends Adapter<T>> targetRemoteObjectAdapter) {
        this.className = className;
        this.remoteObject = new RemoteObjectImpl(name, className, target, targetRemoteObjectAdapter);
        this.remoteObject.setRemoteObjectExposer(this);

        this.activeRemoteRemoteObjects = new LinkedHashMap<URI, InternalRemoteRemoteObject>();
    }

    /**
     * activate and register the remote object on the given url
     *
     * @param url
     *            The URI where to register the remote object
     * @return a remote reference to the remote object ie a RemoteRemoteObject
     * @throws UnknownProtocolException
     *             thrown if the protocol specified within the url is unknow
     */
    public synchronized RemoteRemoteObject createRemoteObject(URI url) throws ProActiveException {
        String protocol = null;
        // check if the url contains a scheme (protocol)
        if (!url.isAbsolute()) {
            // if not expand it
            url = RemoteObjectHelper.expandURI(url);
        }

        protocol = url.getScheme();

        // select the factory matching the required protocol
        RemoteObjectFactory rof = RemoteObjectHelper.getRemoteObjectFactory(protocol);

        try {
            int port = url.getPort();
            if (port == -1) {
                try {
                    url = new URI(url.getScheme(),
                                  url.getUserInfo(),
                                  url.getHost(),
                                  rof.getPort(),
                                  url.getPath(),
                                  url.getQuery(),
                                  url.getFragment());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            // register the object on the register
            InternalRemoteRemoteObject irro = new InternalRemoteRemoteObjectImpl(this.remoteObject, url);
            RemoteRemoteObject rmo = rof.register(irro, url, true);
            irro.setRemoteRemoteObject(rmo);

            // put the url within the list of the activated protocols
            this.activeRemoteRemoteObjects.put(url, irro);
            return rmo;
        } catch (Exception e) {
            throw new ProActiveException("Failed to create remote object (url=" + url + ")", e);
        }
    }

    /**
     * By default, expose remote object with all available protocols specified by the property
     * PA_COMMUNICATION_ADDITIONAL_PROTOCOLS
     */
    public synchronized RemoteRemoteObject createRemoteObject(String name, boolean rebind) throws ProActiveException {
        // Must be created before additionals one in order to avoid an AlreadyBoundException
        RemoteRemoteObject ret = createRemoteObject(name,
                                                    rebind,
                                                    CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
        if (LOGGER_RO.isDebugEnabled()) {
            LOGGER_RO.debug("[ROExposer] Object \"" + name + "\" exposed with default protocol : " +
                            CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
        }
        multiExposeRemoteObject(name, rebind);
        return ret;
    }

    /**
     * Only expose remote object as the specified protocol
     *
     * @param name
     *           Specify a remote object name
     *
     * @throws ProActiveException
     */
    public synchronized RemoteRemoteObject createRemoteObject(String name, boolean rebind, String protocol)
            throws ProActiveException {
        try {
            // select the factory matching the required protocol
            // here is an implicit check for protocol validity
            RemoteObjectFactory rof = AbstractRemoteObjectFactory.getRemoteObjectFactory(protocol);
            URI uri = URIBuilder.buildURI(rof.getBaseURI().getUserInfo(),
                                          rof.getBaseURI().getHost(),
                                          name,
                                          rof.getProtocolId(),
                                          rof.getPort(),
                                          null);
            InternalRemoteRemoteObject irro = activeRemoteRemoteObjects.get(uri);
            if (irro == null) {
                irro = rof.createRemoteObject(this.remoteObject, name, rebind);
                this.activeRemoteRemoteObjects.put(irro.getURI(), irro);
                // Expose the remote object using all specified communication protocol
            }
            return irro.getRemoteRemoteObject();
        } catch (ProtocolException e) {
            throw new ProtocolException("Failed to create remote object (name=" + name + ")", e);
        } catch (ProActiveException e) {
            throw new ProActiveException("Failed to create remote object (name=" + name + ")", e);
        } catch (IOException e) {
            throw new ProActiveException("Failed to create remote object (name=" + name + ")", e);
        }
    }

    /**
     * Expose the remoteObject through all the protocol specified in PA_COMMUNICATION_ADDITIONAL_PROTOCOLS
     *
     * If some Exception are thrown during this additionnal exposure step, there are ignored.
     */
    private synchronized void multiExposeRemoteObject(String name, boolean rebind) throws ProActiveException {
        // Expose the remote object using all specified communication protocol
        if (CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.isSet()) {
            List<String> protocols = CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.getValue();
            Iterator<String> it = protocols.iterator();
            while (it.hasNext()) {
                String protocol = it.next();
                if (protocol.length() > 0) {
                    try {
                        // Create and store RRO in hashtable
                        createRemoteObject(name, true, protocol);
                        if (LOGGER_RO.isDebugEnabled()) {
                            LOGGER_RO.debug("[ROExposer] Object \"" + name + "\" exposed with additionnal protocol : " +
                                            protocol);
                        }
                    } catch (ProActiveRuntimeException pare) {
                        // Not so beautiful
                        if (protocol.equalsIgnoreCase("pamr")) {
                            LOGGER_RO.warn("[ROExposer] The PAMR router seems to be down", pare);
                        } else {
                            LOGGER_RO.warn("[ROExposer] The exposition of " + name + ", through the protocol " +
                                           protocol + " didn't succeed", pare);
                        }
                    } catch (ProtocolException pae) {
                        if (pae.getCause() instanceof AlreadyBoundException) {
                            // Do nothing, here, we try to expose the object with extra protocol,
                            // error here won't cause trouble
                            continue;
                        }
                        // this protocol throw exception, so we remove it from the candidate list for multi exposure
                        LOGGER_RO.warn("[ROExposer] Protocol " + protocol +
                                       " seems invalid for this runtime, this is not a critical error, the protocol will be disabled.",
                                       pae);

                        // Remove the protocol
                        it.remove();
                    }
                }
            }
            CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.setValue(protocols);
        }
    }

    /**
     * This method cleans the remote object at the given url (unexport if applicable and unregister it)
     */
    private void cleanRemoteObject(URI url) throws ProActiveException {
        InternalRemoteRemoteObject irro = this.activeRemoteRemoteObjects.remove(url);
        if (irro == null) {
            throw new IllegalArgumentException("Unknown remote object located at " + url);
        }
        RemoteRemoteObject rro = irro.getRemoteRemoteObject();
        RemoteObjectFactory rof = RemoteObjectHelper.getRemoteObjectFactory(url.getScheme());
        try {
            // unexport rro object (mostly rmi case - see PROACTIVE-419)
            rof.unexport(rro);
        } catch (Exception e) {
            LOGGER_RO.debug("", e);
        }
        try {
            // unregister (--> unbind) related rro
            rof.unregister(url);
        } catch (Exception e) {
            LOGGER_RO.debug("", e);
        }
    }

    /**
     * see PROACTIVE-1234
     * This method disable a specific protocol from a Remote Object
     * by applying the cleanRemoteObject method call to any end point, whose URL starts with that protocol
     */
    public void disableProtocol(String protocol) throws ProActiveException {
        ArrayList<URI> toBeCleaned = new ArrayList<URI>();
        for (URI uri : this.activeRemoteRemoteObjects.keySet()) {
            if (protocol.equals(uri.getScheme())) {
                toBeCleaned.add(uri);
            }
        }

        for (URI uri : toBeCleaned) {
            cleanRemoteObject(uri);
        }
    }

    /**
     *
     * @param protocol
     * @return return the reference on the remote object targeted by the protocol
     */
    @SuppressWarnings("unchecked")
    public RemoteObject<T> getRemoteObject(String protocol) throws ProActiveException {
        for (Iterator<URI> it = this.activeRemoteRemoteObjects.keySet().iterator(); it.hasNext();) {
            URI url = it.next();
            if (protocol.equals(url.getScheme())) {
                return new RemoteObjectAdapter(this.activeRemoteRemoteObjects.get(url).getRemoteRemoteObject());
            }
        }
        return null;
    }

    public RemoteObjectSet getRemoteObjectSet(RemoteRemoteObject ro) throws IOException {
        ArrayList<RemoteRemoteObject> al = new ArrayList<RemoteRemoteObject>();
        for (Map.Entry<URI, InternalRemoteRemoteObject> entry : activeRemoteRemoteObjects.entrySet()) {
            RemoteRemoteObject rro = entry.getValue().getRemoteRemoteObject();
            al.add(rro);
        }
        return new RemoteObjectSet(ro, al);
    }

    /**
     * @return return the activated urls
     */
    public String[] getURLs() {
        String[] urls = new String[this.activeRemoteRemoteObjects.size()];
        int i = 0;
        for (Iterator<URI> it = this.activeRemoteRemoteObjects.keySet().iterator(); it.hasNext();) {
            urls[i++] = it.next().toString();
        }
        return urls;
    }

    public String getURL(String protocol) {
        for (Iterator<URI> it = this.activeRemoteRemoteObjects.keySet().iterator(); it.hasNext();) {
            URI url = it.next();
            if (protocol.equalsIgnoreCase(url.getScheme())) {
                return url.toString();
            }
        }

        return null;
    }

    public String getURL() {
        return getURL(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
    }

    /**
     * unregister all the remote references on the remote object.
     */
    public void unregisterAll() throws ProActiveException {
        // Keep a reference for debug
        @SuppressWarnings("unchecked")
        LinkedHashMap<URI, InternalRemoteRemoteObject> cloned = (LinkedHashMap<URI, InternalRemoteRemoteObject>) this.activeRemoteRemoteObjects.clone();

        for (URI uri : cloned.keySet()) {
            try {
                PARemoteObject.unregister(uri);
            } catch (ProActiveException e) {
                LOGGER_RO.debug("Error when trying to unregister " + uri, e);
            }
        }
    }

    /**
     * @return return the remote object
     */
    public RemoteObjectImpl<T> getRemoteObject() {
        return this.remoteObject;
    }

    public void unexport(URI url) throws ProActiveException {
        RemoteRemoteObject rro = this.activeRemoteRemoteObjects.get(url).getRemoteRemoteObject();
        RemoteObjectFactory rof = RemoteObjectHelper.getRemoteObjectFactory(url.getScheme());
        rof.unexport(rro);
    }

    public void unexportAll() throws ProActiveException {
        URI uri = null;
        for (Iterator<URI> it = this.activeRemoteRemoteObjects.keySet().iterator(); it.hasNext();) {
            uri = it.next();
            RemoteRemoteObject rro = this.activeRemoteRemoteObjects.get(uri).getRemoteRemoteObject();
            RemoteObjectFactory rof = RemoteObjectHelper.getRemoteObjectFactory(uri.getScheme());
            try {
                rof.unexport(rro);
            } catch (Exception e) {
                LOGGER_RO.debug("", e);
            }
        }
    }
}
