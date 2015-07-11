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

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;


/** A registration message
 * 
 * When a client connects to the router. It has to negociate its registration with 
 * {@link MessageType#REGISTRATION_REQUEST} and {@link MessageType#REGISTRATION_REPLY}
 * 
 * The first time a client connect to the router, it asks for an {@link AgentID}. If
 * the client is disconnected, it can reconnect to by advertising its {@link AgentID}.
 * 
 * @since ProActive 4.1.0
 */
public abstract class RegistrationMessage extends Message {

    private static final long UNKNOWN_AGENT_ID = -1;

    /**
     * Fields of the {@link RegistrationMessage} header.
     *
     * These fields are put after the {@link Message} header.
     */
    public enum Field {
        /** The Agent ID assigned to the client (must be set and not 0) */
        AGENT_ID(8, Long.class),
        /** The ID of the router
         *
         * If the client wants to reconnect later, it must know this id
         */
        ROUTER_ID(8, Long.class),
        /** The heartbeat period to use. Both clients and router use the
         * same heartbeat period. Since clients and router must agree on the
         * same period, the client must use this value.
         *
         * Can be 0.
         */
        HEARTBEAT_PERIOD(4, Integer.class),
        /** The magic cookie used to unlock the client slot on the router */
        MAGIC_COOKIE(MagicCookie.COOKIE_SIZE, MagicCookie.class);

        private int length;
        private Class<?> type;

        private Field(int length, Class<?> type) {
            this.length = length;
            this.type = type;
        }

        public long getLength() {
            return this.length;
        }

        public int getOffset() {
            int offset = 0;
            // No way to avoid this iteration over ALL the field
            // There is no such method than Field.getOrdinal(x)
            for (Field field : values()) {
                if (field.ordinal() < this.ordinal()) {
                    offset += field.getLength();
                }
            }
            return offset;
        }

        public String getType() {
            return this.type.toString();
        }

        static public int getTotalOffset() {
            // OPTIM: Can be optimized with caching if needed
            int totalOffset = 0;
            for (Field field : values()) {
                totalOffset += field.getLength();
            }
            return totalOffset;
        }

        @Override
        public String toString() {
            switch (this) {
                case AGENT_ID:
                    return "AGENT_ID";
                case ROUTER_ID:
                    return "ROUTER_ID";
                case HEARTBEAT_PERIOD:
                    return "HEARTBEAT_PERIOD";
                case MAGIC_COOKIE:
                    return "MAGIC_COOKIE";
                default:
                    return super.toString();
            }
        }
    }

    /** The {@link AgentID} */
    final private AgentID agentID;

    /** The router id 
     *
     * 0 if unknown (first connection)
     */
    final private long routerID;

    /** The heartbeatPeriod
     *
     * 0 if unknown
     */
    final private int heartbeatPeriod;

    /** The magic cookie or null if not set */
    final private MagicCookie magicCookie;

    /** Create a registration message.
     * 
     * @param type
     * 		Type of the message {@link MessageType#REGISTRATION_REQUEST} or {@link MessageType#REGISTRATION_REPLY}
     * @param messageId
     * 		The message ID of the message. If {@link MessageType#REGISTRATION_REPLY}, then the message ID
     * 		must be the same than the correlated {@link MessageType#REGISTRATION_REQUEST}
     * @param agentID
     * 		The agentID or null
     * @param routerID
     * 		The router id
     */
    public RegistrationMessage(MessageType type, long messageId, AgentID agentID, long routerID,
            MagicCookie magicCookie, int heartbeatPeriod) {
        super(type, messageId);

        this.agentID = agentID;
        this.routerID = routerID;
        this.heartbeatPeriod = heartbeatPeriod;
        this.magicCookie = magicCookie;
        super.setLength(Message.Field.getTotalOffset() + Field.getTotalOffset());

    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     * @param byteArray the byte array from which to read
     * @param offset the offset at which to find the message in the byte array
     * @throws MalformedMessageException if the byte buffer does not contain a valid message
     */
    public RegistrationMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset, Field.getTotalOffset());

        try {
            this.agentID = readAgentID(byteArray, offset);
            this.routerID = readRouterID(byteArray, offset);
            this.heartbeatPeriod = readHeartbeatPeriod(byteArray, offset);
            this.magicCookie = readMagicCookie(byteArray, offset);
        } catch (MalformedMessageException e) {
            throw new MalformedMessageException("Malformed " + this.getType() + " message:" + e.getMessage());
        }
    }

    public AgentID getAgentID() {
        return this.agentID;
    }

    public long getRouterID() {
        return this.routerID;
    }

    public int getHeartbeatPeriod() {
        return this.heartbeatPeriod;
    }

    public MagicCookie getMagicCookie() {
        return this.magicCookie;
    }

    @Override
    public byte[] toByteArray() {
        int length = super.getLength();
        byte[] buff = new byte[length];

        super.writeHeader(buff, 0);

        long id = UNKNOWN_AGENT_ID;
        if (this.agentID != null) {
            id = this.agentID.getId();
        }

        TypeHelper.longToByteArray(id, buff, Message.Field.getTotalOffset() + Field.AGENT_ID.getOffset());
        TypeHelper.longToByteArray(routerID, buff,
                Message.Field.getTotalOffset() + Field.ROUTER_ID.getOffset());
        TypeHelper.intToByteArray(heartbeatPeriod, buff, Message.Field.getTotalOffset() +
            Field.HEARTBEAT_PERIOD.getOffset());
        System.arraycopy(this.magicCookie.getBytes(), 0, buff, Message.Field.getTotalOffset() +
            Field.MAGIC_COOKIE.getOffset(), (int) Field.MAGIC_COOKIE.getLength());
        return buff;
    }

    /**
     * Reads the AgentID of a formatted message beginning at a certain offset inside a buffer. Encapsulates it in an AgentID object.
     * @param byteArray the buffer in which to read 
     * @param offset the offset at which to find the beginning of the message in the buffer
     * @return the AgentID of the formatted message
     * @throws MalformedMessageException if the message contains an invalid agentID value
     */
    static public AgentID readAgentID(byte[] byteArray, int offset) throws MalformedMessageException {
        long id = TypeHelper.byteArrayToLong(byteArray, offset + Message.Field.getTotalOffset() +
            Field.AGENT_ID.getOffset());
        if (id >= 0)
            return new AgentID(id);
        else if (id == UNKNOWN_AGENT_ID) {
            // in the case of REG_REQ message, the Agent ID is unknown
            MessageType type = Message.readType(byteArray, 0);
            if (type.equals(MessageType.REGISTRATION_REQUEST))
                return null;
            else
                throw new MalformedMessageException("Invalid value for the " + Field.AGENT_ID + " field:" +
                    id);
        } else
            throw new MalformedMessageException("Invalid value for the " + Field.AGENT_ID + " field:" + id);
    }

    /**
     * Reads the router ID  of a formatted message beginning at a certain offset inside a buffer. Encapsulates it in an AgentID object.
     * @param byteArray the buffer in which to read 
     * @param offset the offset at which to find the beginning of the message in the buffer
     * @return the Router ID of the formatted message
     */
    static public long readRouterID(byte[] byteArray, int offset) {
        long id = TypeHelper.byteArrayToLong(byteArray, offset + Message.Field.getTotalOffset() +
            Field.ROUTER_ID.getOffset());
        return id;
    }

    /**
     * Reads the heartbeat period of a formatted message beginning at a certain offset inside a buffer.
     * @param byteArray the buffer in which to read
     * @param offset the offset at which to find the beginning of the message in the buffer
     * @return the heartbeat period of the formatted message
     */
    static public int readHeartbeatPeriod(byte[] byteArray, int offset) throws MalformedMessageException {
        int hb = TypeHelper.byteArrayToInt(byteArray, offset + Message.Field.getTotalOffset() +
            Field.HEARTBEAT_PERIOD.getOffset());

        if (hb < 0) {
            throw new MalformedMessageException(
                "Invalid registration message. The hearbeat field must be positive");
        }
        return hb;
    }

    static public MagicCookie readMagicCookie(byte[] byteArray, int offset) {
        byte[] cBuf = new byte[MagicCookie.COOKIE_SIZE];
        System.arraycopy(byteArray, offset + Message.Field.getTotalOffset() + Field.MAGIC_COOKIE.getOffset(),
                cBuf, 0, (int) Field.MAGIC_COOKIE.getLength());
        return new MagicCookie(cBuf);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((agentID == null) ? 0 : agentID.hashCode());
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
        RegistrationMessage other = (RegistrationMessage) obj;
        if (agentID == null) {
            if (other.agentID != null)
                return false;
        } else if (!agentID.equals(other.agentID))
            return false;
        if (routerID == 0) {
            if (other.routerID != 0)
                return false;
        } else if (routerID != other.routerID)
            // different router
            return false;
        if (heartbeatPeriod == 0) {
            if (other.heartbeatPeriod != 0)
                return false;
        } else if (heartbeatPeriod != other.heartbeatPeriod)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + Field.AGENT_ID.toString() + ":" + this.agentID + ";" +
            Field.ROUTER_ID.toString() + ":" + this.routerID + ";" + Field.HEARTBEAT_PERIOD.toString() + ":" +
            this.heartbeatPeriod;
    }

}
