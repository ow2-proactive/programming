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
package org.objectweb.proactive.core.ssh;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.File;
import java.io.IOException;
import java.net.BindException;

import com.trilead.ssh2.Connection;


/**
 * A connection to a SSH-2 server
 *
 * This class is an interface between trilead and ProActive. It encapsulates
 * a com.trilead.ssh2.Connection and all tunnels and sessions related to this
 * connection
 */
public class SshConnection {
    final private String username;

    final private Connection connection;

    /**
     * Create a SSH connection to a SSH-2 server
     *
     * Only public key authentication is supported. All public keys found
     * in SshParameters.getSshKeyDirectory() are tried. The private key
     * must not be password protected.
     *
     * @param username  A string holding the username
     * @param hostname the hostname of the SSH-2 server
     * @param port port on the server, normally 22
     * @throws IOException If the connection can't be established an IOException
     * is thrown
     * @see SshParameters
     */
    public SshConnection(String username, String hostname, int port, String[] keys) throws IOException {
        this.username = username;

        if (keys.length == 0) {
            throw new IOException("Failed to open a SSH connection to " + username + "@" + hostname + ":" + port +
                                  ". No private keys");
        }

        Connection connection = null;
        for (String key : keys) {
            connection = new Connection(hostname, port);
            connection.connect();
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Trying private key " + key + " for " + username + "@" + hostname + ":" + port);
                }
                connection.authenticateWithPublicKey(username, new File(key), null);
                if (connection.isAuthenticationComplete()) {
                    break;
                } else {
                    connection.close();
                }
            } catch (IOException e) {
                // Gracefully handle password protected private key
                boolean isPasswordProtected = false;
                Throwable t = e;
                while (t != null && !isPasswordProtected) {
                    if (t.getMessage().contains("PEM is encrypted, but no password was specified") ||
                        // discard RSA-1 key
                        t.getMessage().contains("Invalid PEM structure")) {
                        isPasswordProtected = true;
                    }
                    t = t.getCause();
                }

                if (isPasswordProtected) {
                    logger.info("Private SSH key " + key + " is password protected or RSA-1. Ignore it !");
                    connection.close();
                } else {
                    throw e;
                }
            }
        }

        if (!connection.isAuthenticationComplete()) {
            // Connection cannot be opened
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("SSH Authentication failed for " + username + "@" + hostname + ":" + port + ". ");
                sb.append("Following SSH private keys have been tried:");
                for (String key : keys) {
                    sb.append("\t" + key);
                }
                logger.info(sb.toString());
            }
            throw new IOException("Failed to open a SSH connection to " + username + "@" + hostname + ":" + port);
        }

        if (logger.isDebugEnabled()) {
            logger.info("Opened an SSH connection to " + username + "@" + connection.getHostname() + ":" +
                        connection.getPort());
        }
        connection.setTCPNoDelay(true);
        this.connection = connection;
    }

    /** Open a SSH tunnel to remoteHost:remotePort over the SSH connection
     * 
     * A free port is automatically chosen as local port
     *
     * @param remoteHost The remote host
     * @param remotePort The remote TCP port 
     * 
     * @throws IOException if the tunnel cannot be opened
     */
    synchronized public SshTunnel getSSHTunnel(String remoteHost, int remotePort) throws IOException {
        return SshTunnel.getSshTunnel(this, remoteHost, remotePort);
    }

    /** Open a SSH tunnel to remoteHost:remotePort over the SSH connection
     *
     * @param remoteHost The remote host
     * @param remotePort The remote TCP port
     * @param localPort  The local TCP port to bind to
     * 
     * @throws IOException if the tunnel cannot be opened
     * @throws BindException if localPort is not free
     */
    synchronized public SshTunnel getSSHTunnel(String remoteHost, int remotePort, int localPort) throws IOException {
        return new SshTunnel(this, remoteHost, remotePort, localPort);
    }

    /** Close this connection
     *
     * The connection is closed even if a session or a tunnel is opened.
     */
    public void close() {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing SSH Connection" + toString());
        }
        this.connection.close();
    }

    @Override
    public String toString() {
        return this.username + "@" + connection.getHostname() + ":" + connection.getPort();
    }

    protected Connection getTrileadConnection() {
        return this.connection;
    }
}
