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

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;


/**
 *  AMQP properties
 * @since ProActive 5.2.0
 */

public class AMQPQueuePoll implements Runnable {

    public static final Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP);
    Channel channel = null;
    InternalRemoteRemoteObject rro = null;
    QueueingConsumer consumer = null;

    public AMQPQueuePoll(QueueingConsumer consumer, Channel channel, InternalRemoteRemoteObject rro) {
        this.rro = rro;
        this.consumer = consumer;
        this.channel = channel;
    }

    public void run() {
        while (true) {
            Reply rep = null;

            QueueingConsumer.Delivery delivery;

            try {

                delivery = consumer.nextDelivery();
                logger.debug("message consumed ");
                BasicProperties props = delivery.getProperties();
                BasicProperties replyProps = new BasicProperties.Builder().correlationId(
                        props.getCorrelationId()).build();

                try {
                    byte[] message = delivery.getBody();
                    Request req = (Request) ByteToObjectConverter.ProActiveObjectStream.convert(message);

                    rep = rro.receiveMessage(req);

                    channel.basicPublish("", props.getReplyTo(), replyProps,
                            ObjectToByteConverter.ProActiveObjectStream.convert(rep));

                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                } catch (Exception e) {
                    e.printStackTrace();
                    rep = new SynchronousReplyImpl(new MethodCallResult(null, e));

                }
            } catch (ShutdownSignalException e1) {
                // exit from the loop, broker is dead
                e1.printStackTrace();
                break;
            } catch (ConsumerCancelledException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
    }
}
