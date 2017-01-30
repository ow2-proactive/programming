/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
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

    final static private Logger channelLogger = ProActiveLogger.getLogger(AMQPConfig.Loggers.AMQP_CHANNEL_FACTORY);

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
