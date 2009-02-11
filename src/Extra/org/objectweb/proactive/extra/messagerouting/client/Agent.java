package org.objectweb.proactive.extra.messagerouting.client;

import java.net.URI;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.extra.messagerouting.exceptions.MessageRoutingException;
import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;
import org.objectweb.proactive.extra.messagerouting.protocol.message.DataRequestMessage;


/**
 * A message routing client
 * 
 * The {@link Agent} maintains a tunnel opens between the local
 * {@link ProActiveRuntime} and the remote message router. It is in charge of
 * sending and receiving all the messages.
 * 
 * @since ProActive 4.1.0
 */
public interface Agent {

    /** Send a message to a remote {@link Agent}.
     * 
     * if oneWay, the result returned is null. if not, this call is blocked
     * until an answer is provided.
     * 
     * @param targetID
     *            the remote {@link AgentID}
     * @param data
     *            the data to send.
     * @param oneWay
     * 			  if a response is expected or not.
     * @return the data response.
     * @throws MessageRoutingException
     *             if the message cannot be send to the recipient
     */
    public byte[] sendMsg(AgentID targetID, byte[] data, boolean oneWay) throws MessageRoutingException;

    /** Send a message to a remote {@link Agent}.
     * 
     * if oneWay, the the result returned is null. if not, the this call is
     * blocked until an answer is provided.
     * 
     * @param targetURI
     *            the URI of the remote {@link Agent}.
     * @param data
     *            the data to send.
     * @param oneWay
     * @return the data response.
     * @throws ForwardingException
     *             if the timeout is reached.
     */
    public byte[] sendMsg(URI targetURI, byte[] data, boolean oneWay) throws MessageRoutingException;

    /** Send the reply to a message
     * 
     * @param request
     * 			The request correlated to this response
     * @param data
     * 			The response
     * @throws MessageRoutingException
     * 			If the response cannot be sent
     */
    public void sendReply(DataRequestMessage request, byte[] data) throws MessageRoutingException;

    /** Return the local Agent ID */
    public AgentID getAgentID();

}
