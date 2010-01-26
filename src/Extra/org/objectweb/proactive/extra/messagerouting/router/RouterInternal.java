/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
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
