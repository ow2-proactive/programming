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
package org.objectweb.proactive.extensions.pamr.router;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;


/**
 * An attachment belongs to a client as soon as a registration reply have been
 * send. One and only one attachment can exist for any client at a given time.
 * 
 * The attachment is in charge of two thing:
 * <ul>
 * <li>
 * <b>Front end</b>: it is a statefull entity which is in charge of reassembling
 * the message from chunks of data.</li>
 * <li>
 * <b>Back end</b>: it holds the only reference onto the SocketChannel
 * associated to the current tunnel.</li>
 * </ul>
 * 
 * @since ProActive 4.1.0
 */
public class Attachment {

    public static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER);

    /** The id of this attachment
     * 
     * Never used by any other object but can be useful when debugging
     */
    final private long attachmentId;

    /** The client */
    /*
     * Not final because can't be set in the constructor. Once this field is
     * set, it MUST NOT be updated again.
     */
    private Client client = null;

    /** The assembler is charge of reassembling the message for this given client */
    final private MessageAssembler assembler;

    /** The socket channel where to write for this given client */
    final private SocketChannel socketChannel;

    final private AtomicBoolean dtored;

    volatile private String agentHostname;

    public Attachment(RouterImpl router, SocketChannel socketChannel) {
        this.attachmentId = AttachmentIdGenerator.getId();
        this.assembler = new MessageAssembler(router, this);
        this.socketChannel = socketChannel;
        this.client = null;
        this.dtored = new AtomicBoolean(false);
    }

    /** Free the resources (sockets and file descriptor) associated to this attachment. 
     * 
     * Must be called before dereferencing an attachment.
     */
    public void dtor() {
        this.dtored.set(true);

        try {
            this.socketChannel.socket().close();
        } catch (IOException e) {
            ProActiveLogger.logEatedException(logger, e);
        } finally {
            try {
                this.socketChannel.close();
            } catch (IOException e) {
                ProActiveLogger.logEatedException(logger, e);
            }
        }
    }

    /*
     * To avoid file descriptor leak, we use finalize() to close the fds
     * even if dtor() has not been called.
     * 
     * fd are eventually closed when the GC is run
     */
    @Override
    protected void finalize() throws Throwable {
        if (this.dtored.get() == false) {
            logger.trace("File descriptor leak detected. Attachment.dtor() must be called. Please fill a bug report");
            this.dtor();
            super.finalize();
        }
    }

    public MessageAssembler getAssembler() {
        return assembler;
    }

    public long getAttachmentId() {
        return attachmentId;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("attachmentId= " + attachmentId + " ");
        sb.append("socketChannel=" + socketChannel.socket() + " ");
        sb.append("client=" + (client == null ? "unknown" : client.getAgentId()) + " ");
        return sb.toString();
    }

    public void setClient(Client client) {
        if (this.client == null) {
            this.client = client;
        } else {
            logger.warn("Attachement.setClientId() cannot be called twice. Current client: " + this.client +
                        ", discarded: " + client);
        }
    }

    public String getRemoteEndpointName() {
        SocketAddress sa = getRemoteEndpoint();
        if (sa == null) {
            // Troubleshooting: Unknown is sometime returned in production
            // We are trying to understand why.
            Socket s = socketChannel.socket();
            return "unknown (Socket:" + s + " isClosed:" + s.isClosed() + " isConnected: " + s.isConnected() + ")";
        } else {
            return sa.toString();
        }
    }

    public InetSocketAddress getRemoteEndpoint() {
        InetSocketAddress unknown = null;

        if (socketChannel == null)
            return unknown;

        SocketAddress sa = socketChannel.socket().getRemoteSocketAddress();
        if (sa == null)
            return unknown;
        if (sa instanceof InetSocketAddress) {
            // InetSocketAddress is THE implementation for SocketAddress
            return (InetSocketAddress) sa;
        }

        return unknown;

    }

    public void send(ByteBuffer byteBuffer) throws IOException {
        synchronized (this.socketChannel) {
            byteBuffer.clear();
            while (byteBuffer.remaining() > 0) {
                int bytes = this.socketChannel.write(byteBuffer);

                if (logger.isDebugEnabled()) {
                    String dstClient = this.client == null ? "unknown" : client.getAgentId().toString();
                    String remaining = byteBuffer.remaining() > 0 ? byteBuffer.remaining() + " remaining to send" : "";
                    logger.debug("Sent a " + bytes + " bytes message to client " + dstClient + " with " +
                                 this.socketChannel.socket() + ". " + remaining);
                }
            }
        }
    }

    static abstract private class AttachmentIdGenerator {
        static final private AtomicLong generator = new AtomicLong(0);

        static public long getId() {
            return generator.getAndIncrement();
        }
    }

    /** Close the underlying {@link SocketChannel}
     *
     * @throws IOException if the connexion cannot be closed
     */
    public void disconnect() throws IOException {
        this.socketChannel.close();
    }

    public String getAgentHostname() {
        return this.agentHostname;
    }

    public void setAgentHostname(String agentHostname) {
        if (this.agentHostname != null) {
            logger.warn("setAgentHostname is already set: " + this);
        }
        this.agentHostname = agentHostname;
    }
}
