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
package org.objectweb.proactive.extensions.amqp.federation;

import java.io.IOException;
import java.net.URI;

import org.objectweb.proactive.extensions.amqp.remoteobject.AbstractFindQueuesRPCClient;
import org.objectweb.proactive.extensions.amqp.remoteobject.ReusableChannel;

import com.rabbitmq.client.Channel;


/**
 * Class used to discover remote objects with 'amqp-federation' protocol.
 * It inherits discover logic from the AbstractFindQueuesRPCClient
 * and implements logic specific for 'amqp-federation' protocol:
 * <ul>
 * <li>ReusableChannel is received using AMQPFederationUtils.getChannel
 * (AMQPFederationUtils doesn't directly extracts broker's host/port from 
 * the remote object's URL, it uses brokers address mapping from the configuration)
 * <li>for temporary reply queue unique name is generated using utility
 * method AMQPFederationUtils.uniqueQueueName since queue name should be unique
 * among multiple brokers, also reply queue should be bound to the 
 * special federated exchange  
 * </ul>
 * 
 * @since 5.2.0
 *
 */
class FederationFindQueuesRPCClient extends AbstractFindQueuesRPCClient {

    @Override
    protected ReusableChannel getReusableChannel(URI uri) throws IOException {
        return AMQPFederationUtils.getChannel(uri);
    }

    @Override
    protected String createReplyQueue(Channel channel) throws IOException {
        String replyQueueName = AMQPFederationUtils.uniqueQueueName("reply_discover");
        channel.queueDeclare(replyQueueName, false, true, true, null);
        channel.queueBind(replyQueueName,
                          AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_REPLY_EXCHANGE_NAME.getValue(),
                          replyQueueName);
        return replyQueueName;
    }

}
