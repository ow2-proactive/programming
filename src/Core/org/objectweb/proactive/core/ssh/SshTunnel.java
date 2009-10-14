package org.objectweb.proactive.core.ssh;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.ProActiveRandom;

import com.trilead.ssh2.LocalPortForwarder;


/**
*
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

    private int localPort;
    final private int distantPort;
    final private String distantHost;

    /**
     * Open a SSH Tunnel between localhost and distantHost.
     *
     * If no SSH Connection to distantHost exists; a new Connection is opened.
     * Otherwise the connection is reused.
     *
     * @param distantHost
     *            the name of the machine to which a tunnel must be established.
     * @param distantPort
     *            the port number on the distant machine to which a tunnel must
     *            be established
     * @throws IOException
     *             an exception is thrown if either the authentication or the
     *             tunnel establishment fails.
     */
    SshTunnel(SshConnection connection, String distantHost, int distantPort) throws IOException {
        this.distantHost = distantHost;
        this.distantPort = distantPort;

        int initialPort = ProActiveRandom.nextInt(65536 - 1024) + 1024;
        for (localPort = (initialPort == 65535) ? 1024 : (initialPort + 1); localPort != initialPort; localPort = (localPort == 65535) ? 1024
                : (localPort + 1)) {
            logger.trace("initialPort:" + initialPort + " localPort:" + localPort);
            try {
                lpf = connection.getTrileadConnection().createLocalPortForwarder(localPort, distantHost,
                        distantPort);
                if (logger.isDebugEnabled()) {
                    logger.debug("Opened SSH tunnel localport=" + localPort + " distantHost=" + distantHost +
                        " distantPort=" + distantPort);
                }
                return;
            } catch (BindException e) {
                // Try another port
                if (logger.isDebugEnabled()) {
                    logger.debug("The port " + localPort + " is not free");
                }
            }
        }

        // Looped all over the port range
        logger.error("No free local port can be found to establish a new SSH-2 tunnel to " + distantHost +
            ":" + distantPort);
        throw new BindException("No free local port found");
    }

    @Override
    public String toString() {
        return "localport=" + localPort + " distanthost=" + distantHost + " distantport=" + distantPort;
    }

    public void close() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Closing tunnel from " +
                ProActiveInet.getInstance().getInetAddress().getHostAddress() + ":" + localPort + " to " +
                distantHost + ":" + distantPort);
        }

        lpf.close();
    }

    /**
     * This method returns the local port number which can be used to access
     * this tunnel. This method cannot fail.
     */
    public int getPort() {
        return localPort;
    }

    public InetAddress getInetAddress() throws java.net.UnknownHostException {
        return InetAddress.getByName(distantHost);
    }

    public String getDistantHost() {
        return distantHost;
    }

    public int getDistantPort() {
        return distantPort;
    }

    public Socket getSocket() throws IOException {
        InetSocketAddress address = new InetSocketAddress(this.getPort());
        Socket socket = new Socket();
        socket.connect(address);
        return socket;
    }
}
