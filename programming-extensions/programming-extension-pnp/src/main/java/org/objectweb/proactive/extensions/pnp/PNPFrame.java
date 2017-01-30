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

import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.objectweb.proactive.extensions.pnp.exception.PNPMalformedMessageException;


/** A PNP Frame
 *
 * All the PNP Frames inherit from this class and share its header
 *
 * @since ProActive 4.3.0
 */
abstract class PNPFrame {
    static final short PNP_MAGIC_KEY = 543;

    /** Protocol version implemented by this class */
    static final int PROTOV1 = PNP_MAGIC_KEY << 4 | 1;

    /** All the message types supported by the PNP protocol */
    /* ORDER MATTERS ! ordinal() is used to attribute an id to each message type */
    public enum MessageType {
        /** A call to a remote server */
        CALL,
        /** A response from the server to the client */
        CALL_RESPONSE,
        /** An earth beat message to check that the connection is still alive */
        HEARTBEAT,
        /** When a channel is opened, the client advertise the heartbeat period */
        HEARTBEAT_ADV
        /* That's all */
        ;

        /** Reverse map associate a message type to an ID  */
        final static Map<Integer, MessageType> idToMessageType;
        static {
            // Can't populate idToMessageType from constructor since enums are initialized before
            // any static initializers are run. It is safe to do it from this static block
            idToMessageType = new HashMap<Integer, MessageType>();
            for (MessageType messageType : values()) {
                idToMessageType.put(messageType.ordinal(), messageType);
            }
        }

        public static MessageType getMessageType(int value) {
            return idToMessageType.get(value);
        }

        @Override
        public String toString() {
            switch (this) {
                default:
                    return super.toString();
            }
        }
    }

    /** Common PNP Frame header
     *
     * This is the common header of all PNP messages.
     */
    /* ORDER MATTERS ! ordinal() is used */
    public enum Field {
        // Since we use the netty frame encoder we don't need to include a length field

        /** The protocol ID of this message
         *
         * This field can be used to distinguish different protocol version.
         *
         * Value is a strictly positive integer. The following assertion must be true
         * PROTO_ID && {@link PNPFrame#PNP_MAGIC_KEY} == {@link PNPFrame#PNP_MAGIC_KEY}
         */
        PROTO_ID(4, Integer.class),

        /** The type of this message.
         *
         * ProActive Message Routing protocol support different types of message.
         *
         * Value is the ID of the message type as defined by {@link MessageType}
         */
        MSG_TYPE(4, Integer.class);

        private int length;

        private int myOffset = 0;

        static private int totalOffset = 0;

        /* type is only informative */
        private Field(int length, Class<?> type) {
            this.length = length;
        }

        /** Length of the field in bytes */
        public int getLength() {
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

        /** Length of the fields defined by {@link PNPFrame} */
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
                case PROTO_ID:
                    return "PROTO_ID";
                case MSG_TYPE:
                    return "MSG_TYPE";
                default:
                    return super.toString();
            }
        }
    }

    /* @@@@@@@@@@@@@@@@@@@@ Static methods @@@@@@@@@@@@@@@@@@@@@@ */

    /**
     * Convert a byte[] array to readable string format. This makes the "hex"
     * readable!
     *
     * @return result String buffer in String format
     * @param in byte[] buffer to convert to string format
     * @param the number of bytes to dump
    */

    static String byteArrayToHexString(byte[] buf, int len) {
        byte ch = 0x00;
        int i = 0;
        if (buf == null || buf.length == 0 || len <= 0)
            return null;

        String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };

        StringBuilder out = new StringBuilder(buf.length * 2);
        while (i < buf.length && i < len) {
            ch = (byte) (buf[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4); // shift the bits down
            ch = (byte) (ch & 0x0F); // must do this is high order bit is on!
            out.append(pseudo[(int) ch]); // convert the nibble to a String Character
            ch = (byte) (buf[i] & 0x0F); // Strip off low nibble
            out.append(pseudo[(int) ch]); // convert the nibble to a String Character
            i++;
        }
        return out.toString();
    }

    /** Construct a PNP message from a {@link ChannelBuffer}
     *
     * @param buf
     * 		a buffer which contains a message
     * @param offset
     * 		the offset at which the message begins
     * @throws MalformedMessageException
     * 		If the buffer does not contain a valid message
     */
    public static PNPFrame constructMessage(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        // depending on the type of message, call a different constructor
        MessageType type = MessageType.getMessageType(TypeHelper.channelBufferToInt(buf,
                                                                                    offset +
                                                                                         Field.MSG_TYPE.getOffset()));

        switch (type) {
            case CALL:
                return new PNPFrameCall(buf, offset);
            case CALL_RESPONSE:
                return new PNPFrameCallResponse(buf, offset);
            case HEARTBEAT:
                return new PNPFrameHeartbeat(buf, offset);
            case HEARTBEAT_ADV:
                return new PNPFrameHeartbeatAdvertisement(buf, offset);
            default:
                throw new PNPMalformedMessageException("Unknown message type: " + type);
        }
    }

    /** Reads the protocol ID of a message
     *
     * @param buf
     *		a buffer which contains a message
     * @param offset
     * 		the offset at which the message begins
     * @return The value of the protocol ID field of the message contained in buf at the given offset
     * @throws PNPMalformedMessageException If the protocol ID is wrong
     */
    private static int readProtoID(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        int protoId = TypeHelper.channelBufferToInt(buf, offset + Field.PROTO_ID.getOffset());
        if (protoId != PROTOV1) {
            if ((protoId & PNP_MAGIC_KEY << 4) == PNP_MAGIC_KEY << 4) {
                throw new PNPMalformedMessageException("Invalid protocol version " + protoId + " should be " + PROTOV1);
            } else {
                throw new PNPMalformedMessageException("Invalid protocol version " + protoId + " should be " + PROTOV1 +
                                                       " and match the magic key.");
            }
        }

        return protoId;
    }

    /** Reads the type of a message
     *
     * @param buf
     *		a buffer which contains a message
     * @param offset
     * 		the offset at which the message begins
     * @return The value of the message type field of the message contained in buf at the given offset
     * @throws MalformedMessageException if the message type field contains an invalid value
     */
    public static MessageType readType(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        int typeInt = TypeHelper.channelBufferToInt(buf, offset + Field.MSG_TYPE.getOffset());
        MessageType type = MessageType.getMessageType(typeInt);
        if (type != null)
            return type;
        else
            throw new PNPMalformedMessageException("Invalid value for the " + Field.MSG_TYPE + " field:" + typeInt);
    }

    /** Protocol ID of this message */
    final private int protoId;

    /** Type of this message */
    final private MessageType type;

    /** Create the header of a message
     *
     * length and protocol ID fields will be automatically set when available
     * @param type
     * 		Type of the message
     * @param messageId
     * 		ID of the message
     */
    protected PNPFrame(MessageType type) {
        this.type = type;
        this.protoId = PROTOV1;
    }

    /** Create the header of a message from a byte array
     *
     * @param buf
     *		a buffer which contains a message
     * @param offset
     * 		the offset at which the message begins
     * @param fieldsSize
     * 		the size of the additional fields added by the messages. Can only be known within the particular implementations
     * @throws MalformedMessageException
     * 		If the buffer does not contain a valid message (proto ID, length etc.)
     */
    protected PNPFrame(ChannelBuffer buf, int offset) throws PNPMalformedMessageException {
        this.protoId = readProtoID(buf, offset);
        this.type = readType(buf, offset);
    }

    /** Return the type of this message */
    public MessageType getType() {
        return this.type;
    }

    /** Return the protocol ID of this message */
    public int getProtoID() {
        return this.protoId;
    }

    /** Convert this message into a ChannelBuffer */
    public abstract ChannelBuffer toChannelBuffer();

    /** Write the header of this message into buf
     *
     * This method must be called by toByteArray() implementations.
     *
     * @param buf
     * 		The buffer into the header must be put
     * @param offset
     * 		The offset a which the message will begin
     */
    protected void writeHeader(byte[] buf, int offset) {
        TypeHelper.intToByteArray(this.protoId, buf, offset + Field.PROTO_ID.getOffset());
        TypeHelper.intToByteArray(this.type.ordinal(), buf, offset + Field.MSG_TYPE.getOffset());
    }

    @Override
    public String toString() {
        return Field.MSG_TYPE.toString() + ":" + this.type + ";" + Field.PROTO_ID.toString() + ":" + this.protoId + ";";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + protoId;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PNPFrame other = (PNPFrame) obj;
        if (protoId != other.protoId)
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
}
