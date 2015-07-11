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
package org.objectweb.proactive.extensions.pamr.router.processor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationReplyMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.RegistrationRequestMessage;
import org.objectweb.proactive.extensions.pamr.protocol.message.ErrorMessage.ErrorType;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;
import org.objectweb.proactive.extensions.pamr.router.Attachment;
import org.objectweb.proactive.extensions.pamr.router.Client;
import org.objectweb.proactive.extensions.pamr.router.RouterImpl;


/** Asynchronous handler for {@link MessageType#DATA_REQUEST}
 * 
 * @since ProActive 4.1.0
 */
public class ProcessorRegistrationRequest extends Processor {

    final private Attachment attachment;

    public ProcessorRegistrationRequest(ByteBuffer messageAsByteBuffer, Attachment attachment,
            RouterImpl router) {
        super(messageAsByteBuffer, router);
        this.attachment = attachment;
    }

    public void process() throws MalformedMessageException {
        // Message.constructMessage guarantees that the cast is safe. If the message is not a RegistrationRequestMessage,
        // a @{link MalformedMessageException} will be thrown
        try {
            RegistrationRequestMessage message = (RegistrationRequestMessage) Message.constructMessage(
                    this.rawMessage.array(), 0);
            this.attachment.setAgentHostname(message.getAgentHostname());
            AgentID agentId = message.getAgentID();

            if (agentId == null) {
                standardConnection(message);
            } else {
                if (agentId.isReserved()) {
                    // Reserved connection & reconnection
                    reservedConnection(message);
                } else {
                    standardReconnection(message);
                }
            }
        } catch (MalformedMessageException e) {
            // try to see who sent it
            try {
                AgentID sender = RegistrationMessage.readAgentID(this.rawMessage.array(), 0);
                throw new MalformedMessageException(e, sender);
            } catch (MalformedMessageException e1) {
                // cannot get the sender
                throw new MalformedMessageException(e, true);
            }
        }
    }

    private void standardReconnection(RegistrationRequestMessage message) {
        // agentId != null, agentId not reserved, magicCookie != null, routerId != null && correct

        AgentID agentId = message.getAgentID();

        // Check that it is not an "old" client
        if (message.getRouterID() != this.router.getId()) {
            logger.warn("AgentId " + agentId +
                " asked to reconnect but the router IDs do not match. Remote endpoint is: " +
                attachment.getRemoteEndpointName());
            notifyInvalidAgent(message, agentId, ErrorType.ERR_INVALID_ROUTER_ID);
            return;
        }

        // Check if the client is know
        Client client = router.getClient(agentId);
        if (client == null) {
            logger.warn("AgentId " + agentId +
                " asked to reconnect but is not known by this router. Remote endpoint is: " +
                attachment.getRemoteEndpointName());
            notifyInvalidAgent(message, agentId, ErrorType.ERR_INVALID_AGENT_ID);
            return;
        }

        if (!client.getMagicCookie().equals(message.getMagicCookie())) {
            logger.warn("AgentId " + agentId +
                " asked to reconnect but provided an incorrect magic cookie. Remote endpoint is: " +
                attachment.getRemoteEndpointName());
            notifyInvalidAgent(message, agentId, ErrorType.ERR_WRONG_MAGIC_COOKIE);
            return;
        }

        try {
            client.disconnect();
        } catch (IOException e) {
            ProActiveLogger.logEatedException(logger, e);
        }

        // Acknowledge the registration
        RegistrationReplyMessage reply = new RegistrationReplyMessage(agentId, message.getMessageID(),
            this.router.getId(), client.getMagicCookie(), this.router.getHeartbeatTimeout());

        client.setAttachment(attachment);
        boolean resp = this.sendReply(client, reply);
        if (resp) {
            client.updateLastSeen();
            client.sendPendingMessage();
        } else {
            logger.info("Failed to acknowledge the registration for " + agentId);
        }
    }

    private void reservedConnection(RegistrationRequestMessage message) {
        AgentID agentId = message.getAgentID();

        Client client = router.getClient(agentId);
        if (client == null) {
            logger.warn("AgentId " + agentId + " asked to connect. But this reserved id " + agentId +
                " is not known by the router (check your config). Remote endpoint is: " +
                attachment.getRemoteEndpointName());
            notifyInvalidAgent(message, agentId, ErrorType.ERR_INVALID_AGENT_ID);
            return;
        }

        if (!client.getMagicCookie().equals(message.getMagicCookie())) {
            logger.warn("AgentId " + agentId +
                " asked to reconnect but provided an incorrect magic cookie. Remote endpoint is: " +
                attachment.getRemoteEndpointName());
            notifyInvalidAgent(message, agentId, ErrorType.ERR_WRONG_MAGIC_COOKIE);
            return;
        }

        if (message.getRouterID() != RouterImpl.DEFAULT_ROUTER_ID &&
            message.getRouterID() != this.router.getId()) {
            logger.warn("AgentId " + agentId +
                " asked to reconnect but the router IDs do not match. Remote endpoint is: " +
                attachment.getRemoteEndpointName());
            notifyInvalidAgent(message, agentId, ErrorType.ERR_INVALID_ROUTER_ID);
            return;
        }

        // Disconnect the client if needed
        try {
            client.disconnect();
        } catch (IOException e) {
            ProActiveLogger.logEatedException(logger, e);
        }

        // Acknowledge the registration
        RegistrationReplyMessage reply = new RegistrationReplyMessage(agentId, message.getMessageID(),
            this.router.getId(), client.getMagicCookie(), this.router.getHeartbeatTimeout());

        client.setAttachment(attachment);
        boolean resp = this.sendReply(client, reply);
        if (resp) {
            client.updateLastSeen();
            client.sendPendingMessage();
        } else {
            logger.info("Failed to acknowledge the registration for " + agentId);
            attachment.dtor();
        }
    }

    private void standardConnection(RegistrationRequestMessage message) {
        // agentId == null, magicCookie != null, routerId == null

        long routerId = message.getRouterID();
        if (routerId != RouterImpl.DEFAULT_ROUTER_ID) {
            logger.warn("Invalid connection request. router ID must be " + RouterImpl.DEFAULT_ROUTER_ID +
                ". Remote endpoint is: " + attachment.getRemoteEndpointName());

            this.attachment.dtor();
            return;
        }

        AgentID agentId = AgentIdGenerator.getId();
        MagicCookie magicCookie = message.getMagicCookie();
        RegistrationMessage reply = new RegistrationReplyMessage(agentId, message.getMessageID(),
            this.router.getId(), magicCookie, this.router.getHeartbeatTimeout());

        Client client = new Client(attachment, agentId, magicCookie);
        boolean resp = this.sendReply(client, reply);
        if (resp) {
            this.router.addClient(client);
            client.updateLastSeen();
        } else {
            logger.info("Failed to send registration reply to " + this.attachment);
            this.attachment.dtor();
        }
    }

    private void notifyInvalidAgent(RegistrationRequestMessage message, AgentID agentId, ErrorType errorCode) {

        // Send an ERR_ message (best effort)
        ErrorMessage errMessage = new ErrorMessage(errorCode, agentId, agentId, message.getMessageID());

        try {
            attachment.send(ByteBuffer.wrap(errMessage.toByteArray()));
        } catch (IOException e) {
            logger.info("Failed to notify the client that invalid agent has been advertised");
        }

        // Since we disconnect the client, we must free the resources
        this.attachment.dtor();
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
            logger.info("Failed to send registration reply to " + reply.getAgentID(), e);
        }
        return false;
    }

    static abstract private class AgentIdGenerator {
        static final private AtomicLong generator = new AtomicLong(AgentID.MIN_DYNAMIC_AGENT_ID);

        static public AgentID getId() {
            return new AgentID(generator.getAndIncrement());
        }
    }
}
