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
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.extensions.amqp.remoteobject.AMQPUtils;
import org.objectweb.proactive.extensions.amqp.remoteobject.AbstractAMQPRemoteObjectServer;
import org.objectweb.proactive.extensions.amqp.remoteobject.ReusableChannel;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;


/**
 * Remote object server part for 'amqp-federation' protocol. It inherits general server
 * logic from the AbstractAMQPRemoteObjectServer and implements specific for
 * 'amqp-federation' protocol:
 * <ul>
 * <li>remote object's queue is bound to the federated exchanges specified in the AMQPFederationConfig
 * <li>special federated exchange is used to send reply messages
 * <li>handles 'delete' request which is used to unregister remote object and delete its queue 
 * (it isn't possible to use regular AMQP 'deleteQueue' command since remote object's queue can be 
 * created in another broker)
 * <li>handles 'ping' request which is used to check that remote object server is alive
 * </ul>
 *  
 */
public class AMQPFederationRemoteObjectServer extends AbstractAMQPRemoteObjectServer {

    private static final String REPLY_EXCHANGE = AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_REPLY_EXCHANGE_NAME
            .getValue();

    static final String DELETE_QUEUE_MESSAGE_TYPE = "delete";

    static final String PING_MESSAGE_TYPE = "ping";

    private final String queueName;

    public AMQPFederationRemoteObjectServer(InternalRemoteRemoteObject rro) throws IOException,
            ProActiveException {
        super(rro);
        this.queueName = AMQPUtils.computeQueueNameFromURI(rro.getURI());
    }

    @Override
    protected ReusableChannel getReusableChannel() throws ProActiveException, IOException {
        return AMQPFederationUtils.getChannel(rro.getURI());
    }

    @Override
    protected void createObjectQueue(Channel channel, String queueName) throws IOException {
        boolean autoDelete = true;
        boolean durable = false;
        boolean exclusive = false;
        Map<String, Object> arguments = null;

        channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
        channel.queueBind(queueName, AMQPFederationConfig.PA_AMQP_FEDERATION_DISCOVER_EXCHANGE_NAME
                .getValue(), "");
        channel.queueBind(queueName, AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_EXCHANGE_NAME.getValue(),
                queueName);
    }

    @Override
    protected byte[] handleMessage(Channel channel, BasicProperties props, byte[] body) throws IOException {
        if (DELETE_QUEUE_MESSAGE_TYPE.equals(props.getType())) {
            channel.queueDelete(queueName);
            return null;
        } else if (PING_MESSAGE_TYPE.equals(props.getType())) {
            return new byte[] {};
        } else {
            return null;
        }
    }

    @Override
    protected String getReplyExchange() {
        return REPLY_EXCHANGE;
    }

}
