/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.remoteobject.util.socketfactory;

import java.io.IOException;
import java.net.Socket;

import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.ssh.SshConfig;
import org.objectweb.proactive.core.ssh.SshTunnelPool;


/**
 * This implementation for message routing socket factory
 * offers secure SSH sockets
 */
public class MessageRoutingSshSocketFactory implements MessageRoutingSocketFactorySPI {

    final private SshConfig config;
    final private SshTunnelPool tp;

    public MessageRoutingSshSocketFactory() {
        this.config = new SshConfig();

        // No plain socket, we want to use SSH !
        this.config.setTryPlainSocket(false);

        this.config.setGcInterval(60000);

        if (PAProperties.PA_PAMRSSH_CONNECT_TIMEOUT.isSet()) {
            int connectTimeout = PAProperties.PA_PAMRSSH_CONNECT_TIMEOUT.getValueAsInt();
            this.config.setConnectTimeout(connectTimeout);
        }

        if (PAProperties.PA_PAMRSSH_GC_IDLETIME.isSet()) {
            int gcIdleTime = PAProperties.PA_PAMRSSH_GC_IDLETIME.getValueAsInt();
            this.config.setGcIdleTime(gcIdleTime);
        }

        if (PAProperties.PA_PAMRSSH_GC_PERIOD.isSet()) {
            int gcInterval = PAProperties.PA_PAMRSSH_GC_PERIOD.getValueAsInt();
            this.config.setGcInterval(gcInterval);
        }

        if (PAProperties.PA_PAMRSSH_KEY_DIR.isSet()) {
            String dir = PAProperties.PA_PAMRSSH_KEY_DIR.getValue();
            this.config.setKeyDir(dir);
        }

        if (PAProperties.PA_PAMRSSH_KNOWN_HOSTS.isSet()) {
            String knownhost = PAProperties.PA_PAMRSSH_KNOWN_HOSTS.getValue();
            this.config.setKnowHostFile(knownhost);
        }

        if (PAProperties.PA_PAMRSSH_REMOTE_PORT.isSet()) {
            int port = PAProperties.PA_PAMRSSH_REMOTE_PORT.getValueAsInt();
            this.config.setPort(port);
        }

        if (PAProperties.PA_PAMRSSH_REMOTE_USERNAME.isSet()) {
            String username = PAProperties.PA_PAMRSSH_REMOTE_USERNAME.getValue();
            this.config.setUsername(username);
        }

        this.tp = new SshTunnelPool(this.config);
    }

    public Socket createSocket(String host, int port) throws IOException {
        return tp.getSocket(host, port);
    }

    public String getAlias() {
        return "ssh";
    }
}
