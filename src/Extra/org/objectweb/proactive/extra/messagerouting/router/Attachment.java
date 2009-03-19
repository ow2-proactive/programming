/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


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
    public static final Logger logger = ProActiveLogger.getLogger(Loggers.FORWARDING_ROUTER);

    /** The id of this attachment
     * 
     * Never used by any other object but can be useful when debugging
     */
    final private long attachmentId;

    /** The client */
    /* Not final because can't be set in the constructor. Once this field is
     * set, it MUST NOT be updated again.
     */
    private Client client = null;

    /** The assembler is charge of reassembling the message for this given client */
    final private MessageAssembler assembler;

    /** The socket channel where to write for this given client */
    final private SocketChannel socketChannel;

    public Attachment(RouterImpl router, SocketChannel socketChannel) {
        this.attachmentId = AttachmentIdGenerator.getId();
        this.assembler = new MessageAssembler(router, this);
        this.socketChannel = socketChannel;
        this.client = null;
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

    public void send(ByteBuffer byteBuffer) throws IOException {
        /*
         * SocketChannel ARE thread safe. Extra locking to ensure serialization
         * of the calls is useless
         */
        byteBuffer.clear();
        while (byteBuffer.remaining() > 0) {
            int bytes = this.socketChannel.write(byteBuffer);

            if (logger.isDebugEnabled()) {
                String dstClient = this.client == null ? "unknown" : client.getAgentId().toString();
                String remaining = byteBuffer.remaining() > 0 ? byteBuffer.remaining() + " remaining to send"
                        : "";
                logger.debug("Sent a " + bytes + " bytes message to client " + dstClient + " with " +
                    this.socketChannel.socket() + ". " + remaining);
            }
        }
    }

    static abstract private class AttachmentIdGenerator {
        static final private AtomicLong generator = new AtomicLong(0);

        static public long getId() {
            return generator.getAndIncrement();
        }
    }
}