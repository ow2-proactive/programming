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
package org.objectweb.proactive.extensions.amqp.remoteobject;

import java.io.IOException;
import java.net.URI;

import com.rabbitmq.client.Channel;


/**
 * Class used to discover remote objects with 'amqp' protocol.
 * It inherits discover logic from the AbstractFindQueuesRPCClient
 * and implements logic specific for 'amqp' protocol:
 * <ul>
 * <li>ReusableChannel is received using AMQPUtils.getChannel
 * (to connect to the broker AMQPUtils extracts broker's host/port from the remote object's URL)
 * <li>temporary reply queue with unique name is created using
 * standard AMQP method 'queueDeclare' 
 * </ul>
 * 
 * @since 5.2.0
 *
 */
class FindQueuesRPCClient extends AbstractFindQueuesRPCClient {

    @Override
    protected ReusableChannel getReusableChannel(URI uri) throws IOException {
        return AMQPUtils.getChannel(uri);
    }

    @Override
    protected String createReplyQueue(Channel channel) throws IOException {
        return channel.queueDeclare().getQueue();
    }

}
