/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.remoteobject.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.http.message.HttpRegistryListRemoteObjectsMessage;
import org.objectweb.proactive.core.remoteobject.http.message.HttpRemoteObjectLookupMessage;
import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.exceptions.HTTPRemoteException;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class HTTPRemoteObjectFactory extends AbstractRemoteObjectFactory implements RemoteObjectFactory {

    protected String protocolIdentifier = Constants.XMLHTTP_PROTOCOL_IDENTIFIER;

    static {
        createClassServer();
    }

    protected static synchronized void createClassServer() {
        HTTPTransportServlet.get();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject(org.objectweb
     * .proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        try {
            return new HttpRemoteObjectImpl(target, null);
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
    }

    /**
     * Registers an remote object into the http registry
     * 
     * @param urn
     *            The urn of the body (in fact his url + his name)
     * @exception java.io.IOException
     *                if the remote body cannot be registered
     */

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#register(org.objectweb.proactive
     * .core.remoteobject.RemoteObject, java.net.URI, boolean)
     */
    public RemoteRemoteObject register(InternalRemoteRemoteObject ro, URI url, boolean replacePrevious)
            throws ProActiveException {
        URL u = null;

        try {
            u = new URL(url.toString());
            int port = u.getPort();
            url = URI.create(u.toString());
        } catch (MalformedURLException e) {
            url = URI.create(HTTPTransportServlet.get().getURL() + url.toString());
        }

        HTTPRegistry.getInstance().bind(url.toString(), ro);

        HttpRemoteObjectImpl rro = new HttpRemoteObjectImpl(ro, url);

        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT)
                .debug("registering remote object  at endpoint " + url);
        return rro;
    }

    /**
     * Unregisters an remote object previously registered into the bodies table
     * 
     * @param urn
     *            the urn under which the active object has been registered
     */
    public void unregister(URI urn) throws ProActiveException {
        HTTPRegistry.getInstance().unbind(urn.toString());
    }

    /**
     * Looks-up a remote object previously registered in the bodies table .
     * 
     * @param urn
     *            the urn (in fact its url + name) the remote Body is registered to
     * @return a UniversalBody
     */
    public RemoteObject lookup(URI url) throws ProActiveException {
        int port = url.getPort();

        if (port == -1) {
            port = PAProperties.PA_XMLHTTP_PORT.getValueAsInt();
        }

        String urn = url.getPath();
        HttpRemoteObjectLookupMessage message = new HttpRemoteObjectLookupMessage(urn, url, port);
        try {
            message.send();
        } catch (HTTPRemoteException e) {
            throw new ProActiveException(e);
        }
        RemoteRemoteObject result = message.getReturnedObject();

        if (result == null) {
            throw new ProActiveException("The url " + url + " is not bound to any known object");
        } else {
            return new RemoteObjectAdapter(result);
        }
    }

    /**
     * List all active object previously registered in the registry
     * 
     * @param url
     *            the url of the host to scan, typically //machine_name
     * @return a list of Strings, representing the registered names, and {} if no registry
     * @exception java.io.IOException
     *                if scanning reported some problem (registry not found, or malformed Url)
     */

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.core.body.BodyAdapterImpl#list(java.lang.String)
     */
    public URI[] list(URI url) throws ProActiveException {

        HttpRegistryListRemoteObjectsMessage req = new HttpRegistryListRemoteObjectsMessage(url);

        try {
            req.send();

            String[] tmpUrl = req.getReturnedObject();

            URI[] uris = new URI[tmpUrl.length];

            for (int i = 0; i < tmpUrl.length; i++) {
                uris[i] = URI.create(tmpUrl[i]);
            }

            return uris;
        } catch (HTTPRemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#getPort()
     */
    public int getPort() {
        return PAProperties.PA_XMLHTTP_PORT.getValueAsInt();
    }

    public String getProtocolId() {
        return this.protocolIdentifier;
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        // see PROACTIVE-419
    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject remoteObject, String name)
            throws ProActiveException {
        URI uri = URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(), name, this.getProtocolId());

        // register the object on the register
        InternalRemoteRemoteObject irro = new InternalRemoteRemoteObjectImpl(remoteObject, uri);
        RemoteRemoteObject rmo = register(irro, uri, true);
        irro.setRemoteRemoteObject(rmo);

        return irro;
    }

    public URI getBaseURI() {
        return URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(), "",
                Constants.XMLHTTP_PROTOCOL_IDENTIFIER);
    }

}
