package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;


/** A {@link MessageType#DATA_REPLY} message
 * 
 * @since ProActive 4.1.0
 */
public class DataReplyMessage extends DataMessage {
    /** Create a {@link DataReplyMessage}
     * 
     * @param sender
     * 		sender of the reply
     * @param recipient
     * 		recipient of the reply
     * @param msgID
     * 		message ID. Must be the same than the {@link DataRequestMessage}
     * @param data
     * 		The payload
     */
    public DataReplyMessage(AgentID sender, AgentID recipient, long msgID, byte[] data) {
        super(MessageType.DATA_REPLY, sender, recipient, msgID, data);
    }

    /** Create a {@link MessageType#DATA_REPLY} message from a byte array
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins  
     * @throws IllegalArgumentException
     * 		If the buffer does not match message requirements (proto ID, length etc.)
     */
    public DataReplyMessage(byte[] byteArray, int offset) throws IllegalArgumentException {
        super(byteArray, offset);

        if (this.getType() != MessageType.DATA_REPLY) {
            throw new IllegalArgumentException("Invalid message type " + this.getType());
        }
    }
}
