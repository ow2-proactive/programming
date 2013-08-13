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
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.utils.NamedThreadFactory;
import org.objectweb.proactive.utils.ThreadPools;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


/**
 * Abstract class representing server part of the AMQP Remote Object. 
 * Abstract class was introduced since some parts of communication 
 * algorithm should be handled differently for 'amqp' and 'amqp-federation' 
 * protocols.   
 * <p>
 * AbstractAMQPRemoteObject implements logic common for 'amqp' and 'amqp-federation' protocols. 
 * When server object is created it creates queue with unique name (queue name
 * is constructed using remote object's unique name), binds queue to the global direct 
 * exchange and listens for incoming messages. There are two message types common
 * for 'amqp' and 'amqp-federation' protocols:
 * <ul>
 * <li>RPC request: in this case message body is serialized Request, this request
 * is deserialized, passed to the InternalRemoteRemoteObject.receiveMessage and
 * serialized Reply is sent to the reply queue
 * <li>Discover request: this request is sent to discover all existing remote objects,
 * message body is empty. When this message is received AMQPRemoteObjectServer sends
 * its URL to the reply queue. 
 * </ul>
 * 
 * @author ProActive team
 * @since 5.2.0
 *
 */
public abstract class AbstractAMQPRemoteObjectServer {

    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT);

    protected final InternalRemoteRemoteObject rro;

    static final ThreadPoolExecutor tpe = ThreadPools.newCachedThreadPool(5, TimeUnit.MINUTES,
            new NamedThreadFactory("AMQP Consumer Thread ", true));

    public AbstractAMQPRemoteObjectServer(InternalRemoteRemoteObject rro) {
        this.rro = rro;
    }

    protected abstract ReusableChannel getReusableChannel() throws ProActiveException, IOException;

    protected abstract void createObjectQueue(Channel channel, String queueName) throws IOException;

    protected abstract byte[] handleMessage(Channel channel, AMQP.BasicProperties props, byte[] body)
            throws Exception;

    protected abstract String getReplyExchange();

    final class Consumer extends DefaultConsumer {

        private final ReusableChannel reusableChannel;

        public Consumer(ReusableChannel channel) {
            super(channel.getChannel());
            this.reusableChannel = channel;
        }

        @Override
        public void handleCancel(String consumerTag) throws IOException {
            // 'handleCancel' is called after object's queue is deleted
            reusableChannel.returnChannel();
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope, final AMQP.BasicProperties props,
                final byte[] body) throws IOException {
            tpe.execute(new Runnable() {
                public void run() {
                    byte[] replyBody;

                    try {
                        String messageType = props.getType();

                        if (messageType == null) {
                            replyBody = handleMethodCall(body);
                        } else if (AbstractFindQueuesRPCClient.DISCOVERY_QUEUES_MESSAGE_TYPE
                                .equals(messageType)) {
                            replyBody = handleDiscoverQueueMessage();
                        } else {
                            replyBody = handleMessage(getChannel(), props, body);
                        }
                    } catch (Exception e) {
                        logger.error("Error during message processing", e);
                        return;
                    }

                    if (replyBody != null) {
                        try {
                            getChannel()
                                    .basicPublish(getReplyExchange(), props.getReplyTo(), null, replyBody);
                        } catch (IOException e) {
                            logger.error("Failed to send message", e);
                        }
                    }
                }
            });

        }
    }

    private byte[] handleMethodCall(byte[] body) throws Exception {
        Request req = (Request) ByteToObjectConverter.ProActiveObjectStream.convert(body);
        Reply reply = rro.receiveMessage(req);
        return ObjectToByteConverter.ProActiveObjectStream.convert(reply);
    }

    private byte[] handleDiscoverQueueMessage() throws Exception {
        return rro.getURI().toString().getBytes();
    }

    public final void connect(boolean passive) throws IOException, ProActiveException {
        String queueName = AMQPUtils.computeQueueNameFromURI(rro.getURI());

        final ReusableChannel reusableChannel = getReusableChannel();

        boolean queueDeclared = false;

        try {
            Channel channel = reusableChannel.getChannel();

            createObjectQueue(channel, queueName);

            queueDeclared = true;

            if (logger.isDebugEnabled()) {
                logger.debug(String.format("declared queue %s", queueName));
            }

            boolean autoAck = true;
            channel.basicConsume(queueName, autoAck, new Consumer(reusableChannel));
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
