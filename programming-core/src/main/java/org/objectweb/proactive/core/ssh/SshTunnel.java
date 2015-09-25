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
package org.objectweb.proactive.core.ssh;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.ProActiveRandom;

import com.trilead.ssh2.LocalPortForwarder;


/**
* This class represent a SSH Tunnel.
*
* It's a wrapper around a LocalPortForwarder and a SSHConnection object.
* When creating a tunnel {@link SSHConnectionCache} is used to reuse
* already existing SSH-2 connections.
*
* @see SSHConnection
* @see LocalPortForwarder
*/
public class SshTunnel {
    private LocalPortForwarder lpf;

    final private int localPort;
    final private int remotePort;
    final private String remoteHost;

    /** Open a SSH tunnel to remoteHost:remotePort over the SSH connection
     * 
     * A free port is automatically chosen as local port
     * 
     * @param connection The SSH connection to use to create the tunnel
     * @param remoteHost The remote host
     * @param remotePort The remote TCP port 
     * 
     * @throws IOException if the tunnel cannot be opened
     * 
     * @see {@link #getSshTunnel(SshConnection, String, int, int)}
     */
    static SshTunnel getSshTunnel(SshConnection connection, String remoteHost, int remotePort)
            throws IOException {
        int initialPort = ProActiveRandom.nextInt(65536 - 1024) + 1024;
        for (int localPort = (initialPort == 65535) ? 1024 : (initialPort + 1); localPort != initialPort; localPort = (localPort == 65535) ? 1024
                : (localPort + 1)) {

            try {
                logger.trace("initialPort:" + initialPort + " localPort:" + localPort);
                SshTunnel tunnel = new SshTunnel(connection, remoteHost, remotePort, localPort);
                return tunnel;
            } catch (BindException e) {
                // Try another port
                if (logger.isDebugEnabled()) {
                    logger.debug("The port " + localPort + " is not free");
                }
            }
        }

        // Looped all over the port range
        logger.error("No free local port can be found to establish a new SSH-2 tunnel to " + remoteHost +
            ":" + remotePort);
        throw new BindException("No free local port found");
    }

    /** Open a SSH tunnel between the localhost:localport and remoteHost:remotePort over the SSH connection
     * 
     * @param connection The SSH connection to use to create the tunnel
     * @param remoteHost The remote host
     * @param remotePort The remote TCP port 
     * @param localport  The local TCP port to bind to. 
     * 
     * @throws IOException if the tunnel cannot be opened
     * 
     * @see {@link #getSshTunnel(SshConnection, String, int)}
     */
    SshTunnel(SshConnection connection, String remoteHost, int remotePort, int localport) throws IOException {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.localPort = localport;
        InetSocketAddress isa = new InetSocketAddress(ProActiveInet.getInstance().getInetAddress(), localport);
        this.lpf = connection.getTrileadConnection().createLocalPortForwarder(isa, remoteHost, remotePort);

        if (logger.isDebugEnabled()) {
            logger.debug("Opened SSH tunnel localport=" + localPort + " distantHost=" + remoteHost +
                " distantPort=" + remotePort);
        }
    }

    @Override
    public String toString() {
        return "localport=" + localPort + " distanthost=" + remoteHost + " distantport=" + remotePort;
    }

    /** Close the tunnel
     * 
     * This method must be called before the tunnel is garbage collected to avoid a resource leak.
     * 
     * @throws Exception if the tunnel cannot be closed
     */
    public void close() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing tunnel from " +
                ProActiveInet.getInstance().getInetAddress().getHostAddress() + ":" + localPort + " to " +
                remoteHost + ":" + remotePort);
        }

        lpf.close();
    }

    /**
     * @return the local port of the tunnel
     */
    public int getPort() {
        return localPort;
    }

    /**
     * @return the remote host of the tunnel
     */
    public String getDistantHost() {
        return remoteHost;
    }

    /**
     * @return the remote port of the tunnel
     */
    public int getRemotePort() {
        return remotePort;
    }

    /** Grab a socket on the tunnel
     * 
     * @throws IOException if the socket cannot be opened (should never happen)
     */
    public Socket getSocket() throws IOException {
        InetSocketAddress address = new InetSocketAddress(ProActiveInet.getInstance().getInetAddress(),
            this.getPort());
        Socket socket = new Socket();
        socket.connect(address);
        return socket;
    }
}
