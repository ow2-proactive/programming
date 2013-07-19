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
package org.objectweb.proactive.extensions.amqp.federation;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

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
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPUtils;
import org.objectweb.proactive.extensions.amqp.remoteobject.ReusableChannel;

import com.rabbitmq.client.AMQP.BasicProperties;


/**
 * Remote Object Factory adds support for 'amqp-federation' protocol.
 *   
 * @since ProActive 5.2.0
 */
public class AMQPFederationRemoteObjectFactory extends AbstractRemoteObjectFactory implements
        RemoteObjectFactory {

    static final Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT_FACTORY);

    static final public String PROTOCOL_ID = "amqp-federation";

    public AMQPFederationRemoteObjectFactory() {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.objectweb.proactive.core.remoteobject.RemoteObjectFactory#newRemoteObject
     * (org.objectweb .proactive.core.remoteobject.RemoteObject)
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        try {
            return new AMQPFederationRemoteObject(target.getURI());
        } catch (IOException e) {
            throw new ProActiveException(String.format("AMQP unable to create the RemoteRemoteObject for %s",
                    target.toString()), e);
        }
    }

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
        try {
            AMQPFederationRemoteObjectServer amqpROS = new AMQPFederationRemoteObjectServer(ro);
            amqpROS.connect(replacePrevious);
            return new AMQPFederationRemoteObject(uri);
        } catch (IOException e) {
            throw new ProActiveException(String.format("AMQP unable to register the object at %s", uri
                    .toString()), e);
        }
    }

    /**
     * Unregisters an remote object previously registered into the bodies table
     *
     * @param urn
     *            the urn under which the active object has been registered
     */
    public void unregister(URI uri) throws ProActiveException {
        String queueName = AMQPUtils.computeQueueNameFromURI(uri);

        ReusableChannel channel = null;
        try {
            channel = AMQPFederationUtils.getChannel(uri);
            channel.getChannel().basicPublish(
                    AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_EXCHANGE_NAME.getValue(),
                    queueName,
                    new BasicProperties.Builder().type(
                            AMQPFederationRemoteObjectServer.DELETE_QUEUE_MESSAGE_TYPE).build(), null);
            channel.returnChannel();
        } catch (IOException e) {
            if (channel != null) {
                channel.close();
            }
            throw new ProActiveException("Failed to delete object's queue", e);
        }
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
    public <T> RemoteObject<T> lookup(URI uri) throws ProActiveException {
        try {
            String queueName = AMQPUtils.computeQueueNameFromURI(uri);
            if (!AMQPFederationUtils.pingRemoteObject(queueName, uri)) {
                throw new ProActiveException("Failed to find queue for the object with URI " + uri);
            }
            return new RemoteObjectAdapter(new AMQPFederationRemoteObject(uri));
        } catch (IOException e) {
            throw new NotBoundException(String.format("unable to lookup object at %s", uri.toString()), e);
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
        // AMQP specification does not provide a way to list existing queues
        // so to address this, each queue (== Remote Oject) binds itself to 
        // a specific exchange and each RemoteObject listen for a particular message
        // in order to return its url.
        try {
            FederationFindQueuesRPCClient finder = new FederationFindQueuesRPCClient();

            List<URI> response;

            response = finder.discover(uri, AMQPFederationConfig.PA_AMQP_FEDERATION_DISCOVER_EXCHANGE_NAME
                    .getValue(), 5000);

            URI[] result = response.toArray(new URI[response.size()]);
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("AMQP Registry contains %s", Arrays.toString(result)));
            }
            return result;
        } catch (Exception e) {
            throw new ProActiveException(String.format("unable to list the AMQP registry at %s", uri
                    .toString()), e);
        }

    }

    public String getProtocolId() {
        return PROTOCOL_ID;
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        // TODO 
        logger.debug("unexport is not supported for amqp-federation");
    }

    public int getPort() {
        return AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_PORT.getValue();
    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name,
            boolean rebind) throws ProActiveException {
        try {
            // Must be a fixed path
            if (!name.startsWith("/")) {
                name = "/" + name;
            }

            URI uri = new URI(this.getProtocolId(), null,
                AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_ADDRESS.getValue(), this.getPort(), name,
                null, null);

            // register the object on the register
            InternalRemoteRemoteObject irro = new InternalRemoteRemoteObjectImpl(remoteObject, uri);
            RemoteRemoteObject rmo = register(irro, uri, rebind);
            irro.setRemoteRemoteObject(rmo);

            return irro;
        } catch (URISyntaxException e) {
            throw new ProActiveException(String.format("Failed to create remote object %s", name), e);
        }
    }

    public URI getBaseURI() {
        return URIBuilder.buildURI(AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_ADDRESS.getValue(), "",
                this.getProtocolId(), AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_PORT.getValue());
    }

    public ObjectInputStream getProtocolObjectInputStream(InputStream in) throws IOException {
        return new ProActiveMarshalInputStream(in);
    }

    public ObjectOutputStream getProtocolObjectOutputStream(OutputStream out) throws IOException {
        return new ProActiveMarshalOutputStream(out, ProActiveRuntimeImpl.getProActiveRuntime().getURL());
    }
}
