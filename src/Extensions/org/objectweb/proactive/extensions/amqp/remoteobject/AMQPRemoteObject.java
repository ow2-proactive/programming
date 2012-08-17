/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.amqp.remoteobject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.QueueingConsumer.Delivery;


/**
 * AMQP remote object client part. send message to the server part through the
 * queue identified in the url.
 * 
 * @since ProActive 5.2.0
 */

public class AMQPRemoteObject implements RemoteRemoteObject, Serializable {
    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT);

    private static final String exchangeName = AMQPConfig.PA_AMQP_RPC_EXCHANGE_NAME.getValue();

    private static final long RPC_REPLY_TIMEOUT = 10000;

    private final URI remoteObjectURL;

    private final String queueName;

    public AMQPRemoteObject(URI remoteObjectURL) throws ProActiveException, IOException {
        this.remoteObjectURL = remoteObjectURL;
        this.queueName = AMQPUtils.computeQueueNameFromName(URIBuilder.getNameFromURI(remoteObjectURL));
    }

    public Reply receiveMessage(Request message) throws IOException, ProActiveException {
        RpcReusableChannel channel = AMQPUtils.getRpcChannel(remoteObjectURL);
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("AMQP RO sending %s to %s, on exchange %s, queue %s", message
                        .getMethodName(), remoteObjectURL, exchangeName, queueName));
            }

            String replyQueue = channel.getReplyQueue();
            byte[] messageBody = ObjectToByteConverter.ProActiveObjectStream.convert(message);
            channel.getChannel().basicPublish(AMQPConfig.PA_AMQP_RPC_EXCHANGE_NAME.getValue(), queueName,
                    new BasicProperties.Builder().replyTo(replyQueue).build(), messageBody);

            while (true) {
                Delivery delivery = channel.getReplyQueueConsumer().nextDelivery(RPC_REPLY_TIMEOUT);
                if (delivery != null) {
                    Reply reply = (Reply) ByteToObjectConverter.ProActiveObjectStream.convert(delivery
                            .getBody());
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "AMQP RO received response of message %s to %s, on exchange %s, queue %s",
                                message.getMethodName(), remoteObjectURL, exchangeName, queueName));
                    }
                    AMQPUtils.returnChannel(channel);
                    return reply;
                } else {
                    // if didn't receive reply check that remote object server still exists
                    ReusableChannel queueCheckChannel = AMQPUtils.getChannel(remoteObjectURL);
                    try {
                        queueCheckChannel.getChannel().queueDeclarePassive(queueName);
                        AMQPUtils.returnChannel(queueCheckChannel);
                    } catch (IOException e) {
                        throw new IOException("Failed to get response while sending request to the " +
                            queueName, e);
                    }
                }
            }
        } catch (Throwable e) {
            channel.close();

            throw new IOException(String.format("AMQP cannot send %s to %s, on exchange %s, queue %s",
                    message.getMethodName(), remoteObjectURL, exchangeName, queueName), e);
        }
    }

}
