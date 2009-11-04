/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.remoteobject;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.client.Agent;
import org.objectweb.proactive.extra.messagerouting.client.AgentImpl;
import org.objectweb.proactive.extra.messagerouting.client.ProActiveMessageHandler;
import org.objectweb.proactive.extra.messagerouting.exceptions.MessageRoutingException;
import org.objectweb.proactive.extra.messagerouting.remoteobject.message.MessageRoutingRegistryListRemoteObjectsMessage;
import org.objectweb.proactive.extra.messagerouting.remoteobject.message.MessageRoutingRemoteObjectLookupMessage;
import org.objectweb.proactive.extra.messagerouting.remoteobject.util.MessageRoutingRegistry;
import org.objectweb.proactive.extra.messagerouting.remoteobject.util.socketfactory.MessageRoutingSocketFactorySelector;
import org.objectweb.proactive.extra.messagerouting.router.RouterImpl;


/**
 * 
 * @since ProActive 4.1.0
 */
public class MessageRoutingRemoteObjectFactory extends AbstractRemoteObjectFactory implements
        RemoteObjectFactory {
    static final Logger logger = ProActiveLogger.getLogger(Loggers.FORWARDING_REMOTE_OBJECT);

    /** The protocol id of the facotry */
    static final public String PROTOCOL_ID = "pamr";

    final private Agent agent;
    final private MessageRoutingRegistry registry;

    public MessageRoutingRemoteObjectFactory() {
        // Start the agent and contact the router
        // Since there is no initialization phase in ProActive, if the router cannot be contacted
        // we log the error and throw a runtime exception. We cannot do better here
        String routerAddressStr = PAProperties.PA_NET_ROUTER_ADDRESS.getValue();
        if (routerAddressStr == null) {
            logAndThrowException("Message routing cannot be started because " +
                PAProperties.PA_NET_ROUTER_ADDRESS.getKey() + " is not set.");
        }

        int routerPort;
        if (PAProperties.PA_NET_ROUTER_PORT.isSet()) {
            routerPort = PAProperties.PA_NET_ROUTER_PORT.getValueAsInt();
            if (routerPort <= 0 || routerPort > 65535) {
                logAndThrowException("Invalid  router port value: " + routerPort);
            }
        } else {
            routerPort = RouterImpl.DEFAULT_PORT;
            logger.debug(PAProperties.PA_NET_ROUTER_PORT.getKey() + " not set. Using the default port: " +
                routerPort);
        }

        InetAddress routerAddress = null;
        try {
            routerAddress = InetAddress.getByName(routerAddressStr);
        } catch (UnknownHostException e) {
            logAndThrowException("Router address, " + routerAddressStr + " cannot be resolved", e);
        }

        Agent agent = null;
        try {
            agent = new AgentImpl(routerAddress, routerPort, ProActiveMessageHandler.class,
                MessageRoutingSocketFactorySelector.get());
        } catch (ProActiveException e) {
            logAndThrowException("Failed to create the local agent", e);
        }

        this.agent = agent;
        this.registry = MessageRoutingRegistry.singleton;
    }

    private void logAndThrowException(String message) {
        ProActiveRuntimeException exception;

        exception = new ProActiveRuntimeException(message);
        logger.fatal("Failed to initialize" + this.getClass().getName(), exception);
        throw exception;
    }

    private void logAndThrowException(String message, Exception e) {
        ProActiveRuntimeException exception;

        exception = new ProActiveRuntimeException(message, e);
        logger.fatal("Failed to initialize" + this.getClass().getName(), exception);
        throw exception;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject
     * (org.objectweb .proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) {
        return new MessageRoutingRemoteObject(target, null, agent);
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
        MessageRoutingRemoteObject rro = new MessageRoutingRemoteObject(ro, uri, agent);
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
        MessageRoutingRemoteObjectLookupMessage message = new MessageRoutingRemoteObjectLookupMessage(uri,
            agent);
        try {
            message.send();
            RemoteRemoteObject result = message.getReturnedObject();
            if (result == null) {
                throw new ProActiveException("The uri " + uri + " is not bound to any known object");
            } else {
                return new RemoteObjectAdapter(result);
            }
        } catch (MessageRoutingException e) {
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
        MessageRoutingRegistryListRemoteObjectsMessage message = new MessageRoutingRegistryListRemoteObjectsMessage(
            uri, agent);
        try {
            message.send();
            return message.getReturnedObject();
        } catch (MessageRoutingException e) {
            throw new ProActiveException(e);
        }
    }

    public String getProtocolId() {
        return "pamr";
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        // see PROACTIVE-419
    }

    public int getPort() {
        // Reverse connections are used with message routing so this method is
        // irrelevant
        return -1;
    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name,
            boolean rebind) throws ProActiveException {

        try {
            // Must be a fixed path
            if (!name.startsWith("/")) {
                name = "/" + name;
            }

            URI uri = new URI(this.getProtocolId(), null, this.agent.getAgentID().toString(), this.getPort(),
                name, null, null);

            // register the object on the register
            InternalRemoteRemoteObject irro = new InternalRemoteRemoteObjectImpl(remoteObject, uri);
            RemoteRemoteObject rmo = register(irro, uri, rebind);
            irro.setRemoteRemoteObject(rmo);

            return irro;
        } catch (URISyntaxException e) {
            throw new ProActiveException("Failed to create remote object " + name, e);
        }
    }

    public Agent getAgent() {
        return this.agent;
    }

    public URI getBaseURI() {
        return URI.create(this.getProtocolId() + "://" + this.agent.getAgentID() + "/");
    }
}
