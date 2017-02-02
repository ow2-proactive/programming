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
package org.objectweb.proactive.extensions.pamr.client;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;


/** The entity in charge to send and receive data on the wire
 *
 * Tunnel manager goal is to keep the connection to the router open and
 * to send and receive the data. It is the lowest level entity involved in
 * Message Routing
 * 
 * Error handling performed by higher level component. If the connection goes down
 * or something bad happens then an IOException is thrown. It is the responsibility
 * of the caller to create a new tunnel
 * 
 * @since ProActive 4.1.0
 */
public class Tunnel {
    static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_CLIENT_TUNNEL);

    final private Socket socket;

    final private BufferedInputStream bis;

    final private String debugString;

    public Tunnel(Socket socket) throws IOException {
        this.socket = socket;
        this.bis = new BufferedInputStream(socket.getInputStream());

        this.socket.setKeepAlive(true);

        this.debugString = "local=" + socket.getLocalAddress() + " remote=" + socket.getRemoteSocketAddress();
        if (logger.isDebugEnabled()) {
            logger.debug("Opened a new tunnel to router: " + this.debugString);
        }
    }

    synchronized public void write(byte[] buf) throws IOException {
        this.write(buf, 0, buf.length, 0);
    }

    synchronized public void write(byte[] buf, long timeout) throws IOException {
        this.write(buf, 0, buf.length, timeout);
    }

    synchronized public void write(byte[] buf, int offset, int length, long timeout) throws IOException {
        this.socket.getOutputStream().write(buf, offset, length);
        this.socket.getOutputStream().flush();
    }

    public void read(byte[] buf) throws IOException {
        this.read(buf, 0, buf.length, 0);
    }

    public void read(byte[] buf, long timeout) throws IOException {
        this.read(buf, 0, buf.length, timeout);
    }

    public void read(byte[] buf, int offset, int length, long timeout) throws IOException {
        int read = 0;
        while (read < length) {
            int retVal = bis.read(buf, offset + read, length - read);
            if (retVal == -1) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Tunnel " + this.debugString + " got EOF");
                }
                throw new IOException("Failed to read " + length + "byte. EOF reached after " + read + " bytes");
            }

            read += retVal;
            if (logger.isDebugEnabled()) {
                logger.debug("" + read + " bytes have been read on, " + this.debugString + " " + (length - read) +
                             " remaining");
            }
        }
    }

    public boolean shouldWork() {
        if (this.socket == null)
            return false;

        return this.socket.isConnected();
    }

    public synchronized void shutdown() {
        try {
            this.socket.close();
        } catch (IOException e) {
            // We were trying to close a socket gracefully
            // Can't do anything better than log the event and ignore the error
            logger.info("Failed to close the socket " + this.socket);
        }
    }

    public byte[] readMessage() throws IOException {
        byte[] arrayLength = new byte[4];

        // Read the first four bytes (message length)
        this.read(arrayLength);
        int messageLength = TypeHelper.byteArrayToInt(arrayLength, 0);

        byte[] msg = new byte[messageLength];
        System.arraycopy(arrayLength, 0, msg, 0, arrayLength.length);

        this.read(msg, 4, messageLength - 4, 0);
        return msg;
    }

    int getLocalPort() {
        return this.socket.getLocalPort();
    }

    String getLocalAddress() {
        return this.socket.getLocalAddress().toString();
    }

    int getRemotePort() {
        return this.socket.getPort();
    }

    String getRemoteAddress() {
        return this.socket.getInetAddress().toString();
    }

    public void setSoTimeout(int l) {
        try {
            this.socket.setSoTimeout(l);
        } catch (SocketException e) {
            logger.warn("Failed to set the socket timeout on PAMR tunnel", e);
        }
    }
}
