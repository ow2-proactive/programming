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
package org.objectweb.proactive.extensions.pamr.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;


/** A remote agent 
 * 
 * When a remote agent connects to the router a client is created with the remote agent id.
 * This client remains in the router memory forever and will be reused if the remote agent
 * disconnect and reconnect. 
 * 
 * The attachment corresponds to the current connection. If the remote agent is currently 
 * connected then the attachment is non null. If the remote agent is disconnected, the attachment
 * is set to null. A lock must be used before using or modifying the attachment since it can 
 * change at any time.
 *
 * A list of pending message is maintained for each client. They will be send to the client when
 * the client will reconnect. Only message that cannot be dropped must be put in the pending message
 * queue. If possible, it is preferable to send an error message to notify a failure. 
 * 
 * @since ProActive 4.1.0
 */
public class Client {
    static final private Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER);

    public static final Logger admin_logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER_ADMIN);

    /** This client represents one remote Agent. */
    final private AgentID agentId;

    /**
     * Current attachment
     *
     * Change each time the client reconnects.
     *
     * Set to null each time the router detects the remote agent is
     * disconnected. But a non null value does not mean the remote agent is
     * connected (can fail any time)
     */
    private Attachment attachment;

    /** This lock must be held each time the attachment is used.
     *
     * It ensure that one and only one client will update or discard the
     * attachment
     */
    final private Object attachment_lock = new Object();

    /** List of messages to be sent when the client will reconnect */
    final private Queue<ByteBuffer> pendingMessage;

    /** The timestamp of the last client activity
     *
     * Used to determine if the tunnel is broken
     */
    private AtomicLong lastSeen;

    final private MagicCookie magicCookie;

    public Client(AgentID agentID, MagicCookie magicCookie) {
        this(null, agentID, magicCookie);
    }

    public Client(Attachment attachment, AgentID agentID, MagicCookie magicCookie) {
        this.agentId = agentID;
        this.pendingMessage = new ConcurrentLinkedQueue<ByteBuffer>();
        this.lastSeen = new AtomicLong(0);
        this.magicCookie = magicCookie;
        if (attachment != null) {
            this.setAttachment(attachment);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((agentId == null) ? 0 : agentId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Client other = (Client) obj;
        if (agentId == null) {
            if (other.agentId != null)
                return false;
        } else if (!agentId.equals(other.agentId))
            return false;
        return true;
    }

    public AgentID getAgentId() {
        return this.agentId;
    }

    /** Send a message to this client
     * 
     * If the client is not connected or if an error occurs while sending the message
     * the Exception is thrown to the caller.
     * 
     * @param message the message
     * @throws IOException if failed to send the message
     */
    public void sendMessage(ByteBuffer message) throws IOException {
        // attachment is not allowed to change while sending the message
        synchronized (this.attachment_lock) {
            try {
                if (this.attachment != null) {
                    this.attachment.send(message);
                } else {
                    throw new IOException("Client " + this.agentId + " is not connected");
                }
            } catch (IOException e) {
                // The tunnel just failed. Discard the current attachment and
                // wait
                // for client reconnection
                this.discardAttachment("Exception caught while sending a message: " + e.getMessage());
                throw e;
            }
        }
    }

    /** Send a message to this client of cache it until the client is availble
     * 
     * If the client is not connected or if an error occurs while sending the message,
     * the message is put in the pending message queue and will be sent to the client
     * as soon as it reconnect.
     *
     * @param message the message
     */
    public void sendMessageOrCache(ByteBuffer message) {
        // attachment is not allowed to change while sending the message
        synchronized (this.attachment_lock) {
            try {
                if (this.attachment != null) {
                    attachment.send(message);
                } else {
                    this.pendingMessage.add(message);
                }
            } catch (IOException e) {
                // The tunnel just failed. Discard the current attachment and
                // wait
                // for client reconnection
                this.discardAttachment("Exception caught while sending a message: " + e.getMessage());
                this.pendingMessage.add(message);
            }
        }
    }

    /** Send a message to this client
     * 
     * If the client is not connected or if an error occurs while sending the message
     * the Exception is thrown to the caller.
     * 
     * @param message the message
     * @throws IOException if failed to send the message
     */
    public void sendMessage(byte[] message) throws IOException {
        this.sendMessage(ByteBuffer.wrap(message));
    }

    /** Send a message to this client of cache it until the client is availble
     * 
     * If the client is not connected or if an error occurs while sending the message,
     * the message is put in the pending message queue and will be sent to the client
     * as soon as it reconnect.
     *
     * @param message the message
     */
    public void sendMessageOrCache(byte[] message) {
        this.sendMessageOrCache(ByteBuffer.wrap(message));
    }

    /** Discard the current attachment 
     * 
     * Must be called when an IOException is raised by a read or write operation on the 
     * socket. It indicates the tunnel failed and the client must reconnect.
     */
    public void discardAttachment(String cause) {
        synchronized (this.attachment_lock) {
            if (admin_logger.isDebugEnabled() && this.attachment != null) {
                admin_logger.debug("AgentID " + this.getAgentId() + " disconnected: " + cause);
            }

            logger.debug("Discarded attachment for " + this.agentId);
            this.attachment = null;
        }
    }

    /** Set a new attachment for this client
     * 
     * A correct locking implies that the attachment is set to null when
     * this method is called 
     * 
     * @param attachment the new attachment
     */
    public void setAttachment(Attachment attachment) {
        synchronized (this.attachment_lock) {
            if (this.attachment != null) {
                logger.warn("set attachment called on client #" + this.agentId +
                            " but attachment is not null. Race condition occured !");
            }

            logger.debug("New attachment for " + this.agentId);
            this.attachment = attachment;
            this.attachment.setClient(this);

            if (admin_logger.isDebugEnabled()) {
                if (this.lastSeen.get() == 0) {
                    admin_logger.debug("AgentID " + this.getAgentId() + " connected from " +
                                       this.attachment.getAgentHostname() + " (TCP endpoint: " +
                                       this.attachment.getRemoteEndpointName() + ")");
                } else {
                    admin_logger.debug("AgentID " + this.getAgentId() + " reconnected from " +
                                       this.attachment.getAgentHostname() + " (TCP endpoint: " +
                                       this.attachment.getRemoteEndpointName() + ")");
                }
            }

        }
    }

    /** Send the pending message to the client */
    public void sendPendingMessage() {
        /*
         * Coarse grained locking: should be improved
         *
         * This lock currently ensure that their is no race condition
         * between add() and peek()
         * The justification of this global lock is that it is safe and easy to
         * implement _AND_ flushing all the pending messages before forwarding new ones
         * sound reasonable.
         */
        synchronized (this.attachment_lock) {
            ByteBuffer msg;
            while ((msg = this.pendingMessage.peek()) != null) {
                try {
                    this.sendMessage(msg);
                    this.pendingMessage.remove(msg);
                } catch (Exception e) {
                    // The tunnel failed again and the attachment has been set
                    // to null. Nothing we can do. This method will be called again on
                    // client connection
                    break;
                }
            }
        }
    }

    /**
     * 
     * @return true if the attachment is not null.
     */
    public boolean isConnected() {
        synchronized (this.attachment_lock) {
            return this.attachment != null;
        }
    }

    /**
     * Update the lastseen timestamp
     *
     * This timestamp can be used to know when the router saw
     * network traffic from the client for the last time.
     */
    public void updateLastSeen() {
        this.lastSeen.set(System.currentTimeMillis());

    }

    /**
     * @return the lastseen timestamp
     */
    public long getLastSeen() {
        return this.lastSeen.get();
    }

    /** Close the connection to the remote client and discard the attachment.
     *
     * This method can be used to disconnect a client if something goes wrong
     * (invalid message, late heartbeat etc.).
     *
     * Once the connection is closed, the client will detect a broken Tunnel and
     * reopen a new one.
     *
     * @throws IOException If the client cannot be disconneted. The attachement
     * is discarded anyway.
     */
    public void disconnect() throws IOException {
        synchronized (attachment_lock) {
            if (this.attachment != null) {
                try {
                    this.attachment.disconnect();
                } finally {
                    discardAttachment("disconnect called");
                }
            }
        }
    }

    public MagicCookie getMagicCookie() {
        return this.magicCookie;
    }

    @Override
    public String toString() {
        return "Agent id=" + this.agentId + " remote endpoint=" +
               (isConnected() ? this.attachment.getRemoteEndpointName() : "not connected");
    }
}
