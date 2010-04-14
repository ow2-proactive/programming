/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
import org.objectweb.proactive.extra.messagerouting.protocol.TypeHelper;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataMessage.Field;
import org.objectweb.proactive.extra.messagerouting.protocol.message.ErrorMessage.ErrorType;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;


/** A {@link MessageType#HEARTBEAT} message
 *
 * Heartbeat message are used to check the connection. The router periodically sends heartbeat to each
 * client. If the client does not receive the heartbeat in time, it close its tunnel and tries to
 * open a new one.
 *
 * @since ProActive 4.3.0
 */
public class HeartbeatMessage extends Message {

    /**
     * Fields of the {@link DataMessage} header.
     *
     * These fields are put after the {@link Message} header.
     */
    public enum Field {
        /** The ID of the heartbeat */
        HEARTBEAT_ID(8, Long.class);

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
                case HEARTBEAT_ID:
                    return "HEARTBEAT_ID";
                default:
                    return super.toString();
            }
        }
    }

    /** Reads the heartbeatId of a message
     *
     * @param buf
     *            a buffer which contains a message
     * @param offset
     *            the offset at which the message begins
     * @return The value of the length field of the message contained in buf at
     *         the given offset or null if unknown
     */
    public static long readHeartbeatId(byte[] byteArray, int offset) {
        long id = TypeHelper.byteArrayToLong(byteArray, offset + Message.Field.getTotalOffset() +
            Field.HEARTBEAT_ID.getOffset());
        return id;
    }

    final private long heartbeatId;

    /** Create an error message
     *
     * @param recipient
     * 		The recipient of this error message. Can be null for {@link ErrorType#ERR_DISCONNECTION_BROADCAST}
     * @param faulty
     * 		The client which caused the error message. Can be null.
     * @param msgID
     * 		The ID of the message which caused the error or an unique ID generated by the router.
     * @param error
     * 		The error type
     */
    public HeartbeatMessage(long heartbeatId) {
        super(MessageType.HEARTBEAT, -1);
        super.setLength(Message.Field.getTotalOffset() + Field.getTotalOffset());

        this.heartbeatId = heartbeatId;
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     *
     * @param byteArray
     *            the byte array from which to read
     * @param offset
     *            the offset at which to find the message in the byte array
     * @throws MalformedMessageException
     * 			If the buffer does not contain a valid error message
     */
    public HeartbeatMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset, Field.getTotalOffset());

        if (this.getType() != MessageType.HEARTBEAT) {
            throw new MalformedMessageException("Malformed" + MessageType.HEARTBEAT + " message:" +
                "Invalid value for the " + Message.Field.MSG_TYPE + " field:" + this.getType());
        }

        this.heartbeatId = readHeartbeatId(byteArray, offset);
    }

    public long getHeartbeatId() {
        return this.heartbeatId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (heartbeatId ^ (heartbeatId >>> 32));
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
        HeartbeatMessage other = (HeartbeatMessage) obj;
        if (heartbeatId != other.heartbeatId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " Heartbeat Id:" + this.heartbeatId;
    }

    /**
     * This message as a byte array
     *
     * Cached for the sake of speed. But memory can become a bottleneck before
     * CPU or network. Remove this field if router or agent consumes too much
     * memory.
     */
    protected byte[] toByteArray;

    @Override
    public byte[] toByteArray() {
        if (this.toByteArray != null) {
            return this.toByteArray;
        }

        int length = getLength();
        byte[] buf = new byte[length];

        super.writeHeader(buf, 0);
        TypeHelper.longToByteArray(this.heartbeatId, buf, Message.Field.getTotalOffset() +
            Field.HEARTBEAT_ID.getOffset());

        this.toByteArray = buf;
        return buf;
    }

}
