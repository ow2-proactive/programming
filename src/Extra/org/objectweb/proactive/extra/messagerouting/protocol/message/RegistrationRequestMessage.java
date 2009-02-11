package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;


/** A {@link MessageType#REGISTRATION_REQUEST} message
 * 
 * A such message is sent when a client connect or reconnect to the router.
 * 
 * @since ProActive 4.1.0
 */
public class RegistrationRequestMessage extends RegistrationMessage {

    /** Create a {@link MessageType#REGISTRATION_REQUEST} message
     * 
     * @param agentID
     * 		The client {@link AgentID}, or null if not known 
     * @param messageId
     * 		An unique message ID per sender.
     */
    public RegistrationRequestMessage(AgentID agentID, long messageId) {
        super(MessageType.REGISTRATION_REQUEST, messageId, agentID);
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     * @param byteArray the byte array from which to read
     * @param offset the offset at which to find the message in the byte array
     * @throws InstantiationException
     */
    public RegistrationRequestMessage(byte[] byteArray, int offset) throws IllegalArgumentException {
        super(byteArray, offset);

        if (this.getType() != MessageType.REGISTRATION_REQUEST) {
            throw new IllegalArgumentException("Invalid message type " + this.getType());
        }
    }

}
