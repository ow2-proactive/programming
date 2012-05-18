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
import java.util.WeakHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;


/**
 * Connection and Factory to enable connection and channel caching and reuse
 * A connection creates almost 8 threads and a channel 4 threads so reusing them is a good idea.
 * Tests have shown that reusing a connection is easy while reusing a channel is a bad idea as 
 * channels are closed each time an exception occurs.
 * In the current implementation, we only reuse connections, channels are not reused.
 * @since 5.2.0
 *
 */
public class ConnectionAndChannelFactory {

    final static private Logger logger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_CHANNEL_FACTORY);

    static private ConnectionAndChannelFactory instance;

    Map<String, Connection> cachedConnections = new WeakHashMap<String, Connection>();
    Map<String, Channel> cachedChannels = new WeakHashMap<String, Channel>();

    public synchronized static ConnectionAndChannelFactory getInstance() {
        if (instance == null) {
            instance = new ConnectionAndChannelFactory();
        }
        return instance;

    }

    /**
     * provides connection caching and reuse.
     * @param hostname the hostname to the broker
     * @param port the port to the broker
     * @return a Connection to the requested broker
     * @throws IOException is the broker cannot be contacted
     */
    public synchronized Connection getConnection(String hostname, int port) throws IOException {

        String key = generateKey(hostname, port);
        Connection c = cachedConnections.get(key);

        if ((c != null) && (c.isOpen())) {
            return c;
        }
        logger.debug(String.format("requested connection to %s is close, creating a new one", key));

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(hostname);
        factory.setPort(port);

        c = factory.newConnection();

        c.addShutdownListener(new AMQPShutDownListener(c.toString()));

        logger.debug(String.format("checking new connection to %s, isOpen() %s", c.toString(), c.isOpen()));
        cachedConnections.put(key, c);
        return c;
    }

    /**
     * provide a channel, try to reuse a connection if already exists
     * @param hostname the broker to contact
     * @param port the port of the broker
     * @param reuse if we want to reuse an already opened channel (bad idea so far)
     * @return a channel  
     * @throws IOException if something went wrong
     */
    public synchronized Channel getChannel(String hostname, int port, boolean reuse) throws IOException {
        Channel chan;
        String key = null;
        if (reuse) {
            key = generateKey(hostname, port);

            chan = cachedChannels.get(key);

            if ((chan != null) && (chan.isOpen())) {
                return chan;
            }

            logger.debug(String.format("requested channel to %s is close, creating a new one", key));
        }

        Connection c = getConnection(hostname, port);
        chan = c.createChannel();

        //	chan.addShutdownListener(new AMQPShutDownListener(chan.toString()));

        logger.debug(String
                .format("checking new channel to %s, isOpen() %s ", chan.toString(), chan.isOpen()));

        if (reuse) {
            cachedChannels.put(key, chan);
        }
        return chan;
    }

    private String generateKey(String hostname, int port) {
        return hostname + port;
    }

}
