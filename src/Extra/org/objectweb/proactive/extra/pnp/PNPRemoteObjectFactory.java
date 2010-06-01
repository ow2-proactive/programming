/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.pnp;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.pnp.exception.PNPException;


/**
 * @since ProActive 4.3.0
 */
public class PNPRemoteObjectFactory extends AbstractRemoteObjectFactory implements RemoteObjectFactory {
    static final Logger logger = ProActiveLogger.getLogger(PNPConfig.Loggers.PNP);

    /** The protocol id of the factory */
    static final public String PROTOCOL_ID = "pnp";

    final private PNPAgent agent;
    final private PNPRegistry registry;

    public PNPRemoteObjectFactory() {

        PNPAgent agent = null;
        try {
            agent = new PNPAgent(PNPConfig.PA_PNP_PORT.getValue());
        } catch (Exception e) {
            logger.warn("Failed to instanciate the PNP remote object factory", e);
        }
        this.agent = agent;
        this.registry = PNPRegistry.singleton;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject
     * (org.objectweb .proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) {
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
        this.registry.bind(uri, ro, replacePrevious); // throw a ProActiveException if needed
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
        registry.unbind(uri);
    }

    /**
     * Looks-up a remote object previously registered in the bodies table .
     *
     * @param urn
     *            the urn (in fact its url + name) the remote Body is registered
     *            to
     * @return a UniversalBody
     */
    public RemoteObject lookup(URI uri) throws ProActiveException {
        PNPROMessageLookup message = new PNPROMessageLookup(uri, agent);
        try {
            message.send();
            RemoteRemoteObject result = message.getReturnedObject();
            if (result == null) {
                throw new ProActiveException("The uri " + uri + " is not bound to any known object");
            } else {
                return new RemoteObjectAdapter(result);
            }
        } catch (PNPException e) {
            throw new ProActiveException(e);
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
        PNPROMessageListRemoteObjectsMessage message = new PNPROMessageListRemoteObjectsMessage(uri, agent);
        try {
            message.send();
            return message.getReturnedObject();
        } catch (PNPException e) {
            throw new ProActiveException(e);
        }
    }

    public String getProtocolId() {
        return PROTOCOL_ID;
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        // see PROACTIVE-419
    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name,
            boolean rebind) throws ProActiveException {

        try {
            // Must be a fixed path
            if (!name.startsWith("/")) {
                name = "/" + name;
            }

            URI uri = new URI(this.getProtocolId(), null, this.agent.getInetAddress().getHostAddress(),
                this.agent.getPort(), name, null, null);

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
        return URI.create(this.getProtocolId() + "://" + this.agent.getInetAddress().getHostAddress() + ":" +
            this.agent.getPort() + "/");
    }

    public int getPort() {
        return this.agent.getPort();
    }
}
