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

import javax.net.SocketFactory;

import org.objectweb.proactive.core.ssh.SshTunnelSocketFactory;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extensions.amqp.AMQPConfig;


/**
 * Utility class
 * @since 5.2.0
 *
 */
public class AMQPUtils {

    private static final ConnectionAndChannelFactory connectionFactory;

    static {
        SocketFactory socketFactory;
        if (AMQPConfig.PA_AMQP_SOCKET_FACTORY.isSet() &&
            "ssh".equals(AMQPConfig.PA_AMQP_SOCKET_FACTORY.getValue())) {
            socketFactory = new SshTunnelSocketFactory(AMQPConfig.PA_AMQP_SSH_KEY_DIR,
                AMQPConfig.PA_AMQP_SSH_KNOWN_HOSTS, AMQPConfig.PA_AMQP_SSH_REMOTE_PORT,
                AMQPConfig.PA_AMQP_SSH_REMOTE_USERNAME);
        } else {
            socketFactory = null;
        }
        connectionFactory = new ConnectionAndChannelFactory(socketFactory);
    }

    private static final String QUEUE_PREFIX = "proactive.remoteobject.";

    /**
     * build an AMQP queue name from the remote object's name used in Programming
     */
    public static String computeQueueNameFromURI(URI uri) {
        return QUEUE_PREFIX + URIBuilder.getNameFromURI(uri);
    }

    static ReusableChannel getChannel(URI uri) throws IOException {
        return connectionFactory.getChannel(getConnectionParameters(uri));
    }

    static RpcReusableChannel getRpcChannel(URI uri) throws IOException {
        return connectionFactory.getRpcChannel(getConnectionParameters(uri));
    }

    private static AMQPConnectionParameters getConnectionParameters(URI uri) {
        String host = getBrokerHost(uri);
        int port = getBrokerPort(uri);
        String username = AMQPConfig.PA_AMQP_BROKER_USER.getValue();
        String password = AMQPConfig.PA_AMQP_BROKER_PASSWORD.getValue();
        String vhost = AMQPConfig.PA_AMQP_BROKER_VHOST.getValue();
        return new AMQPConnectionParameters(host, port, username, password, vhost);
    }

    private static String getBrokerHost(URI uri) {
        String host = URIBuilder.getHostNameFromUrl(uri);
        if ((host == null) || (host.isEmpty())) {
            if (AMQPConfig.PA_AMQP_BROKER_ADDRESS.isSet()) {
                host = AMQPConfig.PA_AMQP_BROKER_ADDRESS.getValue();
            }
        }
        return host;
    }

    private static int getBrokerPort(URI uri) {
        int port = URIBuilder.getPortNumber(uri);
        if (port == 0) {
            if (AMQPConfig.PA_AMQP_BROKER_PORT.isSet()) {
                port = AMQPConfig.PA_AMQP_BROKER_PORT.getValue();
            }
        }
        return port;
    }

}
