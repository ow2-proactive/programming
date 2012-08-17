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
import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


/**
 * Server-side implementation of the remote object
 *  
 * @since 5.2.0
 */
public class AMQPRemoteObjectServer {

    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT);

    final static private String QUEUES_MESSAGE_TYPE = AMQPConfig.PA_AMQP_DISCOVERY_QUEUES_MESSAGE_TYPE
            .getValue();

    private final InternalRemoteRemoteObject rro;
    private final String queueName;

    static final ThreadPoolExecutor tpe = ThreadPools.newCachedThreadPool(60, TimeUnit.SECONDS,
            new NamedThreadFactory("AMQP Consumer Thread ", true));

    public AMQPRemoteObjectServer(InternalRemoteRemoteObject rro) throws ProActiveException, IOException {
        this.rro = rro;
        this.queueName = AMQPUtils.computeQueueNameFromName(URIBuilder.getNameFromURI(rro.getURI()));
    }

    public void connect(boolean passive) throws IOException, ProActiveException {
        final ReusableChannel reusableChannel = AMQPUtils.getChannel(rro.getURI());

        boolean autoDelete = true;
        boolean durable = false;
        boolean exclusive = false;
        Map<String, Object> arguments = null;

        boolean queueDeclared = false;

        try {
            Channel channel = reusableChannel.getChannel();

            Queue.DeclareOk declareOk = channel.queueDeclare(queueName, durable, exclusive, autoDelete,
                    arguments);

            queueDeclared = true;

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("declared queue %s,response %s", queueName, declareOk.toString()));
            }

            channel.queueBind(queueName, AMQPConfig.PA_AMQP_DISCOVER_EXCHANGE_NAME.getValue(), "");
            channel.queueBind(queueName, AMQPConfig.PA_AMQP_RPC_EXCHANGE_NAME.getValue(), queueName);

            boolean autoAck = true;

            channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {

                @Override
                public void handleCancel(String consumerTag) throws IOException {
                    // 'handleCancel' called after object's queue is deleted
                    AMQPUtils.returnChannel(reusableChannel);
                }

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                        final AMQP.BasicProperties props, final byte[] body) throws IOException {
                    tpe.execute(new Runnable() {
                        public void run() {
                            byte[] replyBody = null;

                            BasicProperties replyProps = null;

                            try {

                                if (QUEUES_MESSAGE_TYPE.equals(props.getType())) {
                                    // service message
                                    replyProps = new BasicProperties.Builder().correlationId(
                                            props.getCorrelationId()).build();
                                    replyBody = rro.getURI().toString().getBytes();
                                } else {
                                    Request req = (Request) ByteToObjectConverter.ProActiveObjectStream
                                            .convert(body);

                                    if (logger.isDebugEnabled()) {
                                        logger.debug(String.format("message %s consumed on queue %s", req
                                                .getMethodName(), queueName));
                                    }

                                    Reply reply = rro.receiveMessage(req);
                                    replyBody = ObjectToByteConverter.ProActiveObjectStream.convert(reply);
                                }

                            } catch (Exception e) {
                                logger.error("Error during reply processing", e);

                                Reply reply = new SynchronousReplyImpl(new MethodCallResult(null, e));
                                try {
                                    replyBody = ObjectToByteConverter.ProActiveObjectStream.convert(reply);
                                } catch (IOException convertException) {
                                    logger.error("Failed to convert reply", convertException);
                                }
                            } finally {
                                try {
                                    getChannel().basicPublish("", props.getReplyTo(), replyProps, replyBody);
                                } catch (IOException e) {
                                    logger.error("Failed to send message", e);
                                }
                            }

                        }
                    });

                }
            });

        } catch (IOException e) {
            if (queueDeclared) {
                try {
                    reusableChannel.getChannel().queueDelete(queueName);
                } catch (Exception queueDeleteException) {
                    logger.warn("Failed to delete queue", queueDeleteException);
                }
            }

            reusableChannel.close();

            throw e;
        }

    }

}
