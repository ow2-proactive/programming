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
import java.net.URI;

import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;

import com.rabbitmq.client.Channel;


/**
 * Utility class
 * @since 5.2.0
 *
 */
public class AMQPUtils {

    /**
     *
     * @param remoteObjectURL
     * @return
     */
    public static String computeQueueNameFromUrl(String remoteObjectURL) {
        String name = URIBuilder.getNameFromURI(remoteObjectURL);
        return computeQueueNameFromName(name);
    }

    /**
     * build an AMQP queue name from the name used in Programming
     */
    public static String computeQueueNameFromName(String name) {
        return AMQPConfig.PA_AMQP_QUEUE_PREFIX.getValue() + name;
    }

    public static String generateNewExchange(String name) {
        return AMQPConfig.PA_AMQP_QUEUE_PREFIX.getValue() + name + "." +
            java.util.UUID.randomUUID().toString();
    }

    public static Channel getChannelToBroker(URI uri) throws IOException {

        String host = URIBuilder.getHostNameFromUrl(uri);
        if ((host == null) || (host.isEmpty())) {
            if (AMQPConfig.PA_AMQP_BROKER_ADDRESS.isSet()) {
                host = AMQPConfig.PA_AMQP_BROKER_ADDRESS.getValue();
            }
        }

        int port = URIBuilder.getPortNumber(uri);
        if (port == 0) {
            if (AMQPConfig.PA_AMQP_BROKER_PORT.isSet()) {
                port = AMQPConfig.PA_AMQP_BROKER_PORT.getValue();
            }
        }

        Channel channel = ConnectionAndChannelFactory.getInstance().getChannel(host, port, false);

        return channel;
    }

}
