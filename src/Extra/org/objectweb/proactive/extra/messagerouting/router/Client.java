/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;


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
    static final private Logger logger = ProActiveLogger.getLogger(Loggers.FORWARDING_ROUTER);
    public static final Logger admin_logger = ProActiveLogger.getLogger(Loggers.FORWARDING_ROUTER_ADMIN);

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

    public Client(Attachment attachment, AgentID agentID) {
        this.attachment = attachment;
        this.attachment.setClient(this);
        this.agentId = agentID;
        this.pendingMessage = new ConcurrentLinkedQueue<ByteBuffer>();

        if (admin_logger.isDebugEnabled()) {
            admin_logger.debug("AgentID " + this.getAgentId() + " connected from " +
                this.attachment.getRemoteEndpoint());
        }

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
                this.discardAttachment();
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
                this.discardAttachment();
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
    public void discardAttachment() {
        synchronized (this.attachment_lock) {
            if (admin_logger.isDebugEnabled()) {
                admin_logger.debug("AgentID " + this.getAgentId() + " disconnected");
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
                logger.warn("set attachment called but attachment is not null. Race condition occured !");
            }

            logger.debug("New attachment for " + this.agentId);
            this.attachment = attachment;
            this.attachment.setClient(this);

            if (admin_logger.isDebugEnabled()) {
                admin_logger.debug("AgentID " + this.getAgentId() + " reconnected from " +
                    this.attachment.getRemoteEndpoint());
            }

        }
    }

    /** Send the pending message to the client */
    public void sendPendingMessage() {
        /* Coarse grained locking: should be improved
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
}