/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
import org.objectweb.proactive.core.ssh.SshRMIClientSocketFactory;


public class RmiSshRemoteObjectFactory extends AbstractRmiRemoteObjectFactory {
    final static SshRMIClientSocketFactory sf;

    static {
        SshConfig sshConfig = new SshConfig();
        sshConfig.setTryPlainSocket(CentralPAPropertyRepository.PA_RMISSH_TRY_NORMAL_FIRST.isTrue());

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

        String keyDir = System.getProperty("user.home") + File.separator + ".ssh" + File.separator;
        if (CentralPAPropertyRepository.PA_RMISSH_KEY_DIR.isSet()) {
            keyDir = CentralPAPropertyRepository.PA_RMISSH_KEY_DIR.getValue();
        }
        sshConfig.setKeyDir(keyDir);

        if (CentralPAPropertyRepository.PA_RMISSH_REMOTE_PORT.isSet()) {
            int port = CentralPAPropertyRepository.PA_RMISSH_REMOTE_PORT.getValue();
            sshConfig.setPort(port);
        }

        if (CentralPAPropertyRepository.PA_RMISSH_REMOTE_USERNAME.isSet()) {
            String username = CentralPAPropertyRepository.PA_RMISSH_REMOTE_USERNAME.getValue();
            sshConfig.setUsername(username);
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
