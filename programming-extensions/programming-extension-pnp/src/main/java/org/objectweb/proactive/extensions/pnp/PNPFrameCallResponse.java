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
package org.objectweb.proactive.extensions.pnp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.objectweb.proactive.extensions.pnp.exception.PNPMalformedMessageException;


/** A PNP frame for a call response
 *
 * A call response is sent by the server to the client. The payload depends of the call payload.
 *
 * A call response is identified by an unique call id (the id of the call).
 * @since ProActive 4.3.0
 */
class PNPFrameCallResponse extends PNPFrame {
    /** The offset of the payload */
    static final private int RESPONSE_MESSAGE_HEADER_LENGTH = PNPFrame.Field.getTotalOffset() + Field.getTotalOffset();

    /**
     * Fields of the {@link PNPFrameCall} header.
     *
     * These fields are put after the {@link PNPFrame} header.
     */
    public enum Field {
        /** The unique id of the call*/
        CALL_ID(8, Long.class);

        private int length;

        private int myOffset = 0;

        static private int totalOffset = 0;

        /* type is only informative */
        private Field(int length, Class<?> type) {
            this.length = length;
        }

        /** Length of the field in bytes */
        public long getLength() {
            return this.length;
        }

        /** Offset of the field in the message */
        public int getOffset() {
            /*
             * WARNING: RACY SINGLE-CHECK INTIALIZATION
             *
             * This method relies on the Java Memory Model specification to perform
             * a racy single-check initialization.
             * DO NOT CHANGE THIS METHOD IF YOU DON'T KNOW WHAT IT IS.
             *
             * See: Effective Java, chapter 10, Item 71: Lazy initialization
             * See: http://jeremymanson.blogspot.com/2008/12/benign-data-races-in-java.html
             */
            int tmpOffset = myOffset;
            if (tmpOffset == 0) {
                for (Field field : values()) {
                    if (field.ordinal() < this.ordinal()) {
                        tmpOffset += field.getLength();
                    }
                }

                myOffset = tmpOffset;
            }

            return myOffset;
        }

        /** Length of the fields defined by {@link PNPFrameCall} */
        static public int getTotalOffset() {
            /*
             * WARNING: RACY SINGLE-CHECK INTIALIZATION
             *
             * This method relies on the Java Memory Model specification to perform
             * a racy single-check initialization.
             * DO NOT CHANGE THIS METHOD IF YOU DON'T KNOW WHAT IT IS.
             *
             * See: Effective Java, chapter 10, Item 71: Lazy initialization
             * See: http://jeremymanson.blogspot.com/2008/12/benign-data-races-in-java.html
             */
            int tmpOffset = totalOffset;
            if (tmpOffset == 0) {
                for (Field field : values()) {
                    tmpOffset += field.getLength();
                }

                totalOffset = tmpOffset;
            }

            return totalOffset;
        }

        @Override
        public String toString() {
            switch (this) {
                case CALL_ID:
                    return "CALL_ID";
                default:
                    return super.toString();
            }
        }
    }

    final protected long callId;

    final protected byte[] payload;

    final protected ChannelBuffer payloadChannelBuffer;

    public long getCallId() {
        return callId;
    }

    /**
     * Create a {@link PNPFrameCall}
     *
     * All the parameters must be non null
     *
     */
    protected PNPFrameCallResponse(long callId, byte[] payload) {
        super(PNPFrame.MessageType.CALL_RESPONSE);

        this.callId = callId;
        this.payload = payload;
        this.payloadChannelBuffer = null;
    }

    /**
     * Create a {@link PNPFrameCall} from a byte array
     *
     * @param buf
     *            a buffer which contains a message
     * @param offset
     *            the offset at which the message begins
     * @throws PNPMalformedMessageException
     *             If the buffer does not contain a valid message (proto ID,
     *             length etc.)
     */
    protected PNPFrameCallResponse(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        super(buf, offset);

        this.callId = readCallId(buf, offset);

        buf.readerIndex(0);
        int datalength = buf.readableBytes() - RESPONSE_MESSAGE_HEADER_LENGTH;
        this.payloadChannelBuffer = buf.slice(RESPONSE_MESSAGE_HEADER_LENGTH, datalength);
        this.payload = null;
    }

    private long readCallId(ChannelBuffer buf, int offset) {
        long callId = TypeHelper.channelBufferToLong(buf,
                                                     offset + PNPFrame.Field.getTotalOffset() +
                                                          Field.CALL_ID.getOffset());

        return callId;
    }

    /** Return the payload of this message */
    public InputStream getPayload() {
        if (this.payload == null) {
            this.payloadChannelBuffer.readerIndex(0);
            return new ChannelBufferInputStream(this.payloadChannelBuffer);
        } else {
            return new ByteArrayInputStream(this.payload);
        }
    }

    @Override
    public String toString() {
        byte[] buf = this.payload;
        if (buf == null) {
            buf = new byte[Math.min(this.payloadChannelBuffer.readableBytes(), 64)];
            this.payloadChannelBuffer.getBytes(0, buf, 0, buf.length);
        }

        int payloadLenght;
        if (this.payload == null) {
            this.payloadChannelBuffer.readerIndex(0);
            payloadLenght = this.payloadChannelBuffer.readableBytes();
        } else {
            payloadLenght = this.payload.length;
        }

        return super.toString() + Field.CALL_ID.toString() + ":" + this.callId + "; PAYLOAD:(" + payloadLenght + ")" +
               byteArrayToHexString(buf, 64);
    }

    @Override
    public ChannelBuffer toChannelBuffer() {

        byte[] header = new byte[RESPONSE_MESSAGE_HEADER_LENGTH];
        super.writeHeader(header, 0);
        TypeHelper.longToByteArray(this.callId, header, PNPFrame.Field.getTotalOffset() + Field.CALL_ID.getOffset());

        return ChannelBuffers.wrappedBuffer(header, this.payload);
    }
}
