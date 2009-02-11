package org.objectweb.proactive.extra.messagerouting.client;

import org.objectweb.proactive.extra.messagerouting.protocol.message.DataRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;


/** Handle a {@link MessageType#DATA_REQUEST} message received by an Agent 
 * 
 * @since ProActive 4.1.0
 */
public interface MessageHandler {

    public void pushMessage(DataRequestMessage message);
}
