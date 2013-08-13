package org.objectweb.proactive.extensions.amqp.remoteobject;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;
import org.objectweb.proactive.extensions.amqp.remoteobject.ConnectionAndChannelFactory.CachedConnection;

import com.rabbitmq.client.Channel;


/**
 * Creation of new Channel for each operation has performance impact, ReusableChannel class
 * is used to organize channels cache.
 * 
 * @author ProActive team
 *
 */
public class ReusableChannel {

    final static private Logger channelLogger = ProActiveLogger
            .getLogger(AMQPConfig.Loggers.AMQP_CHANNEL_FACTORY);

    private final CachedConnection connection;

    protected Channel channel;

    ReusableChannel(CachedConnection connection, Channel channel) {
        this.connection = connection;
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public boolean isOpened() {
        return channel != null;
    }

    public void close() {
        try {
            if (channel.isOpen()) {
                channel.close();
            }
        } catch (Exception e) {
            channelLogger.warn("Failed to close channel", e);
        } finally {
            channel = null;
        }
    }

    public void returnChannel() {
        if (isOpened()) {
            connection.returnChannel(this);
        }
    }

    public String toString() {
        return getClass().getSimpleName() + "-" + channel.getChannelNumber();
    }

}
