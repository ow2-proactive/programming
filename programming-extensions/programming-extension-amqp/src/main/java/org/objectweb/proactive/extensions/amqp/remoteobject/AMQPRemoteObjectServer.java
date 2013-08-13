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
package org.objectweb.proactive.extensions.amqp.remoteobject;

import java.io.IOException;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;


/**
 * Remote object server part for 'amqp' protocol. It inherits general server
 * logic from the AbstractAMQPRemoteObjectServer and implements specific for
 * 'amqp' protocol:
 * <ul>
 * <li>remote object's queue is bound to the exchanges specified in the AMQPConfig
 * <li>default "" reply exchange is used to send reply messages
 * </ul>
 *  
 */
public class AMQPRemoteObjectServer extends AbstractAMQPRemoteObjectServer {

    public AMQPRemoteObjectServer(InternalRemoteRemoteObject rro) throws ProActiveException, IOException {
        super(rro);
    }

    @Override
    protected ReusableChannel getReusableChannel() throws IOException, ProActiveException {
        return AMQPUtils.getChannel(rro.getURI());
    }

    @Override
    protected void createObjectQueue(Channel channel, String queueName) throws IOException {
        boolean autoDelete = true;
        boolean durable = false;
        boolean exclusive = false;
        Map<String, Object> arguments = null;

        channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
        channel.queueBind(queueName, AMQPConfig.PA_AMQP_DISCOVER_EXCHANGE_NAME.getValue(), "");
        channel.queueBind(queueName, AMQPConfig.PA_AMQP_RPC_EXCHANGE_NAME.getValue(), queueName);
    }

    @Override
    protected byte[] handleMessage(Channel channel, BasicProperties props, byte[] body) {
        // doesn't handle additional messages
        return null;
    }

    @Override
    protected String getReplyExchange() {
        return "";
    }
}
