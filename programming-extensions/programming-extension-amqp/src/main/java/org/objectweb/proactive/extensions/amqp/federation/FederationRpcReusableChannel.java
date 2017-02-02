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
import java.util.Map;

import org.objectweb.proactive.extensions.amqp.remoteobject.ConnectionAndChannelFactory.CachedConnection;

import com.rabbitmq.client.Channel;


/**
 * RpcReusableChannel which is used for 'amqp-federation' protocol. 
 * <p>
 * To create name for the reply queue it uses utility method AMQPFederationUtils.uniqueQueueName 
 * (it is needed since queue name should be unique among multiple brokers), also 
 * reply queue it bound to the special federated exchange. 
 * 
 * @author ProActive team
 *
 */
class FederationRpcReusableChannel extends org.objectweb.proactive.extensions.amqp.remoteobject.RpcReusableChannel {

    public FederationRpcReusableChannel(CachedConnection connection, Channel channel) {
        super(connection, channel);
    }

    @Override
    protected String createReplyQueue() throws IOException {
        String queueName = AMQPFederationUtils.uniqueQueueName("reply_rpc");
        boolean durable = false;
        boolean exclusive = true;
        boolean autoDelete = true;
        Map<String, Object> arguments = null;
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
        channel.queueBind(queueName,
                          AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_REPLY_EXCHANGE_NAME.getValue(),
                          queueName);
        return queueName;
    }

}
