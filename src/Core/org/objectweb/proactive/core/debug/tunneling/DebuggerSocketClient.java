/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.tunneling;

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
     * @see org.eclipse.proactive.extendeddebugger.core.tunneling.ic2d.debug.connection.AbstractDebuggerSocket#handshake(Socket)
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
            System.err.println("********host: " + host);
            System.err.println("********port: " + port);
            connected = true;
            InetSocketAddress sa = new InetSocketAddress(host, port);
            Socket socket = new Socket();
            socket.connect(sa, 10000);
            addConnection(socket);
        }
    }

    /**
     * @see org.eclipse.proactive.extendeddebugger.core.tunneling.ic2d.debug.connection.AbstractDebuggerSocket#closeConnection()
     */
    @Override
    public void closeConnection() {
        if (target != null) {
            target.closeConnection();
        }
        super.closeConnection();
    }
}
