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
package org.objectweb.proactive.extensions.pnp;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.NotBoundException;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.converter.remote.ProActiveMarshalInputStream;
import org.objectweb.proactive.core.util.converter.remote.ProActiveMarshalOutputStream;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pnp.exception.PNPException;


/**
 * The real PNP remote object factory
 *
 *
 * @since ProActive 5.0.0
 */
public class PNPRemoteObjectFactoryBackend extends AbstractRemoteObjectFactory implements RemoteObjectFactory {
    static final Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP);

    final private String protoId;
    final private PNPAgent agent;
    final private PNPRegistry registry;
    /** Exception that should have been thrown by ctor.
     *
     * ROF ctor are not allowed to throw exception. To ease troubleshooting, we keep a
     * reference on the exception.
     */
    final private Exception ctorException;

    public PNPRemoteObjectFactoryBackend(String proto, PNPConfig config, PNPExtraHandlers extraHandlers) {
        PNPAgent agent = null;
        Exception exception = null;
        try {
            agent = new PNPAgent(config, extraHandlers);
        } catch (Exception e) {
            exception = e;
            logger.error("Failed to instanciate the PNP remote object factory", e);
        }
        this.agent = agent;
        this.ctorException = exception;
        this.registry = PNPRegistry.singleton;
        this.protoId = proto;
    }

    private void throwIfAgentIsNul(String msg) throws ProActiveException {
        if (this.agent == null) {
            throw new PNPException(msg + ". PNP not properly configured, agent is null (cause follows)",
                this.ctorException);
        }

        return;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject
     * (org.objectweb .proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        throwIfAgentIsNul("newRemoteObject call failed");

        return new PNPRemoteObject(target, null, agent);
    }

    /**
     * Registers an remote object into the registry
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
     * org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#register
     * (org.objectweb.proactive .core.remoteobject.RemoteObject, java.net.URI,
     * boolean)
     */
    public RemoteRemoteObject register(InternalRemoteRemoteObject ro, URI uri, boolean replacePrevious)
            throws ProActiveException {
        throwIfAgentIsNul("register call failed");

        this.registry.bind(URIBuilder.getNameFromURI(uri), ro, replacePrevious); // throw a ProActiveException if needed
        PNPRemoteObject rro = new PNPRemoteObject(ro, uri, agent);
        if (logger.isDebugEnabled()) {
            logger.debug("Registered remote object at endpoint " + uri);
        }
        return rro;
    }

    /**
     * Unregisters an remote object previously registered into the bodies table
     *
     * @param urn
     *            the urn under which the active object has been registered
     */
    public void unregister(URI uri) throws ProActiveException {
        this.registry.unbind(URIBuilder.getNameFromURI(uri));
    }

    /**
     * Looks-up a remote object previously registered in the bodies table .
     *
     * @param urn
     *            the urn (in fact its url + name) the remote Body is registered
     *            to
     * @return a UniversalBody
     */
    @SuppressWarnings("unchecked")
    public RemoteObject lookup(URI uri) throws ProActiveException {
        throwIfAgentIsNul("lookup call failed");

        String name = URIBuilder.getNameFromURI(uri);
        PNPROMessageLookup message = new PNPROMessageLookup(uri, name, agent);
        try {
            message.send();
            RemoteRemoteObject result = message.getReturnedObject();
            if (result == null) {
                throw new NotBoundException("The uri " + uri + " is not bound to any known object");
            } else {
                return new RemoteObjectAdapter(result);
            }
        } catch (IOException e) {
            throw new ProActiveException("Lookup of " + uri + "failed due to network error", e);
        }
    }

    /**
     * List all active object previously registered in the registry
     *
     * @param url
     *            the url of the host to scan, typically //machine_name
     * @return a list of Strings, representing the registered names, and {} if
     *         no registry
     * @exception java.io.IOException
     *                if scanning reported some problem (registry not found, or
     *                malformed Url)
     */

    /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.proactive.core.body.BodyAdapterImpl#list(java.lang.String)
     */
    public URI[] list(URI uri) throws ProActiveException {
        throwIfAgentIsNul("list call failed");

        PNPROMessageListRemoteObjectsMessage message = new PNPROMessageListRemoteObjectsMessage(uri, agent);
        try {
            message.send();
            String[] names = message.getReturnedObject();
            URI[] uris = new URI[names.length];

            for (int i = 0; i < names.length; i++) {
                uris[i] = URIBuilder.buildURI(uri, names[i]);
            }

            return uris;
        } catch (IOException e) {
            throw new ProActiveException("Listing registered remote objects on " + uri +
                " failed due to network error", e);
        }
    }

    public String getProtocolId() {
        return this.protoId;
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        // see PROACTIVE-419
    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name,
            boolean rebind) throws ProActiveException {
        throwIfAgentIsNul("createRemoteObject call failed");

        try {
            // Must be a fixed path
            if (!name.startsWith("/")) {
                name = "/" + name;
            }

            URI uri = new URI(this.getProtocolId(), null, URIBuilder.getHostNameorIP(this.agent
                    .getInetAddress()), this.agent.getPort(), name, null, null);

            // register the object on the register
            InternalRemoteRemoteObject irro = new InternalRemoteRemoteObjectImpl(remoteObject, uri);
            RemoteRemoteObject rmo = register(irro, uri, rebind);
            irro.setRemoteRemoteObject(rmo);

            return irro;
        } catch (URISyntaxException e) {
            throw new ProActiveException("Failed to create remote object " + name, e);
        }
    }

    public PNPAgent getAgent() {
        return this.agent;
    }

    public URI getBaseURI() {
        final URI uri;
        if (this.agent == null) {
            uri = URI.create(this.getProtocolId() + "://pnp-failed-to-initialize-invalid-url/");
        } else {
            uri = URI.create(this.getProtocolId() + "://" +
                URIBuilder.getHostNameorIP(this.agent.getInetAddress()) + ":" + this.agent.getPort() + "/");
        }

        return uri;
    }

    public int getPort() {
        return this.agent == null ? -1 : this.agent.getPort();
    }

    public ObjectInputStream getProtocolObjectInputStream(InputStream in) throws IOException {
        return new ProActiveMarshalInputStream(in);
    }

    public ObjectOutputStream getProtocolObjectOutputStream(OutputStream out) throws IOException {
        return new ProActiveMarshalOutputStream(out, ProActiveRuntimeImpl.getProActiveRuntime().getURL());
    }
}
