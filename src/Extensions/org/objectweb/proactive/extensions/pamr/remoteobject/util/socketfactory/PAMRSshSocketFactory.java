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
package org.objectweb.proactive.extensions.pamr.remoteobject.util.socketfactory;

import java.io.IOException;
import java.net.Socket;

import org.objectweb.proactive.core.ssh.SshConfig;
import org.objectweb.proactive.core.ssh.SshTunnelPool;
import org.objectweb.proactive.core.ssh.SshConfigFileParser.SshToken;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;


/**
 * This implementation for message routing socket factory
 * offers secure SSH sockets
 */
public class PAMRSshSocketFactory implements PAMRSocketFactorySPI {

    final private SshConfig config;
    final private SshTunnelPool tp;

    public PAMRSshSocketFactory() {
        this.config = new SshConfig();

        // No plain socket, we want to use SSH !
        this.config.setTryPlainSocket(false);

        this.config.setGcInterval(60000);

        if (PAMRConfig.PA_PAMRSSH_CONNECT_TIMEOUT.isSet()) {
            int connectTimeout = PAMRConfig.PA_PAMRSSH_CONNECT_TIMEOUT.getValue();
            this.config.setConnectTimeout(connectTimeout);
        }

        if (PAMRConfig.PA_PAMRSSH_GC_IDLETIME.isSet()) {
            int gcIdleTime = PAMRConfig.PA_PAMRSSH_GC_IDLETIME.getValue();
            this.config.setGcIdleTime(gcIdleTime);
        }

        if (PAMRConfig.PA_PAMRSSH_GC_PERIOD.isSet()) {
            int gcInterval = PAMRConfig.PA_PAMRSSH_GC_PERIOD.getValue();
            this.config.setGcInterval(gcInterval);
        }

        if (PAMRConfig.PA_PAMRSSH_KEY_DIR.isSet()) {
            String dir = PAMRConfig.PA_PAMRSSH_KEY_DIR.getValue();
            this.config.setKeyDir(dir);
        }

        if (PAMRConfig.PA_PAMRSSH_KNOWN_HOSTS.isSet()) {
            String knownhost = PAMRConfig.PA_PAMRSSH_KNOWN_HOSTS.getValue();
            this.config.setKnowHostFile(knownhost);
        }

        if (PAMRConfig.PA_PAMRSSH_REMOTE_PORT.isSet()) {
            int port = PAMRConfig.PA_PAMRSSH_REMOTE_PORT.getValue();
            this.config.addDefaultHostInformation(SshToken.PORT, String.valueOf(port));
        }

        if (PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME.isSet()) {
            String username = PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME.getValue();
            this.config.addDefaultHostInformation(SshToken.USERNAME, username);
        }

        this.tp = new SshTunnelPool(this.config);
    }

    public SshConfig getSshConfig() {
        return this.config;
    }

    public Socket createSocket(String host, int port) throws IOException {
        return tp.getSocket(host, port);
    }

    public String getAlias() {
        return "ssh";
    }
}
