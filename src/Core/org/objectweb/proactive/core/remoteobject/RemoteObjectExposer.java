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
package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 * The RemoteObjectExposer is in charge of exposing an object as a remote object.
 * It allows the exposition of the object it represents on one or multiple protocols, keeps
 * references on already activated protocols, allows to unregister and unexport one or more protocols.
 */
public class RemoteObjectExposer<T> implements Serializable {
    protected Hashtable<URI, InternalRemoteRemoteObject> activeRemoteRemoteObjects;
    private String className;
    private RemoteObjectImpl<T> remoteObject;

    public RemoteObjectExposer() {
    }

    public RemoteObjectExposer(String className, T target) {
        this(className, target, null);
    }

    /**
     *
     * @param className the classname of the stub for the remote object
     * @param target the object to turn into a remote object
     * @param targetRemoteObjectAdapter the adapter object that allows to implement specific behaviour like cache mechanism
     */
    public RemoteObjectExposer(String className, Object target,
            Class<? extends Adapter<T>> targetRemoteObjectAdapter) {
        this.className = className;
        try {
            this.remoteObject = new RemoteObjectImpl(className, target, targetRemoteObjectAdapter);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.activeRemoteRemoteObjects = new Hashtable<URI, InternalRemoteRemoteObject>();
    }

    /**
     * activate and register the remote object on the given url
     * @param url The URI where to register the remote object
     * @return a remote reference to the remote object ie a RemoteRemoteObject
     * @throws UnknownProtocolException thrown if the protocol specified within the url is unknow
     */
    public synchronized RemoteRemoteObject createRemoteObject(URI url) throws UnknownProtocolException {
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
                    url = new URI(url.getScheme(), url.getUserInfo(), url.getHost(), RemoteObjectHelper
                            .getDefaultPortForProtocol(protocol), url.getPath(), url.getQuery(), url
                            .getFragment());
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
        } catch (ProActiveException e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).warn(
                    "unable to activate a remote object at endpoint " + url.toString(), e);

            e.printStackTrace();
            return null;
        }
    }

    public synchronized RemoteRemoteObject createRemoteObject(String name) {
        try {
            // select the factory matching the required protocol
            RemoteObjectFactory rof = AbstractRemoteObjectFactory.getDefaultRemoteObjectFactory();

            InternalRemoteRemoteObject irro = rof.createRemoteObject(this.remoteObject, name);
            this.activeRemoteRemoteObjects.put(irro.getURI(), irro);
            return irro.getRemoteRemoteObject();
        } catch (Exception e) {
            ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).warn(
                    "unable to activate a remote object at endpoint " + name, e);

            return null;
        }
    }

    /**
     *
     * @param protocol
     * @return return the reference on the remote object targeted by the protocol
     */
    @SuppressWarnings("unchecked")
    public RemoteObject<T> getRemoteObject(String protocol) throws ProActiveException {
        Enumeration<URI> e = this.activeRemoteRemoteObjects.keys();

        while (e.hasMoreElements()) {
            URI url = e.nextElement();
            if (protocol.equals(url.getScheme())) {
                return new RemoteObjectAdapter(this.activeRemoteRemoteObjects.get(url)
                        .getRemoteRemoteObject());
            }
        }

        return null;
    }

    /**
     * @return return the activated urls
     */
    public String[] getURLs() {
        String[] urls = new String[this.activeRemoteRemoteObjects.size()];

        Enumeration<URI> e = this.activeRemoteRemoteObjects.keys();
        int i = 0;
        while (e.hasMoreElements()) {
            urls[i] = e.nextElement().toString();
            i++;
        }

        return urls;
    }

    public String getURL(String protocol) {
        Enumeration<URI> e = this.activeRemoteRemoteObjects.keys();

        while (e.hasMoreElements()) {
            URI url = e.nextElement();
            if (protocol.equals(url.getScheme())) {
                return url.toString();
            }
        }

        return null;
    }

    public String getURL() {
        return getURL(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue());
    }

    /**
     * unregister all the remote references on the remote object.
     */
    public void unregisterAll() throws ProActiveException {
        Enumeration<URI> uris = this.activeRemoteRemoteObjects.keys();
        URI uri = null;
        while (uris.hasMoreElements()) {
            uri = uris.nextElement();
            //RemoteRemoteObject rro = this.activatedProtocols.get(uri);
            try {
                PARemoteObject.unregister(uri);
            } catch (ProActiveException e) {
                ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).info(
                        "Could not unregister " + uri + ". Error message: " + e.getMessage());
                throw e;
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
        Enumeration<URI> uris = this.activeRemoteRemoteObjects.keys();
        URI uri = null;
        while (uris.hasMoreElements()) {
            uri = uris.nextElement();
            RemoteRemoteObject rro = this.activeRemoteRemoteObjects.get(uri).getRemoteRemoteObject();
            RemoteObjectFactory rof = RemoteObjectHelper.getRemoteObjectFactory(uri.getScheme());
            rof.unexport(rro);
        }

    }

}
