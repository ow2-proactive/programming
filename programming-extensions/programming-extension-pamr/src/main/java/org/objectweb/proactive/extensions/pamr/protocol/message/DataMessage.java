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
package org.objectweb.proactive.extensions.pamr.protocol.message;

import java.util.Arrays;

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;


/** A data message
 * 
 * @since ProActive 4.1.0
 */
public abstract class DataMessage extends Message {
    /** The offset of the payload */
    private static final int DATA_MESSAGE_HEADER_LENGTH = Message.Field.getTotalOffset() +
        Field.getTotalOffset();

    private static final long UNKNOWN_AGENT_ID = -1;

    /**
     * Fields of the {@link DataMessage} header.
     * 
     * These fields are put after the {@link Message} header.
     */
    public enum Field {
        /** The {@link AgentID} of the sender of this message */
        SRC_AGENT_ID(8, Long.class),
        /** The {@link AgentID} of the recipient of this message */
        DST_AGENT_ID(8, Long.class);

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

        /** Length of the fields defined by {@link DataMessage} */
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
                case SRC_AGENT_ID:
                    return "SRC_AGENT_ID";
                case DST_AGENT_ID:
                    return "DST_AGENT_ID";
                default:
                    return super.toString();
            }
        }
    }

    /* @@@@@@@@@@@@@@@@@@@@ Static methods @@@@@@@@@@@@@@@@@@@@@@ */

    /** Reads the sender of a message
     * 
     * @param buf
     *            a buffer which contains a message
     * @param offset
     *            the offset at which the message begins
     * @return The value of the length field of the message contained in buf at
     *         the given offset or null if unknown
     * @throws MalformedMessageException
     * 			if the SRC_AGENT_ID field from the message is invalid
     */
    static public AgentID readSender(byte[] byteArray, int offset) throws MalformedMessageException {
        long id = TypeHelper.byteArrayToLong(byteArray, offset + Message.Field.getTotalOffset() +
            Field.SRC_AGENT_ID.getOffset());
        if (id >= 0)
            return new AgentID(id);
        else if (id == UNKNOWN_AGENT_ID) {
            // in the case of error messages, the Agent ID could be unknown
            MessageType type = Message.readType(byteArray, 0);
            if (type.equals(MessageType.ERR_))
                return null;
            else
                throw new MalformedMessageException("Invalid value for " + Field.SRC_AGENT_ID + " field: " +
                    id);
        } else
            throw new MalformedMessageException("Invalid value for " + Field.SRC_AGENT_ID + " field: " + id);
    }

    /** Reads the recipient of a message
     * 
     * @param buf
     *            a buffer which contains a message
     * @param offset
     *            the offset at which the message begins
     * @return The value of the length field of the message contained in buf at
     *         the given offset or null if unknown
     * @throws MalformedMessageException
     * 			if the DST_AGENT_ID field from the message is invalid
     */
    public static AgentID readRecipient(byte[] byteArray, int offset) throws MalformedMessageException {
        long id = TypeHelper.byteArrayToLong(byteArray, offset + Message.Field.getTotalOffset() +
            Field.DST_AGENT_ID.getOffset());

        if (id >= 0)
            return new AgentID(id);
        else if (id == UNKNOWN_AGENT_ID) {
            // in the case of error messages, the Agent ID could be unknown
            MessageType type = Message.readType(byteArray, 0);
            if (type.equals(MessageType.ERR_))
                return null;
            else
                throw new MalformedMessageException("Invalid value for " + Field.SRC_AGENT_ID + " field: " +
                    id);
        } else
            throw new MalformedMessageException("Invalid value for " + Field.SRC_AGENT_ID + " field: " + id);
    }

    /** Sender of this message */
    final protected AgentID sender;
    /** Recipient of this message */
    final protected AgentID recipient;
    /** Payload of this message */
    /*
     * Since data is not used by the router could be lazily created to avoid
     * data duplication
     */
    final protected byte[] data;
    /**
     * This message as a byte array
     * 
     * Cached for the sake of speed. But memory can become a bottleneck before
     * CPU or network. Remove this field if router or agent consumes too much
     * memory.
     */
    protected byte[] toByteArray;

    /**
     * Create a {@link DataMessage}
     * 
     * All the parameters must be non null
     * 
     * @param type
     *            Type of the message. Only {@link MessageType#DATA_REQUEST} and
     *            {@link MessageType#DATA_REPLY} are valid types.
     * 
     * @param src
     *            Sender
     * @param dst
     *            Recipient
     * @param msgID
     *            Message ID
     * @param data
     *            Payload
     */
    protected DataMessage(MessageType type, AgentID src, AgentID dst, long msgID, byte[] data) {
        super(type, msgID);
        this.sender = src;
        this.recipient = dst;
        this.data = data;
        this.toByteArray = null;

        int length = 0;
        length += Message.Field.getTotalOffset();
        length += Field.getTotalOffset();
        length += (data != null ? data.length : 0);
        super.setLength(length);
    }

    /**
     * Create a {@link DataMessage} from a byte array
     * 
     * @param buf
     *            a buffer which contains a message
     * @param offset
     *            the offset at which the message begins
     * @throws MalformedMessageException
     *             If the buffer does not contain a valid message (proto ID,
     *             length etc.)
     */
    protected DataMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset, Field.getTotalOffset());

        try {
            this.sender = readSender(byteArray, offset);
            this.recipient = readRecipient(byteArray, offset);

            int datalength = super.getLength() - DATA_MESSAGE_HEADER_LENGTH;
            this.data = new byte[datalength];
            System.arraycopy(byteArray, offset + DATA_MESSAGE_HEADER_LENGTH, this.data, 0, datalength);

            this.toByteArray = byteArray;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new MalformedMessageException("Malformed " + this.getType() + " message:" +
                "Invalid value for " + Message.Field.LENGTH + " field:" + super.getLength(), e);
        } catch (MalformedMessageException e) {
            throw new MalformedMessageException("Malformed " + this.getType() + " message:" + e.getMessage());
        }
    }

    @Override
    public byte[] toByteArray() {
        if (this.toByteArray != null) {
            return this.toByteArray;
        }

        int length = getLength();
        byte[] buf = new byte[length];

        super.writeHeader(buf, 0);

        long srcId = UNKNOWN_AGENT_ID;
        if (sender != null) {
            srcId = sender.getId();
        }
        TypeHelper.longToByteArray(srcId, buf,
                Message.Field.getTotalOffset() + Field.SRC_AGENT_ID.getOffset());

        long dstId = UNKNOWN_AGENT_ID;
        if (recipient != null) {
            dstId = recipient.getId();
        }
        TypeHelper.longToByteArray(dstId, buf,
                Message.Field.getTotalOffset() + Field.DST_AGENT_ID.getOffset());

        if (data != null) {
            System.arraycopy(data, 0, buf, DATA_MESSAGE_HEADER_LENGTH, data.length);
        }

        this.toByteArray = buf;

        return buf;
    }

    /** Return the sender of this message */
    public AgentID getSender() {
        return sender;
    }

    /** Return the recipient of this message */
    public AgentID getRecipient() {
        return recipient;
    }

    /** Return the payload of this message */
    public byte[] getData() {
        return data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(data);
        result = prime * result + ((recipient == null) ? 0 : recipient.hashCode());
        result = prime * result + ((sender == null) ? 0 : sender.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        DataMessage other = (DataMessage) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        if (recipient == null) {
            if (other.recipient != null)
                return false;
        } else if (!recipient.equals(other.recipient))
            return false;
        if (sender == null) {
            if (other.sender != null)
                return false;
        } else if (!sender.equals(other.sender))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + Field.SRC_AGENT_ID.toString() + ":" + this.sender + ";" +
            Field.DST_AGENT_ID.toString() + ":" + this.recipient + ";";
    }

}
