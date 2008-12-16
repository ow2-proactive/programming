package org.objectweb.proactive.ic2d.debug.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class DebuggerSocketServer extends AbstractDebuggerSocket {
    private static final long serialVersionUID = 8136088432502063948L;

    /** The socket server */
    protected ServerSocket serverSocket;

    public DebuggerSocketServer() {
    }

    /**
     * @see org.objectweb.proactive.ic2d.debug.connection.AbstractDebuggerSocket#setTarget(AbstractDebuggerSocket)
     *
     * @param target
     *            The target (must be a {@link DebuggerSocketClient})
     */
    public void setTarget(AbstractDebuggerSocket target) {
        if (target instanceof DebuggerSocketClient) {
            super.setTarget(target);
        } else {
            throw new UnsupportedOperationException("Target must be a DebuggerSocketClient");
        }
    }

    /**
     * @see org.objectweb.proactive.ic2d.debug.connection.AbstractDebuggerSocket#handshake(Socket)
     */
    public void handshake(Socket socket) throws IOException {
        Data handshake = new Data(14);
        socket.getInputStream().read(new byte[14], 0, 14);
        handshake.read("JDWP-Handshake");
        handshake.write(socket.getOutputStream());
        socket.getOutputStream().flush();
    }

    /**
     * Connect the server socket and wait for an external debugger connection
     */
    public void connect() {
        new Thread() {
            public void run() {
                try {
                    DebuggerSocketClient client = (DebuggerSocketClient) target;
                    Socket socket;
                    serverSocket = new ServerSocket(port);
                    port = serverSocket.getLocalPort();
                    if ((socket = serverSocket.accept()) != null) {
                        addConnection(socket);
                        client.connect();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * @see org.objectweb.proactive.ic2d.debug.connection.AbstractDebuggerSocket#closeConnection()
     */
    @Override
    public void closeConnection() {
        super.closeConnection();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
