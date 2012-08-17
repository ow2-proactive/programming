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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.utils.TimeoutAccounter;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;


/**
 * AMQP specification does not provide a way to list existing queues.
 * However the Remote Object Factory is expected to provide such a feature.
 * to overcome the limitation a custom discovery mechanism has been introduced.
 * when created, each remote object binds itself to a particular exchange 
 * # 
 * @since 5.2.0
 *
 */
class FindQueuesRPCClient {

    FindQueuesRPCClient() {
    }

    public List<URI> discover(URI uri, String exchangeName, long timeout) throws Exception {
        ReusableChannel reusableChannel = AMQPUtils.getChannel(uri);
        try {
            Channel channel = reusableChannel.getChannel();

            String replyQueueName = channel.queueDeclare().getQueue();

            QueueingConsumer consumer = new QueueingConsumer(channel);
            String consumerTag = channel.basicConsume(replyQueueName, true, consumer);

            List<URI> response = new ArrayList<URI>();
            String corrId = java.util.UUID.randomUUID().toString();

            BasicProperties props = new BasicProperties.Builder().correlationId(corrId).replyTo(
                    replyQueueName).type(AMQPConfig.PA_AMQP_DISCOVERY_QUEUES_MESSAGE_TYPE.getValue()).build();

            channel.basicPublish(exchangeName, "", props, null);

            TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);

            while (!time.isTimeoutElapsed()) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery(200);
                if ((delivery != null) && (delivery.getProperties().getCorrelationId().equals(corrId))) {
                    URI u = URI.create(new String(delivery.getBody()));
                    response.add(u);
                }
            }

            // stop consuming, this also should delete temporary queue
            channel.basicCancel(consumerTag);

            AMQPUtils.returnChannel(reusableChannel);

            return response;
        } catch (Exception e) {
            reusableChannel.close();
            throw e;
        }
    }
}
