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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;

import javax.net.SocketFactory;

import org.objectweb.proactive.core.config.PAPropertyInteger;
import org.objectweb.proactive.core.config.PAPropertyString;
import org.objectweb.proactive.core.ssh.SshConfigFileParser.SshToken;


public class SshTunnelSocketFactory extends SocketFactory {

    private final SshConfig config;

    private final SshTunnelPool tp;

    public SshTunnelSocketFactory(PAPropertyString keyDir, PAPropertyString knownHosts,
            PAPropertyInteger remotePort, PAPropertyString remoteUserName) {
        this.config = new SshConfig();

        this.config.setTryPlainSocket(false);
        this.config.setGcInterval(60000);

        if (keyDir.isSet()) {
            String dir = keyDir.getValue();
            this.config.setKeyDir(dir);
        }
        if (knownHosts.isSet()) {
            String knownhost = knownHosts.getValue();
            this.config.setKnowHostFile(knownhost);
        }
        if (remotePort.isSet()) {
            int port = remotePort.getValue();
            this.config.addDefaultHostInformation(SshToken.PORT, String.valueOf(port));
        }
        if (remoteUserName.isSet()) {
            String username = remoteUserName.getValue();
            this.config.addDefaultHostInformation(SshToken.USERNAME, username);
        }

        this.tp = new SshTunnelPool(this.config);
    }

    @Override
    public Socket createSocket() throws IOException {
        Socket socket = new Socket() {

            private Socket realSocket;

            @Override
            public void connect(SocketAddress endpoint) throws IOException {
                createRealSocket(endpoint);
            }

            @Override
            public void connect(SocketAddress endpoint, int timeout) throws IOException {
                createRealSocket(endpoint);
            }

            @Override
            public void bind(SocketAddress bindpoint) throws IOException {
                if (realSocket != null) {
                    realSocket.bind(bindpoint);
                } else {
                    super.bind(bindpoint);
                }
            }

            private void createRealSocket(SocketAddress endpoint) throws IOException {
                if (realSocket != null) {
                    throw new IllegalStateException("Already connected");
                }
                // propagate socket parameters if case they were set for 'this' instance 
                InetSocketAddress addr = (InetSocketAddress) endpoint;
                realSocket = tp.getSocket(addr.getHostName(), addr.getPort());
                realSocket.setKeepAlive(getKeepAlive());
                realSocket.setOOBInline(getOOBInline());
                realSocket.setReceiveBufferSize(getReceiveBufferSize());
                realSocket.setReuseAddress(getReuseAddress());
                realSocket.setSendBufferSize(getSendBufferSize());
                realSocket.setSoTimeout(getSoTimeout());
                realSocket.setTcpNoDelay(getTcpNoDelay());
                realSocket.setTrafficClass(getTrafficClass());
            }

            @Override
            public InetAddress getInetAddress() {
                if (realSocket != null) {
                    return realSocket.getInetAddress();
                } else {
                    return super.getInetAddress();
                }
            }

            @Override
            public InetAddress getLocalAddress() {
                if (realSocket != null) {
                    return realSocket.getLocalAddress();
                } else {
                    return super.getLocalAddress();
                }
            }

            @Override
            public int getPort() {
                if (realSocket != null) {
                    return realSocket.getPort();
                } else {
                    return super.getPort();
                }
            }

            @Override
            public int getLocalPort() {
                if (realSocket != null) {
                    return realSocket.getLocalPort();
                } else {
                    return super.getLocalPort();
                }
            }

            @Override
            public SocketAddress getRemoteSocketAddress() {
                if (realSocket != null) {
                    return realSocket.getRemoteSocketAddress();
                } else {
                    return super.getRemoteSocketAddress();
                }
            }

            @Override
            public SocketAddress getLocalSocketAddress() {
                if (realSocket != null) {
                    return realSocket.getLocalSocketAddress();
                } else {
                    return super.getLocalSocketAddress();
                }
            }

            @Override
            public SocketChannel getChannel() {
                if (realSocket != null) {
                    return realSocket.getChannel();
                } else {
                    return super.getChannel();
                }
            }

            @Override
            public InputStream getInputStream() throws IOException {
                if (realSocket != null) {
                    return realSocket.getInputStream();
                } else {
                    return super.getInputStream();
                }
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                if (realSocket != null) {
                    return realSocket.getOutputStream();
                } else {
                    return super.getOutputStream();
                }
            }

            @Override
            public void setTcpNoDelay(boolean on) throws SocketException {
                if (realSocket != null) {
                    realSocket.setTcpNoDelay(on);
                } else {
                    super.setTcpNoDelay(on);
                }
            }

            @Override
            public boolean getTcpNoDelay() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getTcpNoDelay();
                } else {
                    return super.getTcpNoDelay();
                }
            }

            @Override
            public void setSoLinger(boolean on, int linger) throws SocketException {
                if (realSocket != null) {
                    realSocket.setSoLinger(on, linger);
                } else {
                    super.setSoLinger(on, linger);
                }
            }

            @Override
            public int getSoLinger() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getSoLinger();
                } else {
                    return super.getSoLinger();
                }
            }

            @Override
            public void sendUrgentData(int data) throws IOException {
                if (realSocket != null) {
                    realSocket.sendUrgentData(data);
                } else {
                    super.sendUrgentData(data);
                }
            }

            @Override
            public void setOOBInline(boolean on) throws SocketException {
                if (realSocket != null) {
                    realSocket.setOOBInline(on);
                } else {
                    super.setOOBInline(on);
                }
            }

            @Override
            public boolean getOOBInline() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getOOBInline();
                } else {
                    return super.getOOBInline();
                }
            }

            @Override
            public void setSoTimeout(int timeout) throws SocketException {
                if (realSocket != null) {
                    realSocket.setSoTimeout(timeout);
                } else {
                    super.setSoTimeout(timeout);
                }
            }

            @Override
            public int getSoTimeout() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getSoTimeout();
                } else {
                    return super.getSoTimeout();
                }
            }

            @Override
            public void setSendBufferSize(int size) throws SocketException {
                if (realSocket != null) {
                    realSocket.setSendBufferSize(size);
                } else {
                    super.setSendBufferSize(size);
                }
            }

            @Override
            public int getSendBufferSize() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getSendBufferSize();
                } else {
                    return super.getSendBufferSize();
                }
            }

            @Override
            public void setReceiveBufferSize(int size) throws SocketException {
                if (realSocket != null) {
                    realSocket.setReceiveBufferSize(size);
                } else {
                    super.setReceiveBufferSize(size);
                }
            }

            @Override
            public int getReceiveBufferSize() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getReceiveBufferSize();
                } else {
                    return super.getReceiveBufferSize();
                }
            }

            @Override
            public void setKeepAlive(boolean on) throws SocketException {
                if (realSocket != null) {
                    realSocket.setKeepAlive(on);
                } else {
                    super.setKeepAlive(on);
                }
            }

            @Override
            public boolean getKeepAlive() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getKeepAlive();
                } else {
                    return super.getKeepAlive();
                }
            }

            @Override
            public void setTrafficClass(int tc) throws SocketException {
                if (realSocket != null) {
                    realSocket.setTrafficClass(tc);
                } else {
                    super.setTrafficClass(tc);
                }
            }

            @Override
            public int getTrafficClass() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getTrafficClass();
                } else {
                    return super.getTrafficClass();
                }
            }

            @Override
            public void setReuseAddress(boolean on) throws SocketException {
                if (realSocket != null) {
                    realSocket.setReuseAddress(on);
                } else {
                    super.setReuseAddress(on);
                }
            }

            @Override
            public boolean getReuseAddress() throws SocketException {
                if (realSocket != null) {
                    return realSocket.getReuseAddress();
                } else {
                    return super.getReuseAddress();
                }
            }

            @Override
            public void close() throws IOException {
                if (realSocket != null) {
                    realSocket.close();
                } else {
                    super.close();
                }
            }

            @Override
            public void shutdownInput() throws IOException {
                if (realSocket != null) {
                    realSocket.shutdownInput();
                } else {
                    super.shutdownInput();
                }
            }

            @Override
            public void shutdownOutput() throws IOException {
                if (realSocket != null) {
                    realSocket.shutdownOutput();
                } else {
                    super.shutdownOutput();
                }
            }

            @Override
            public String toString() {
                if (realSocket != null) {
                    return realSocket.toString();
                } else {
                    return super.toString();
                }
            }

            @Override
            public boolean isConnected() {
                if (realSocket != null) {
                    return realSocket.isConnected();
                } else {
                    return super.isConnected();
                }
            }

            @Override
            public boolean isBound() {
                if (realSocket != null) {
                    return realSocket.isBound();
                } else {
                    return super.isBound();
                }
            }

            @Override
            public boolean isClosed() {
                if (realSocket != null) {
                    return realSocket.isClosed();
                } else {
                    return super.isClosed();
                }
            }

            @Override
            public boolean isInputShutdown() {
                if (realSocket != null) {
                    return realSocket.isInputShutdown();
                } else {
                    return super.isInputShutdown();
                }
            }

            @Override
            public boolean isOutputShutdown() {
                if (realSocket != null) {
                    return realSocket.isOutputShutdown();
                } else {
                    return super.isOutputShutdown();
                }
            }

            @Override
            public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
                if (realSocket != null) {
                    realSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
                } else {
                    super.setPerformancePreferences(connectionTime, latency, bandwidth);
                }
            }

        };
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return createSocket(address.getHostName(), port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return createSocket(host.getHostName(), port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return createSocket(host, port);
    }

    public Socket createSocket(String host, int port) throws IOException {
        return tp.getSocket(host, port);
    }

}
