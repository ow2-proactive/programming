package org.objectweb.proactive.ic2d.debug.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


public class DebuggerSocketClient extends AbstractDebuggerSocket {
    private static final long serialVersionUID = -3968459071256257021L;

    /** Is the client connected ? */
    private boolean connected;

    /** Host for the connection of the socket */
    protected String host;

    public DebuggerSocketClient() {
    }

    /**
     * Return the host
     *
     * @return host The host
     */
    public String getHost() {
        return host;
    }

    /**
     * Set the host
     *
     * @param host
     *            The host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * @see org.objectweb.proactive.ic2d.debug.connection.AbstractDebuggerSocket#handshake(Socket)
     */
    public void handshake(Socket socket) throws IOException {
        Data handshake = new Data();
        handshake.read("JDWP-Handshake");
        handshake.write(socket.getOutputStream());
        socket.getOutputStream().flush();
        handshake.read(socket.getInputStream());
    }

    /**
     * Connect the client to a JVM
     *
     * @throws UnknownHostException
     * @throws IOException
     */
    public void connect() throws UnknownHostException, IOException {
        if (!connected) {
            connected = true;
            InetSocketAddress sa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            socket.connect(sa, 10000);
            addConnection(socket);
        }
    }

    /**
     * @see org.objectweb.proactive.ic2d.debug.connection.AbstractDebuggerSocket#closeConnection()
     */
    @Override
    public void closeConnection() {
        if (target != null) {
            target.closeConnection();
        }
        super.closeConnection();
    }
}
