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

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;


/**
 * Abstract class representing client part of the AMQP Remote Object. 
 * Abstract class was introduced since some parts of communication 
 * algorithm should be handled differently for 'amqp' and 'amqp-federation' 
 * protocols.   
 * <p>
 * AbstractAMQPRemoteObject implements general RPC algorithm common 
 * for 'amqp' and 'amqp-federation' protocols. 
 * Each server object listens for the messages on the dedicated queue,
 * AMQPRemoteObject extracts server object's queue name from the server 
 * object's url and executes remote call using AMQP:
 * <ul>
 * <li>get RpcReusableChannel
 * <li>send message to the server using global direct exchange (replyTo 
 * attribute is set to the reply queue name provided by the RpcReusableChannel)    
 * <li>wait for the reply on the replyQueue
 * </ul>
 * <p>
 * To don't wait forever for the reply in case if remote object is down AMQPRemoteObject periodically checks
 * that server object's queue still exists.
 * 
 * @author ProActive team
 * @since 5.2.0
 *
 */
public abstract class AbstractAMQPRemoteObject implements RemoteRemoteObject, Serializable {
    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT);

    private final String rpcExchangeName;

    private final long replyTimeout;

    protected final URI remoteObjectURL;

    protected final String queueName;

    public AbstractAMQPRemoteObject(URI remoteObjectURL, String rpcExchangeName, long replyTimeout)
            throws ProActiveException, IOException {
        this.remoteObjectURL = remoteObjectURL;
        this.queueName = AMQPUtils.computeQueueNameFromURI(remoteObjectURL);
        this.rpcExchangeName = rpcExchangeName;
        this.replyTimeout = replyTimeout;
    }

    @Override
    public URI getURI() {
        return remoteObjectURL;
    }

    /*
     * Get RpcReusableChannel which will be used to execute remote call
     */
    protected abstract RpcReusableChannel getRpcReusableChannel() throws IOException;

    /*
     * Check that target remote object still exists
     */
    protected abstract void checkTargetObjectExists() throws IOException;

    @Override
    public final Reply receiveMessage(Request message) throws IOException, ProActiveException {
        RpcReusableChannel channel = getRpcReusableChannel();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("AMQP RO sending %s to %s, on exchange %s, queue %s", message
                        .getMethodName(), remoteObjectURL, rpcExchangeName, queueName));
            }

            String replyQueue = channel.getReplyQueue();
            byte[] messageBody = ObjectToByteConverter.ProActiveObjectStream.convert(message);
            channel.getChannel().basicPublish(rpcExchangeName, queueName,
                    new BasicProperties.Builder().replyTo(replyQueue).build(), messageBody);

            while (true) {
                Delivery delivery = channel.getReplyQueueConsumer().nextDelivery(replyTimeout);
                if (delivery != null) {
                    Reply reply = (Reply) ByteToObjectConverter.ProActiveObjectStream.convert(delivery
                            .getBody());
                    if (logger.isDebugEnabled()) {
                        logger.debug(String.format(
                                "AMQP RO received response of message %s to %s, on exchange %s, queue %s",
                                message.getMethodName(), remoteObjectURL, rpcExchangeName, queueName));
                    }
                    channel.returnChannel();
                    return reply;
                } else {
                    // if didn't receive reply after timeout expired then check that remote object server still exists
                    checkTargetObjectExists();
                }
            }
        } catch (Throwable e) {
            channel.close();

            throw new IOException(String.format("AMQP cannot send %s to %s, on exchange %s, queue %s",
                    message.getMethodName(), remoteObjectURL, rpcExchangeName, queueName), e);
        }
    }

}
