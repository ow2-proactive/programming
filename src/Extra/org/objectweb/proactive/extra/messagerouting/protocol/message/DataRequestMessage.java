package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;


/** A {@link MessageType#DATA_REQUEST} message
 * 
 * @since ProActive 4.1.0
 */
public class DataRequestMessage extends DataMessage {

    /** Create a {@link MessageType#DATA_REQUEST} message
     * 
     * @param sender
     * 		sender of the request
     * @param recipient
     * 		recipient of the request
     * @param msgID
     * 		an unique ID per sender.
     * @param data
     * 		the payload
     */
    public DataRequestMessage(AgentID sender, AgentID recipient, long msgID, byte[] data) {
        super(MessageType.DATA_REQUEST, sender, recipient, msgID, data);
    }

    /** Create a {@link MessageType#DATA_REQUEST} message from a byte array
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins  
     * @throws IllegalArgumentException
     * 		If the buffer does not match message requirements (proto ID, length etc.)
     */
    public DataRequestMessage(byte[] byteArray, int offset) throws IllegalArgumentException {
        super(byteArray, offset);

        if (this.getType() != MessageType.DATA_REQUEST) {
            throw new IllegalArgumentException("Invalid message type " + this.getType());
        }
    }
}
