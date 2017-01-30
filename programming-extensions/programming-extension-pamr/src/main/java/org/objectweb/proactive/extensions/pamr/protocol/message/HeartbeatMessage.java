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
package org.objectweb.proactive.extensions.pamr.protocol.message;

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage.ErrorType;


/** A {@link MessageType#HEARTBEAT} message
 *
 * Heartbeat message are used to check the connection. If clients or router do not
 * receive an heartbeat in time, the tunnel is closed and the client will try to
 * reopen a new one.
 *
 * Both clients and router must sends heartbeats to be able to detect
 * disconnection ASAP (xxx.write() will not throw an exception until the TCP kernel
 * buffer is full).
 *
 * @since ProActive 4.3.0
 */
public abstract class HeartbeatMessage extends Message {
    static final private long ROUTER_AGENT_ID = -53;

    /**
     * Fields of the {@link DataMessage} header.
     *
     * These fields are put after the {@link Message} header.
     */
    public enum Field {
        /** The ID of the heartbeat
         *
         * Only used for easier debugging
         */
        HEARTBEAT_ID(8, Long.class),
        /** The source agent ID
         *
         * -1 if the heartbeat is sent by the router
         */
        SRC_AGENT_ID(8, Long.class);

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

        /** Length of the fields defined by {@link DataMessage} */
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
                case HEARTBEAT_ID:
                    return "HEARTBEAT_ID";
                case SRC_AGENT_ID:
                    return "SRC_AGENT_ID";
                default:
                    return super.toString();
            }
        }
    }

    /** Reads the heartbeat period of a message
     *
     * @param buf
     *            a buffer which contains a message
     * @param offset
     *            the offset at which the message begins
     * @return The value of the heartbeat period of the message contained in buf at
     *         the given offset
     */
    private static long readHeartbeatPeriod(byte[] byteArray, int offset) {
        long id = TypeHelper.byteArrayToLong(byteArray,
                                             offset + Message.Field.getTotalOffset() + Field.HEARTBEAT_ID.getOffset());
        return id;
    }

    private AgentID readSrcAgentId(byte[] byteArray, int offset) throws MalformedMessageException {
        long id = TypeHelper.byteArrayToLong(byteArray,
                                             offset + Message.Field.getTotalOffset() + Field.SRC_AGENT_ID.getOffset());

        if (id >= 0) {
            return new AgentID(id);
        } else if (id == ROUTER_AGENT_ID) {
            return null;
        } else {
            throw new MalformedMessageException("Invalid value for " + Field.SRC_AGENT_ID + " field:" + id);
        }
    }

    final private long heartbeatId;

    final private AgentID srcAgentId;

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
    public HeartbeatMessage(MessageType type, long heartbeatId, AgentID srcAgentId) {
        super(type, -1);
        super.setLength(Message.Field.getTotalOffset() + Field.getTotalOffset());

        this.heartbeatId = heartbeatId;
        this.srcAgentId = srcAgentId;
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

        this.heartbeatId = readHeartbeatPeriod(byteArray, offset);
        this.srcAgentId = readSrcAgentId(byteArray, offset);
    }

    public long getHeartbeatId() {
        return this.heartbeatId;
    }

    protected AgentID getSrcAgentId() {
        return this.srcAgentId;
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
        TypeHelper.longToByteArray(this.heartbeatId,
                                   buf,
                                   Message.Field.getTotalOffset() + Field.HEARTBEAT_ID.getOffset());

        long id = ROUTER_AGENT_ID;
        if (this.srcAgentId != null) {
            id = this.srcAgentId.getId();
        }
        TypeHelper.longToByteArray(id, buf, Message.Field.getTotalOffset() + Field.SRC_AGENT_ID.getOffset());

        this.toByteArray = buf;
        return buf;
    }

}
