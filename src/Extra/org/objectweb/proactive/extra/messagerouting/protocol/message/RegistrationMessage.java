package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.TypeHelper;


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

    /**
     * Fields of the {@link RegistrationMessage} header.
     * 
     * These fields are put after the {@link Message} header.
     */
    public enum Field {
        AGENT_ID(8, Long.class);

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
    }

    /** The {@link AgentID} */
    final private AgentID agentID;

    /** Create a registration message.
     * 
     * @param type
     * 		Type of the message {@link MessageType#REGISTRATION_REQUEST} or {@link MessageType#REGISTRATION_REPLY}
     * @param messageId
     * 		The message ID of the message. If {@link MessageType#REGISTRATION_REPLY}, then the message ID
     * 		must be the same than the correlated {@link MessageType#REGISTRATION_REQUEST}
     * @param agentID
     * 		The agentID or null
     */
    public RegistrationMessage(MessageType type, long messageId, AgentID agentID) {
        super(type, messageId);

        this.agentID = agentID;
        super.setLength(Message.Field.getTotalOffset() + Field.getTotalOffset());

    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     * @param byteArray the byte array from which to read
     * @param offset the offset at which to find the message in the byte array
     */
    public RegistrationMessage(byte[] byteArray, int offset) {
        super(byteArray, offset);

        this.agentID = readAgentID(byteArray, offset);
    }

    public AgentID getAgentID() {
        return this.agentID;
    }

    @Override
    public byte[] toByteArray() {
        int length = super.getLength();
        byte[] buff = new byte[length];

        super.writeHeader(buff, 0);

        long id = -1;
        if (this.agentID != null) {
            id = this.agentID.getId();
        }
        TypeHelper.longToByteArray(id, buff, Message.Field.getTotalOffset() + Field.AGENT_ID.getOffset());
        return buff;
    }

    /**
     * Reads the AgentID of a formatted message beginning at a certain offset inside a buffer. Encapsulates it in an AgentID object.
     * @param byteArray the buffer in which to read 
     * @param offset the offset at which to find the beginning of the message in the buffer
     * @return the AgentID of the formatted message
     */
    static public AgentID readAgentID(byte[] byteArray, int offset) {
        long id = TypeHelper.byteArrayToLong(byteArray, offset + Message.Field.getTotalOffset() +
            Field.AGENT_ID.getOffset());
        return (id >= 0) ? new AgentID(id) : null;
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
        return true;
    }

}