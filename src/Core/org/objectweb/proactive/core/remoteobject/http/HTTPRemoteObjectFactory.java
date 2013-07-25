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
package org.objectweb.proactive.core.remoteobject.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.mop.PAObjectInputStream;
import org.objectweb.proactive.core.mop.PAObjectOutputStream;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.NotBoundException;
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
            url = URI.create(u.toString());
        } catch (MalformedURLException e) {
            url = URI.create(HTTPTransportServlet.get().getURL() + url.toString());
        }

        HTTPRegistry.getInstance().bind(URIBuilder.getNameFromURI(url), ro, replacePrevious); // Can throw a ProActiveException

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
        HTTPRegistry.getInstance().unbind(URIBuilder.getNameFromURI(urn));
    }

    /**
     * Looks-up a remote object previously registered in the bodies table .
     *
     * @param urn
     *            the urn (in fact its url + name) the remote Body is registered to
     * @return a UniversalBody
     */
    public RemoteObject lookup(URI url) throws ProActiveException {

        HttpRemoteObjectLookupMessage message = new HttpRemoteObjectLookupMessage(url.toString());
        try {
            message.send();
        } catch (HTTPRemoteException e) {
            throw new ProActiveException(e);
        }
        RemoteRemoteObject result = message.getReturnedObject();

        if (result == null) {
            throw new NotBoundException("The url " + url + " is not bound to any known object");
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

            String[] names = req.getReturnedObject();

            URI[] uris = new URI[names.length];

            for (int i = 0; i < names.length; i++) {
                uris[i] = URIBuilder.buildURI(url, names[i]);
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
        return CentralPAPropertyRepository.PA_XMLHTTP_PORT.getValue();
    }

    public String getProtocolId() {
        return this.protocolIdentifier;
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        // see PROACTIVE-419
    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject remoteObject, String name,
            boolean rebind) throws ProActiveException {
        URI uri = URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(), name, this.getProtocolId());

        // register the object on the register
        InternalRemoteRemoteObject irro = new InternalRemoteRemoteObjectImpl(remoteObject, uri);
        RemoteRemoteObject rmo = register(irro, uri, rebind);
        irro.setRemoteRemoteObject(rmo);

        return irro;
    }

    public URI getBaseURI() {
        return URI.create(this.getProtocolId() + "://" + ProActiveInet.getInstance().getHostname() + ":" +
            getPort() + "/");
    }

    public ObjectInputStream getProtocolObjectInputStream(InputStream in) throws IOException {
        return new PAObjectInputStream(in);
    }

    public ObjectOutputStream getProtocolObjectOutputStream(OutputStream out) throws IOException {
        return new PAObjectOutputStream(out);
    }
}
