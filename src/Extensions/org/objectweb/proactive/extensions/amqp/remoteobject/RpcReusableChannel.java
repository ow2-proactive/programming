package org.objectweb.proactive.extensions.amqp.remoteobject;

import java.io.IOException;

import org.objectweb.proactive.extensions.amqp.remoteobject.ConnectionAndChannelFactory.CachedConnection;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;


/**
 * Cached AMQP channel which is used to execute RPC. To execute RPC
 * temporary reply queue with unique name is needed, creation of this queue
 * for each call has performance impact, RpcReusableChannel is used to
 * create this queue only once.
 * 
 * 
 * @author ProActive team
 *
 */
public class RpcReusableChannel extends ReusableChannel {

    private String replyQueue;

    private QueueingConsumer queueConsumer;

    public RpcReusableChannel(CachedConnection connection, Channel channel) {
        super(connection, channel);
    }

    /*
     * queue creation is moved to the separate method, so that it can be overridden 
     * for 'amqp-federation' protocol  
     */
    protected String createReplyQueue() throws IOException {
        return channel.queueDeclare().getQueue();
    }

    public final String getReplyQueue() throws IOException {
        if (replyQueue == null) {
            replyQueue = createReplyQueue();
            queueConsumer = new QueueingConsumer(channel);
            boolean autoAck = true;
            channel.basicConsume(replyQueue, autoAck, queueConsumer);
        }
        return replyQueue;
    }

    public final QueueingConsumer getReplyQueueConsumer() {
        if (queueConsumer == null) {
            throw new IllegalStateException("Queue isn't created");
        }
        return queueConsumer;
    }

}
