/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject.rmi;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.rmi.RegistryHelper;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 *        remote object Factory for the RMI protocol
 */
public abstract class AbstractRmiRemoteObjectFactory extends AbstractRemoteObjectFactory implements
        RemoteObjectFactory {
    static final Logger LOGGER_RO = ProActiveLogger.getLogger(Loggers.REMOTEOBJECT);
    final private Class<? extends RmiRemoteObject> clRemoteObject;

    final protected String protocolIdentifier;
    protected static RegistryHelper registryHelper;

    static {

        createClassServer();
        createRegistry();
    }

    public AbstractRmiRemoteObjectFactory(String protocolIdentifier,
            Class<? extends RmiRemoteObject> clRemoteObject) {
        this.protocolIdentifier = protocolIdentifier;
        this.clRemoteObject = clRemoteObject;
    }

    /**
     *  create the RMI registry
     */
    private static synchronized void createRegistry() {
        if (registryHelper == null) {
            registryHelper = new RegistryHelper();
            try {
                registryHelper.initializeRegistry();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract Registry getRegistry(URI url) throws RemoteException;

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        try {
            Constructor<? extends RmiRemoteObject> c = clRemoteObject
                    .getConstructor(InternalRemoteRemoteObject.class);
            return c.newInstance(target);
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#list(java.net.URI)
     */
    public URI[] list(URI url) throws ProActiveException {
        try {
            Registry registry = getRegistry(url);
            String[] names = registry.list();

            if (names != null) {
                URI[] uris = new URI[names.length];
                for (int i = 0; i < names.length; i++) {
                    uris[i] = URIBuilder.setProtocol(URI.create(names[i]), protocolIdentifier);
                }
                return uris;
            }
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#register(org.objectweb.proactive.core.remoteobject.RemoteObject, java.net.URI, boolean)
     */
    public RemoteRemoteObject register(InternalRemoteRemoteObject target, URI url,
            boolean replacePreviousBinding) throws ProActiveException {
        RmiRemoteObject rro = null;
        try {
            Constructor<? extends RmiRemoteObject> c = clRemoteObject
                    .getConstructor(InternalRemoteRemoteObject.class);
            rro = c.newInstance(target);
        } catch (Exception e) {
            throw new ProActiveException(e);
        }

        Registry reg = null;
        try {
            reg = getRegistry(url);
        } catch (Exception e) {
            LOGGER_RO.debug("creating new rmiregistry on port : " + url.getPort());
            try {
                LocateRegistry.createRegistry(url.getPort());
            } catch (RemoteException e1) {
                LOGGER_RO.warn("damn cannot start a rmiregistry on port " + url.getPort());
                throw new ProActiveException(e1);
            }
        }

        try {
            if (replacePreviousBinding) {
                reg.rebind(url.getPath().toString().substring(1), rro);
            } else {
                reg.bind(url.getPath().toString().substring(1), rro);
            }
            LOGGER_RO.debug(" successfully bound in registry at " + url);
        } catch (java.rmi.AlreadyBoundException e) {
            LOGGER_RO.warn(url + " already bound in registry", e);
            throw new ProActiveException(e);
        } catch (RemoteException e) {
            LOGGER_RO.debug(" cannot bind object at " + url);
            throw new ProActiveException(e);
        }
        return rro;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#unregister(java.net.URI)
     */
    public void unregister(URI url) throws ProActiveException {
        try {
            Registry reg = getRegistry(url);
            reg.unbind(URIBuilder.removeProtocol(url).toString());
            LOGGER_RO.debug(url + " unbound in registry");
        } catch (IOException e) {
            //No need to throw an exception if an object is already unregistered
            LOGGER_RO.warn(url + " is not bound in the registry ");
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#lookup(java.net.URI)
     */
    public <T> RemoteObject<T> lookup(URI uri) throws ProActiveException {
        Object o = null;

        URI modifiedURI = uri;
        if (uri.getPort() == -1) {
            LOGGER_RO.debug("No port specified, using the default one");
            modifiedURI = URIBuilder.buildURI(URIBuilder.getHostNameFromUrl(uri), URIBuilder
                    .getNameFromURI(uri));
            modifiedURI = RemoteObjectHelper.expandURI(modifiedURI);
        }

        // Try if URL is the address of a RmiRemoteBody
        try {
            Registry reg = getRegistry(uri);
            o = reg.lookup(modifiedURI.getPath().toString().substring(1));
            LOGGER_RO.debug(modifiedURI.toString() + " looked up successfully");
        } catch (java.rmi.NotBoundException e) {
            // there are one rmiregistry on target computer but nothing bound to this url isn t bound
            throw new ProActiveException("The url " + modifiedURI + " is not bound to any known object", e);
        } catch (RemoteException e) {
            throw new ProActiveException("Registry could not be contacted, " + modifiedURI, e);
        }

        if (o instanceof RmiRemoteObject) {
            return new RemoteObjectAdapter((RmiRemoteObject) o);
        }

        throw new ProActiveException("The given url does exist but doesn't point to a remote object  url=" +
            modifiedURI + " class found is " + o.getClass().getName());
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#getPort()
     */
    public int getPort() {
        return Integer.parseInt(PAProperties.PA_RMI_PORT.getValue());
    }

    public String getProtocolId() {
        return this.protocolIdentifier;
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        if (rro instanceof RmiRemoteObject) {
            try {
                UnicastRemoteObject.unexportObject((RmiRemoteObject) rro, true);
            } catch (NoSuchObjectException e) {
                throw new ProActiveException(e);
            }
        } else {
            throw new ProActiveException("the remote object is not a rmi remote object");
        }

    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name)
            throws ProActiveException {
        URI uri = URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(), name, this.getProtocolId());
        // register the object on the register
        InternalRemoteRemoteObject irro = new InternalRemoteRemoteObjectImpl(remoteObject, uri);
        RemoteRemoteObject rmo = register(irro, uri, true);
        irro.setRemoteRemoteObject(rmo);

        return irro;
    }

    public URI getBaseURI() {
        return URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(), "", getProtocolId());
    }

}
