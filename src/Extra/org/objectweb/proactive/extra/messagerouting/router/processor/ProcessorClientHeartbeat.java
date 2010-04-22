package org.objectweb.proactive.extra.messagerouting.router.processor;

import java.nio.ByteBuffer;

import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.HeartbeatClientMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.HeartbeatMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;
import org.objectweb.proactive.extra.messagerouting.router.Client;
import org.objectweb.proactive.extra.messagerouting.router.RouterImpl;


/** Asynchronous handler for {@link MessageType#HEARTBEAT}
 *
 * @since ProActive 4.3.0
 */
public class ProcessorClientHeartbeat extends Processor {

    public ProcessorClientHeartbeat(ByteBuffer messageAsByteBuffer, RouterImpl router) {
        super(messageAsByteBuffer, router);
    }

    @Override
    public void process() throws MalformedMessageException {
        HeartbeatClientMessage hbMsg = new HeartbeatClientMessage(this.rawMessage.array(), 0);
        AgentID srcAgentId = hbMsg.getSrcAgentId();
        Client client = router.getClient(srcAgentId);
        if (client != null) {
            client.updateLastSeen();
        } else {
            logger.warn("Received an heartbeat from an unknown client: " + srcAgentId);
        }
    }

}
