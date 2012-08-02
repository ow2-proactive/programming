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

import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.utils.Sleeper;

import com.rabbitmq.client.AMQP.Exchange.DeclareOk;
import com.rabbitmq.client.AMQP.Queue.BindOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.ShutdownSignalException;


/**
 * AMQP remote object client part. send message to the server part through the
 * queue identified in the url.
 * 
 * @since ProActive 5.2.0
 */

public class AMQPRemoteObject implements RemoteRemoteObject, Serializable {
    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_REMOTE_OBJECT);

    /** The URL of the RemoteObject */
    private URI remoteObjectURL;

    // private transient Channel channel;
    private String queueName = null;
    // private transient String replyQueueName;
    private transient String exchangeName;
    // private transient QueueingConsumer rPCconsumer;
    private transient AMQPRemoteObjectServer amqpros;
    // private transient RpcClient rpc;
    private boolean connected = false;

    public AMQPRemoteObject(URI remoteObjectURL) throws ProActiveException, IOException {
        this.remoteObjectURL = remoteObjectURL;
        // connect();
    }

    public AMQPRemoteObject(InternalRemoteRemoteObject remoteObject, URI remoteObjectURL)
            throws ProActiveException, IOException {
        this.remoteObjectURL = remoteObjectURL;
        // connect();
    }

    public AMQPRemoteObject(InternalRemoteRemoteObject remoteObject, URI remoteObjectURL,
            AMQPRemoteObjectServer amqpros) throws ProActiveException, IOException {
        this(remoteObjectURL);
        this.amqpros = amqpros;
    }

    public Reply receiveMessage(Request message) throws IOException, ProActiveException {

        RpcClient rpc = connect(5);

        Reply response = null;

        logger.debug(String.format("AMQP RO sending %s to %s, on exchange %s, queue %s", message
                .getMethodName(), remoteObjectURL, exchangeName, queueName));

        byte[] syncReply;
        try {

            syncReply = rpc.primitiveCall(ObjectToByteConverter.ProActiveObjectStream.convert(message));
            response = (Reply) ByteToObjectConverter.ProActiveObjectStream.convert(syncReply);
        } catch (ShutdownSignalException e) {
            EOFException ex = new EOFException();
            ex.initCause(e);
            throw ex;
        } catch (Throwable e) {
            throw new IOException(String.format("AMQP cannot send %s to %s, on exchange %s, queue %s",
                    message.getMethodName(), remoteObjectURL, exchangeName, queueName), e);
        } finally {
            try {
                String exchangeName = rpc.getExchange();
                Channel c = rpc.getChannel();

                rpc.close();
                c.exchangeDelete(exchangeName);
            } catch (IOException e) {
                ProActiveLogger.logEatedException(logger, e);
            }
        }

        logger.debug(String.format("AMQP RO received response of message %s to %s, on exchange %s, queue %s",
                message.getMethodName(), remoteObjectURL, exchangeName, queueName));

        return response;

    }

    public void setURI(URI url) {
        this.remoteObjectURL = url;
    }

    public URI getURI() {
        return this.remoteObjectURL;
    }

    private RpcClient connect(int retries) throws IOException {

        Channel channel = null;
        String name = URIBuilder.getNameFromURI(remoteObjectURL);
        queueName = AMQPUtils.computeQueueNameFromName(name);
        exchangeName = AMQPUtils.generateNewExchange(name);

        channel = AMQPUtils.getChannelToBroker(remoteObjectURL);

        boolean durable = false;
        boolean autoDelete = true;
        boolean internal = false;
        DeclareOk channelDeclare = null;

        java.util.Map<java.lang.String, java.lang.Object> arguments = null;
        try {

            channelDeclare = channel.exchangeDeclare(exchangeName, "direct", durable, autoDelete, internal,
                    arguments);

        } catch (IOException ex) {
            logger.warn(ex);
            throw ex;
        }

        // strict routing
        String routingKey = queueName;
        BindOk queueBind = null;
        try {
            queueBind = channel.queueBind(queueName, exchangeName, routingKey);

            return new RpcClient(channel, exchangeName, routingKey);
        } catch (IOException e) {
            if (!channel.isOpen() && (retries > 0)) {
                new Sleeper(1000).sleep();
                return connect(retries - 1);
            }

            if (queueBind != null) {
                channel.queueUnbind(queueName, exchangeName, routingKey);
            }

            if (channelDeclare != null) {
                channel.exchangeDelete(exchangeName);
            }

            logger
                    .debug(String
                            .format(
                                    "to=%s, name=%s,queueName=%s,exhangeName=%s, caught IO channel %s isOpen %s,reason is %s",
                                    remoteObjectURL, name, queueName, exchangeName, channel.toString(),
                                    (channel != null ? channel.isOpen() : ""), ProActiveLogger
                                            .getStackTraceAsString(e)));

            throw e;

        }
    }

    // @Override
    // protected void finalize() throws Throwable {
    //
    //
    // try {
    // if (rpc != null) {
    // rpc.close();
    // }
    // } catch (Throwable e) {
    // e.printStackTrace();
    // }
    // try {
    // if (channel != null) {
    // if ( channel.getConnection().isOpen()) {
    // channel.getConnection().close() ;
    // }
    // if ( channel.isOpen() ) {
    // channel.close();
    // }
    // }
    // } catch (Throwable e) {
    // e.printStackTrace();
    // }
    // super.finalize();
    // }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        connected = false;
        // connect();
    }
}
