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
package org.objectweb.proactive.core.remoteobject.rmissh;

import java.io.File;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.rmi.AbstractRmiRemoteObjectFactory;
import org.objectweb.proactive.core.ssh.SshConfig;
import org.objectweb.proactive.core.ssh.SshConfigFileParser;
import org.objectweb.proactive.core.ssh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.ssh.proxycommand.ProxyCommandConfig;


public class RmiSshRemoteObjectFactory extends AbstractRmiRemoteObjectFactory {
    final static SshRMIClientSocketFactory sf;

    static {
        SshConfig sshConfig = new SshConfig();
        sshConfig.setTryPlainSocket(CentralPAPropertyRepository.PA_RMISSH_TRY_NORMAL_FIRST.isTrue());
        sshConfig.setTryProxyCommand(ProxyCommandConfig.PA_RMISSH_TRY_PROXY_COMMAND.isTrue());

        SshConfigFileParser parser = new SshConfigFileParser();

        int gcInterval = 10000;
        if (CentralPAPropertyRepository.PA_RMISSH_GC_PERIOD.isSet()) {
            gcInterval = CentralPAPropertyRepository.PA_RMISSH_GC_PERIOD.getValue();
        }
        sshConfig.setGcInterval(gcInterval);

        int gcIdleTime = 10000;
        if (CentralPAPropertyRepository.PA_RMISSH_GC_IDLETIME.isSet()) {
            gcIdleTime = CentralPAPropertyRepository.PA_RMISSH_GC_IDLETIME.getValue();
        }
        sshConfig.setGcIdleTime(gcIdleTime);

        int connectTimeout = 2000;
        if (CentralPAPropertyRepository.PA_RMISSH_CONNECT_TIMEOUT.isSet()) {
            connectTimeout = CentralPAPropertyRepository.PA_RMISSH_CONNECT_TIMEOUT.getValue();
        }
        sshConfig.setConnectTimeout(connectTimeout);

        String knownHostfile = System.getProperty("user.home") + File.separator + ".ssh" + File.separator +
                               "know_hosts";
        if (CentralPAPropertyRepository.PA_RMISSH_KNOWN_HOSTS.isSet()) {
            knownHostfile = CentralPAPropertyRepository.PA_RMISSH_KNOWN_HOSTS.getValue();
        }
        sshConfig.setKnowHostFile(knownHostfile);

        if (CentralPAPropertyRepository.PA_RMISSH_KEY_DIR.isSet()) {
            sshConfig.setKeyDir(CentralPAPropertyRepository.PA_RMISSH_KEY_DIR.getValue());
        }

        // Parse the ssh configuration file and store information in the sshConfig
        parser.parse(sshConfig);

        // Parse ProActive Property and store information in the sshConfig
        if (ProxyCommandConfig.PA_SSH_PROXY_GATEWAY.isSet()) {
            String property = ProxyCommandConfig.PA_SSH_PROXY_GATEWAY.getValue();
            parser.parseProperty(property, sshConfig);
        }

        sf = new SshRMIClientSocketFactory(sshConfig);
    }

    public RmiSshRemoteObjectFactory() {
        super(Constants.RMISSH_PROTOCOL_IDENTIFIER, RmiSshRemoteObjectImpl.class);
    }

    protected Registry getRegistry(URI url) throws RemoteException {
        return LocateRegistry.getRegistry(url.getHost(), url.getPort(), sf);
    }

}
