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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


/**
 * Connection and Factory to enable connection and channel caching and reuse
 * 
 * @since 5.2.0
 *
 */
public class ConnectionAndChannelFactory {

    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_CHANNEL_FACTORY);

    private final Map<String, CachedConnection> cachedConnections = new HashMap<String, CachedConnection>();

    public static class CachedConnection {

        private final ConnectionAndChannelFactory factory;

        private final Connection connection;

        private final List<ReusableChannel> cachedChannels = new ArrayList<ReusableChannel>();

        private final List<RpcReusableChannel> cachedRpcChannels = new ArrayList<RpcReusableChannel>();

        CachedConnection(ConnectionAndChannelFactory factory, Connection connection) {
            this.factory = factory;
            this.connection = connection;
        }

        ReusableChannel getChannel() throws IOException {
            ReusableChannel channel = getChannel(cachedChannels);
            if (channel == null) {
                channel = new ReusableChannel(this, connection.createChannel());
            }
            return channel;
        }

        RpcReusableChannel getRpcChannel() throws IOException {
            RpcReusableChannel channel = (RpcReusableChannel) getChannel(cachedRpcChannels);
            if (channel == null) {
                channel = factory.createRpcReusableChannel(this, connection.createChannel());
            }
            return channel;
        }

        private ReusableChannel getChannel(List<? extends ReusableChannel> channels) throws IOException {
            synchronized (channels) {
                for (Iterator<? extends ReusableChannel> i = channels.iterator(); i.hasNext();) {
                    ReusableChannel channel = i.next();
                    i.remove();
                    if (channel.getChannel().isOpen()) {
                        return channel;
                    }
                }
                return null;
            }
        }

        void returnChannel(ReusableChannel channel) {
            if (channel instanceof RpcReusableChannel) {
                synchronized (cachedRpcChannels) {
                    cachedRpcChannels.add((RpcReusableChannel) channel);
                }
            } else {
                synchronized (cachedChannels) {
                    cachedChannels.add(channel);
                }
            }
        }

    }

    private final SocketFactory socketFactory;

    public ConnectionAndChannelFactory(SocketFactory socketFactory) {
        this.socketFactory = socketFactory;
    }

    public void returnChannel(ReusableChannel channel) {
        channel.returnChannel();
    }

    public ReusableChannel getChannel(AMQPConnectionParameters connectionParameters) throws IOException {
        CachedConnection connection = getConnection(connectionParameters);
        return connection.getChannel();
    }

    public RpcReusableChannel getRpcChannel(AMQPConnectionParameters connectionParameters) throws IOException {
        CachedConnection connection = getConnection(connectionParameters);
        return connection.getRpcChannel();
    }

    private synchronized CachedConnection getConnection(AMQPConnectionParameters connectionParameters)
            throws IOException {
        String key = connectionParameters.getKey();
        CachedConnection connection = cachedConnections.get(key);
        if (connection == null) {
            logger.debug(String.format("creating a new connection %s", key));

            ConnectionFactory factory = new ConnectionFactory();
            if (socketFactory != null) {
                factory.setSocketFactory(socketFactory);
            }
            factory.setHost(connectionParameters.getHost());
            factory.setPort(connectionParameters.getPort());
            factory.setUsername(connectionParameters.getUsername());
            factory.setPassword(connectionParameters.getPassword());
            factory.setVirtualHost(connectionParameters.getVhost());
            Connection c = factory.newConnection();
            c.addShutdownListener(new AMQPShutDownListener(c.toString()));

            connection = new CachedConnection(this, c);
            cachedConnections.put(key, connection);
        }

        return connection;
    }

    protected RpcReusableChannel createRpcReusableChannel(CachedConnection connection, Channel channel) {
        return new RpcReusableChannel(connection, channel);
    }
}
