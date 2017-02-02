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
package org.objectweb.proactive.core.ssh.proxycommand;

import java.io.IOException;

import org.objectweb.proactive.core.ssh.SshConfig;
import org.objectweb.proactive.core.ssh.SshConnection;

import com.trilead.ssh2.Session;


/**
 * Implement the OpenSSH proxyCommand behavior
 */
public class SshProxyConnection extends SshConnection {

    final private static String FORWARDER_COMMAND = "nc";

    final private static String SSHCLIENTCOMMAND = "ssh";

    private String proxyCommandTemplate = "";

    /**
     * Because special processing is needed before constructor call
     *
     * @param hostGW
     *          The gateway to use for contacting the host (if needed)
     * @param localhostGW
     *          The gateway to use to relay outgoing connection (if needed)
     * @param config
     *          The information about the ssh connection
     * @param keys
     *          All the private keys to use
     *
     * @return
     *          A Ssh Connection which can provide proxyCommand sessions
     *
     * @throws IOException
     */
    public static SshProxyConnection getInstance(String gateway, String outGateway, SshConfig config)
            throws IOException {
        String username;
        int port;
        String proxyCommandTempl = FORWARDER_COMMAND + " " + "%h" + " " + "%p";
        String hostname = null;

        if (outGateway != null) {
            hostname = outGateway;
            if (gateway != null) {
                String user = config.getUsername(gateway);
                proxyCommandTempl = SSHCLIENTCOMMAND + " " + user + "@" + gateway + " " + FORWARDER_COMMAND + " " +
                                    "%h" + " " + "%p";
            }
        } else {
            if (gateway != null) {
                hostname = gateway;
            } else {
                return null;
            }
        }

        username = config.getUsername(hostname);
        port = config.getPort(hostname);
        String key[] = config.getPrivateKeyPath(hostname);
        return new SshProxyConnection(username, hostname, port, key, proxyCommandTempl);
    }

    private SshProxyConnection(String username, String hostname, int port, String[] keys, String proxyCommandTempl)
            throws IOException {
        super(username, hostname, port, keys);
        this.proxyCommandTemplate = proxyCommandTempl;
    }

    /**
     * Return an Ssh Session for contacting host on port which use the
     *     proxyCommand mechanism
     *
     * @param host
     *          the machine to contact
     * @param port
     *          the port to use
     */
    public SshProxySession getSession(String host, int port) throws IOException {
        String proxyCommand = proxyCommandTemplate;
        proxyCommand = proxyCommand.replace("%h", host);
        proxyCommand = proxyCommand.replace("%p", Integer.toString(port));
        Session sess = this.getTrileadConnection().openSession();
        sess.execCommand(proxyCommand);
        return new SshProxySession(sess);
    }
}
