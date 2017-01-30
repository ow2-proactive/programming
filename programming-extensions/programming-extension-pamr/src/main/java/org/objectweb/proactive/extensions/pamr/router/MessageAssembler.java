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

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;


/** Reassemble messages from data chunks
 * 
 * {@link RouterImpl} reads chunk of data from a {@link SocketChannel}. We have to 
 * reassemble the messages from the data chunks. Each {@link SocketChannel} correspond 
 * to one and only one client, so we just have to use the message length and protocol id
 * to assemble the messages.
 * 
 * If an invalid message is detected (wrong message length or protocol id) the socket 
 * channel is closed
 * 
 * @since ProActive 4.1.0
 */
public class MessageAssembler {
    public static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER);

    final private RouterInternal router;

    final private Attachment attachment;

    /** The current incomplete message
     * 
     * null when the length and the protocol id of the current message are
     * still unknown. 
     */
    private ByteBuffer currentMessage;

    /** Length and protocol id of the current message 
     * 
     * null when a message has been assembled and no data is available
     */
    private LengthAndProto lengthAndProto;

    public MessageAssembler(RouterInternal router, Attachment attachment) {
        this.router = router;
        this.attachment = attachment;

        this.currentMessage = null;
        this.lengthAndProto = null;
    }

    synchronized public void pushBuffer(ByteBuffer buffer) throws MalformedMessageException {
        // PROACTIVE-869: Update last seen to avoid disconnection due to large message

        Client client = this.attachment.getClient();
        if (client != null) {
            client.updateLastSeen();
        }

        while (buffer.remaining() != 0) {

            if (this.currentMessage == null) {

                if (this.lengthAndProto == null) {
                    this.lengthAndProto = new LengthAndProto();
                }

                while (buffer.remaining() > 0 && !lengthAndProto.isReady()) {
                    lengthAndProto.push(buffer.get());
                }

                if (lengthAndProto.isReady()) {

                    int proto = lengthAndProto.getProto();
                    int l = this.lengthAndProto.getLength();

                    // Check the protocol is correct. Otherwise something fucked up
                    // and the connection is closed to avoid a disaster
                    if (proto != Message.PROTOV2) {
                        logger.error("Invalid protocol ID received from " + attachment + ": expected=" +
                                     Message.PROTOV2 + " received=" + proto);
                        throw new MalformedMessageException("Invalid protocol ID");
                    } else if (l < Message.Field.getTotalOffset()) {
                        logger.error("Invalid message length received from " + attachment + ": " + l);
                        throw new MalformedMessageException("Invalid message length");
                    }

                    // Allocate a buffer for the reassembled message
                    currentMessage = ByteBuffer.allocate(l);

                    // Buffer position is no more 0, we copy the data that have been read
                    // by the previous loop
                    currentMessage.putInt(l);
                    currentMessage.putInt(proto);
                } else {
                    // Length is still not available, it means that buffer.remaing() has been reached
                    // We can safely exit the loop
                    break;
                }
            }

            // This point can only be reached if length & proto have been read
            // currentMessage is not null

            // Number of bytes missing to complete the currentMessage
            int missingBytes = currentMessage.remaining();
            // Number of bytes available in the buffer
            int availableBytes = buffer.remaining();

            int toCopy = missingBytes > availableBytes ? availableBytes : missingBytes;

            // Don't use put(ByteBuffer) it does NOT use the limit
            currentMessage.put(buffer.array(), buffer.position(), toCopy);
            buffer.position(buffer.position() + toCopy);

            // Checks if current message is complete
            if (currentMessage.remaining() == 0) {
                if (logger.isDebugEnabled()) {
                    String dest = this.attachment.getClient() == null ? " unknown"
                                                                      : this.attachment.getClient().toString();
                    logger.debug("Assembled one message for client " + dest);
                }

                this.router.handleAsynchronously(currentMessage, this.attachment);
                this.currentMessage = null;
                this.lengthAndProto = null;
            }
        }

    }

    private static class LengthAndProto {
        static private int SIZE = Message.Field.LENGTH.getLength() + Message.Field.PROTO_ID.getLength();

        private byte[] buf;

        private int index;

        protected LengthAndProto() {
            buf = new byte[SIZE];
            index = 0;
        }

        protected void push(byte b) {
            buf[index++] = b;
        }

        protected boolean isReady() {
            return index == SIZE;
        }

        protected int getLength() {
            return Message.readLength(buf, 0);
        }

        protected int getProto() {
            return Message.readProtoID(buf, 0);
        }
    }
}
