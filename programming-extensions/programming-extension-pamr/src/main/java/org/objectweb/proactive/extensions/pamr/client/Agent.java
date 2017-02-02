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
package org.objectweb.proactive.extensions.pamr.client;

import java.net.URI;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.extensions.pamr.exceptions.PAMRException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.message.DataRequestMessage;


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
     * @throws PAMRException
     *             if the message cannot be send to the recipient
     */
    public byte[] sendMsg(AgentID targetID, byte[] data, boolean oneWay) throws PAMRException;

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
    public byte[] sendMsg(URI targetURI, byte[] data, boolean oneWay) throws PAMRException;

    /** Send the reply to a message
     * 
     * @param request
     * 			The request correlated to this response
     * @param data
     * 			The response
     * @throws PAMRException
     * 			If the response cannot be sent
     */
    public void sendReply(DataRequestMessage request, byte[] data) throws PAMRException;

    /** Return the local Agent ID */
    public AgentID getAgentID();

    /** Close the current tunnel
     *
     * The agent will eventually reconnect to the router.
     *
     * @param cause
     *          Cause of the failure
     */
    public void closeTunnel(PAMRException cause);
}
