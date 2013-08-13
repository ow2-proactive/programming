package org.objectweb.proactive.extensions.amqp.federation;

import java.io.IOException;
import java.util.Map;

import org.objectweb.proactive.extensions.amqp.remoteobject.ConnectionAndChannelFactory.CachedConnection;

import com.rabbitmq.client.Channel;


/**
 * RpcReusableChannel which is used for 'amqp-federation' protocol. 
 * <p>
 * To create name for the reply queue it uses utility method AMQPFederationUtils.uniqueQueueName 
 * (it is needed since queue name should be unique among multiple brokers), also 
 * reply queue it bound to the special federated exchange. 
 * 
 * @author ProActive team
 *
 */
class FederationRpcReusableChannel extends
        org.objectweb.proactive.extensions.amqp.remoteobject.RpcReusableChannel {

    public FederationRpcReusableChannel(CachedConnection connection, Channel channel) {
        super(connection, channel);
    }

    @Override
    protected String createReplyQueue() throws IOException {
        String queueName = AMQPFederationUtils.uniqueQueueName("reply_rpc");
        boolean durable = false;
        boolean exclusive = true;
        boolean autoDelete = true;
        Map<String, Object> arguments = null;
        channel.queueDeclare(queueName, durable, exclusive, autoDelete, arguments);
        channel.queueBind(queueName, AMQPFederationConfig.PA_AMQP_FEDERATION_RPC_REPLY_EXCHANGE_NAME
                .getValue(), queueName);
        return queueName;
    }

}
