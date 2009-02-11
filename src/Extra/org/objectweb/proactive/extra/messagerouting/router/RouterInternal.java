package org.objectweb.proactive.extra.messagerouting.router;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;


/**
 *  
 * <i>This class cannot be package private because of the processor subpackage and test</i>
 * 
 * @since ProActive 4.1.0
 */
public abstract class RouterInternal extends Router {
    /** Submit a job to be executed asynchronously. 
     * 
     * All time consuming tasks should be submitted by using this method. The single threaded
     * front end should not execute any other code than reading data chunk from {@link SocketChannel}.
     * 
     * @param message the received message to be handled
     * @param attachment the attachment used to received the message
     */
    abstract public void handleAsynchronously(ByteBuffer message, Attachment attachment);

    /** Returns the client corresponding to a given {@link AgentID}
     * 
     * @param agentId the {@link AgentID}
     * @return the corresponding client or null is unknonwn
     */
    abstract public Client getClient(AgentID agentId);

    /** Add a new client to the routing table
     * 
     * @param client the new client
     */
    abstract public void addClient(Client client);
}
