package org.objectweb.proactive.extra.messagerouting.router.processor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.ErrorMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extra.messagerouting.protocol.message.ErrorMessage.ErrorType;
import org.objectweb.proactive.extra.messagerouting.protocol.message.Message.MessageType;
import org.objectweb.proactive.extra.messagerouting.router.Attachment;
import org.objectweb.proactive.extra.messagerouting.router.Client;
import org.objectweb.proactive.extra.messagerouting.router.RouterImpl;


/** Asynchronous handler for {@link MessageType#DATA_REQUEST}
 * 
 * @since ProActive 4.1.0
 */
public class ProcessorRegistrationRequest extends Processor {

    final private RegistrationRequestMessage message;
    final private Attachment attachment;
    final private RouterImpl router;

    public ProcessorRegistrationRequest(ByteBuffer messageAsByteBuffer, Attachment attachment,
            RouterImpl router) {
        this.attachment = attachment;
        this.router = router;

        Message tmpMsg = Message.constructMessage(messageAsByteBuffer.array(), 0);
        this.message = (RegistrationRequestMessage) tmpMsg;
    }

    public void process() {
        AgentID agentId = this.message.getAgentID();
        if (agentId == null) {
            connection();
        } else {
            reconnection();
        }
    }

    /* Generate and unique AgentID and send the registration reply
     * in best effort. If succeeded, add the new client to the router
     */
    private void connection() {
        AgentID agentId = AgentIdGenerator.getId();

        RegistrationMessage reply = new RegistrationReplyMessage(agentId, this.message.getMessageID());

        Client client = new Client(attachment, agentId);
        boolean resp = this.sendReply(client, reply);
        if (resp) {
            this.router.addClient(client);
        }
    }

    /* Check if the client is known. If not send an ERR_.
     * Otherwise, send the registration reply in best effort.
     * If succeeded, update the attachment in the client, and
     * flush the pending messages.
     */
    private void reconnection() {
        // Check if the client is know
        AgentID agentId = message.getAgentID();
        Client client = router.getClient(agentId);

        if (client == null) {
            // Send an ERR_ message (best effort)
            logger.warn("AgentId " + agentId + " asked to reconnect but is not known by this router");

            ErrorMessage errMessage = new ErrorMessage(ErrorType.ERR_INVALID_AGENT_ID, agentId, agentId,
                this.message.getMessageID());
            try {
                attachment.send(ByteBuffer.wrap(errMessage.toByteArray()));
            } catch (IOException e) {
                logger.info("Failed to notify the client that invalid agent has been advertised");
            }
        } else {
            // Acknowledge the registration
            client.setAttachment(attachment);
            RegistrationReplyMessage reply = new RegistrationReplyMessage(agentId, this.message
                    .getMessageID());

            boolean resp = this.sendReply(client, reply);
            if (resp) {
                client.sendPendingMessage();
            } else {
                logger.info("Failed to acknowledge the registration for " + agentId);
                // Drop the attachment
            }
        }
    }

    /* Send the registration reply to the client (best effort)
     *
     * We don't want to cache the message on failure because if the tunnel
     * failed, the client will register again anyway.
     */
    private boolean sendReply(Client client, RegistrationMessage reply) {
        try {
            client.sendMessage(reply.toByteArray());
            return true;
        } catch (IOException e) {
            logger.info("Failed to send registration reply to " + reply.getAgentID() + ", IOException");
        }
        return false;
    }

    static abstract private class AgentIdGenerator {
        static final private AtomicLong generator = new AtomicLong(0);

        static public AgentID getId() {
            return new AgentID(generator.getAndIncrement());
        }
    }

}
