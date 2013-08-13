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
