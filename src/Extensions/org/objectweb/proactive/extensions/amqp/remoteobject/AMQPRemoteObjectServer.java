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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.ThreadPools;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.AMQP.Queue.BindOk;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;


/**
 * Server-side implementation of the remote object
 *  
 * @since 5.2.0
 */
public class AMQPRemoteObjectServer {

    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT);

    Connection connection = null;
    Channel channel = null;
    InternalRemoteRemoteObject rro = null;
    QueueingConsumer consumer = null;
    String queueName = null;
    String consumerTag = null;
    BindOk queueBind = null;
    DeclareOk ok = null;

    static final ThreadPoolExecutor tpe = ThreadPools.newCachedThreadPool(1, TimeUnit.SECONDS,
            new NamedThreadFactory("AMQP Consumer Thread ", true));

    public AMQPRemoteObjectServer(InternalRemoteRemoteObject rro) throws ProActiveException, IOException {
        this.rro = rro;
        String name = URIBuilder.getNameFromURI(rro.getURI());
        this.queueName = AMQPUtils.computeQueueNameFromName(name);
    }

    public void connect(boolean passive) throws IOException, ProActiveException {

        channel = AMQPUtils.getChannelToBroker(rro.getURI());

        boolean autoDelete = true;
        boolean durable = false;
        boolean exclusive = false;
        java.util.Map<java.lang.String, java.lang.Object> arguments = null;

        try {

            ok = channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);

            logger.debug(String.format("declared queue %s,response %s", queueName, ok.toString()));

            // bind the queue to the exchange of the rof
            // to receive service messages
            queueBind = channel.queueBind(queueName, AMQPConfig.PA_AMQP_FACTORY_EXCHANGE_NAME.getValue(), "");

            boolean autoAck = true;

            consumerTag = channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        AMQP.BasicProperties properties, byte[] body) throws IOException {

                    final Envelope env = envelope;
                    final AMQP.BasicProperties p = properties;
                    final byte[] b = body;

                    tpe.execute(new Runnable() {
                        public void run() {

                            String routingKey = env.getRoutingKey();
                            byte[] rep = null;
                            long deliveryTag = env.getDeliveryTag();

                            BasicProperties props = p;
                            BasicProperties replyProps = new BasicProperties.Builder().correlationId(
                                    props.getCorrelationId()).build();

                            try {

                                if (AMQPConfig.PA_AMQP_DISCOVERY_QUEUES_MESSAGE_TYPE.getValue().equals(
                                        props.getType())) {

                                    // service message

                                    rep = rro.getURI().toString().getBytes();

                                } else {

                                    byte[] message = b;
                                    Request req = (Request) ByteToObjectConverter.ProActiveObjectStream
                                            .convert(message);

                                    logger.debug(String.format("message %s consumed on queue %s", req
                                            .getMethodName(), queueName));

                                    Reply reply = rro.receiveMessage(req);
                                    rep = ObjectToByteConverter.ProActiveObjectStream.convert(reply);

                                }

                            } catch (Exception e) {
                                Reply reply = new SynchronousReplyImpl(new MethodCallResult(null, e));
                                try {
                                    rep = ObjectToByteConverter.ProActiveObjectStream.convert(reply);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            } finally {
                                try {
                                    channel.basicPublish("", props.getReplyTo(), replyProps, rep);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });

                }
            });

        } catch (IOException e) {
            unbind();
            throw e;
        }

    }

    private void unbind() throws IOException, ProActiveException {
        if (queueBind != null) {
            channel.queueUnbind(queueName, AMQPConfig.PA_AMQP_FACTORY_EXCHANGE_NAME.getValue(), "");
        }

        if (ok != null) {
            channel.queueDelete(queueName);
        }
    }

}
