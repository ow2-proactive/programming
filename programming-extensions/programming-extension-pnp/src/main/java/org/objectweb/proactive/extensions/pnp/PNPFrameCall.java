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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pnp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.objectweb.proactive.extensions.pnp.exception.PNPMalformedMessageException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.buffer.ChannelBuffers;


/** A PNP frame for a call
 *
 * A call is sent be the client to the server. The payload is a {@link PNPROMessage} that
 * is executed by the remote server.
 *
 * A call is identified by an unique call id. To nicely handle network failure, an heartbeat period
 * can be specified.
 *
 * @since ProActive 4.3.0
 */
class PNPFrameCall extends PNPFrame {
    /** The offset of the payload */
    static final private int REQUEST_MESSAGE_HEADER_LENGTH = PNPFrame.Field.getTotalOffset() +
        Field.getTotalOffset();

    /** Fields of the {@link PNPFrameCall} header.
     *
     * These fields are put after the {@link PNPFrame} header.
     */
    public enum Field {
        /** An unique identifier for a call */
        CALL_ID(8, Long.class),
        /** Is this a one way call ?
         *
         * One way calls does not expect {@link PNPFrameCallResponse} and there
         * is no way to know if the frame has been received or not
         */
        ONE_WAY(4, Integer.class), // Could be a bool but int to avoid padding issue
        /** The heartbeat period.
         *
         * If >0 the server must send an heartbeat on the channel at least every
         * {@link #HEARTBEAT_PERIOD}
         */
        HEARTBEAT_PERIOD(8, Long.class),
        /** The call service timeout */
        SERVICE_TIMEOUT(8, Long.class);

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
            /* WARNING: RACY SINGLE-CHECK INTIALIZATION
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
            /* WARNING: RACY SINGLE-CHECK INTIALIZATION
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
                case HEARTBEAT_PERIOD:
                    return "EARTHBEAT_PERID";
                case ONE_WAY:
                    return "ONE_WAY";
                case SERVICE_TIMEOUT:
                    return "SERVICE_TIMEOUT";
                default:
                    return super.toString();
            }
        }
    }

    final protected boolean oneWay;
    final protected long hearthbeatPeriod;
    final protected long serviceTimeout;
    final protected long callId;
    final protected byte[] payload;
    final protected ChannelBuffer payloadChannelBuffer;

    /**
     * Create a {@link PNPFrameCall}
     *
     * All the parameters must be non null
     */
    public PNPFrameCall(long callId, boolean oneWay, long hearthbeatPeriod, long serviceTimeout,
            byte[] payload) {
        super(PNPFrame.MessageType.CALL);

        this.callId = callId;
        this.oneWay = oneWay;
        this.hearthbeatPeriod = hearthbeatPeriod;
        this.serviceTimeout = serviceTimeout;
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
    protected PNPFrameCall(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        super(buf, offset);

        this.callId = readCallId(buf, offset);
        this.oneWay = readOneWay(buf, offset);
        this.hearthbeatPeriod = readHearthbeatPeriod(buf, offset);
        this.serviceTimeout = readServiceTimeout(buf, offset);

        buf.resetReaderIndex();
        int datalength = buf.readableBytes() - REQUEST_MESSAGE_HEADER_LENGTH;
        if (datalength <= 0) {
            throw new PNPMalformedMessageException("Invalid frame call: no payload");
        }
        this.payloadChannelBuffer = buf.slice(REQUEST_MESSAGE_HEADER_LENGTH, datalength);
        this.payload = null;
    }

    private long readServiceTimeout(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        long timeout = TypeHelper.channelBufferToLong(buf, offset + PNPFrame.Field.getTotalOffset() +
            Field.SERVICE_TIMEOUT.getOffset());

        if (timeout < 0) {
            throw new PNPMalformedMessageException("Invalid " + Field.SERVICE_TIMEOUT + " value: " + timeout);
        }

        return timeout;
    }

    private long readHearthbeatPeriod(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        long heartbeat = TypeHelper.channelBufferToLong(buf, offset + PNPFrame.Field.getTotalOffset() +
            Field.HEARTBEAT_PERIOD.getOffset());

        if (heartbeat < 0) {
            throw new PNPMalformedMessageException("Invalid " + Field.HEARTBEAT_PERIOD + " value: " +
                heartbeat);
        }

        return heartbeat;
    }

    private boolean readOneWay(ChannelBuffer buf, int offset) {
        int oneWay = TypeHelper.channelBufferToInt(buf, offset + PNPFrame.Field.getTotalOffset() +
            Field.ONE_WAY.getOffset());

        return oneWay != 0;
    }

    private long readCallId(ChannelBuffer buf, int offset) {
        long callId = TypeHelper.channelBufferToLong(buf, offset + PNPFrame.Field.getTotalOffset() +
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

        return super.toString() + Field.CALL_ID.toString() + ":" + this.callId + ";" +
            Field.HEARTBEAT_PERIOD.toString() + ":" + this.hearthbeatPeriod + ";" + Field.ONE_WAY.toString() +
            ":" + this.oneWay + ";" + Field.SERVICE_TIMEOUT.toString() + ":" + this.serviceTimeout +
            "; PAYLOAD" + ":(" + payloadLenght + ")" + byteArrayToHexString(buf, 64);
    }

    @Override
    public ChannelBuffer toChannelBuffer() {

        byte[] header = new byte[REQUEST_MESSAGE_HEADER_LENGTH];
        super.writeHeader(header, 0);
        TypeHelper.longToByteArray(this.callId, header,
                PNPFrame.Field.getTotalOffset() + Field.CALL_ID.getOffset());
        TypeHelper.intToByteArray(this.oneWay ? 1 : 0, header, PNPFrame.Field.getTotalOffset() +
            Field.ONE_WAY.getOffset());
        TypeHelper.longToByteArray(this.hearthbeatPeriod, header, PNPFrame.Field.getTotalOffset() +
            Field.HEARTBEAT_PERIOD.getOffset());
        TypeHelper.longToByteArray(this.serviceTimeout, header, PNPFrame.Field.getTotalOffset() +
            Field.SERVICE_TIMEOUT.getOffset());

        return ChannelBuffers.wrappedBuffer(header, this.payload);
    }

    public boolean isOneWay() {
        return this.oneWay;
    }

    public long getHearthbeatPeriod() {
        return hearthbeatPeriod;
    }

    public long getServiceTimeout() {
        return serviceTimeout;
    }

    public long getCallId() {
        return callId;
    }
}
