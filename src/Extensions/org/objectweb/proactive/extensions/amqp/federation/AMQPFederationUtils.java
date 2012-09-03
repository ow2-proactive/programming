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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.amqp.federation;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPConnectionParameters;
import org.objectweb.proactive.extensions.amqp.remoteobject.ConnectionAndChannelFactory;
import org.objectweb.proactive.extensions.amqp.remoteobject.ReusableChannel;
import org.objectweb.proactive.extensions.amqp.remoteobject.RpcReusableChannel;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;


/**
 * Utility class
 * @since 5.2.0
 *
 */
class AMQPFederationUtils {

    private static final ConnectionAndChannelFactory connectionFactory = new ConnectionAndChannelFactory() {
        @Override
        protected RpcReusableChannel createRpcReusableChannel(CachedConnection connection, Channel channel) {
            return new FederationRpcReusableChannel(connection, channel);
        }
    };

    private static final Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT);

    /*
     * For 'amqp-federation' protocol broker's address isn't extracted from the remote
     * object's URL, it is assumed that it is possible to connect only to one broker specified in the 
     * AMQPFederationConfig.  
     */
    private static final AMQPConnectionParameters connectionParameters;

    static {
        String host = AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_ADDRESS.getValue();
        int port = AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_PORT.getValue();
        String username = AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_USER.getValue();
        String password = AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_PASSWORD.getValue();
        String vhost = AMQPFederationConfig.PA_AMQP_FEDERATION_BROKER_VHOST.getValue();
        connectionParameters = new AMQPConnectionParameters(host, port, username, password, vhost);
    }

    static boolean pingRemoteObject(String queueName) throws IOException {
        RpcReusableChannel reusableChannel = AMQPFederationUtils.getRpcChannel();
        Channel channel = reusableChannel.getChannel();
        try {
            BasicProperties props = new BasicProperties.Builder().replyTo(reusableChannel.getReplyQueue())
                    .type(AMQPFederationRemoteObjectServer.PING_MESSAGE_TYPE).build();

            channel.basicPublish(AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_EXCHANGE_NAME.getValue(),
                    queueName, props, null);

            QueueingConsumer.Delivery delivery = null;

            try {
                delivery = reusableChannel.getReplyQueueConsumer().nextDelivery(
                        AMQPFederationConfig.PA_AMQP_FEDERATION_PING_TIMEOUT.getValue());
            } catch (InterruptedException e) {
                logger.warn("AMQPFederationUtils.isQueueExists is interrupted", e);
            }

            if (delivery == null) {
                reusableChannel.close();
                return false;
            } else {
                reusableChannel.returnChannel();
                return true;
            }
        } catch (IOException e) {
            channel.close();
            throw e;
        }
    }

    /*
     * Generate unique queue name. Can't use broker-generated unique queue names 
     * for 'amqp-federation' protocol since queue name should be unique among 
     * multiple brokers
     */
    static String uniqueQueueName(String prefix) {
        return prefix + "_" + new UniqueID().getCanonString();
    }

    static ReusableChannel getChannel() throws IOException {
        return connectionFactory.getChannel(connectionParameters);
    }

    static RpcReusableChannel getRpcChannel() throws IOException {
        return connectionFactory.getRpcChannel(connectionParameters);
    }

}
