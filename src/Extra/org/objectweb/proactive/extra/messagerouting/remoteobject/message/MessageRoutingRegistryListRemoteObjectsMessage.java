/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.remoteobject.message;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.extra.messagerouting.client.Agent;
import org.objectweb.proactive.extra.messagerouting.remoteobject.util.MessageRoutingRegistry;


/** Represents a MessageRoutingMessage. 
 * 
 * When processed, this message list all the remote objects registered on the receiver.
 *
 * @since ProActive 4.1.0
 */

public class MessageRoutingRegistryListRemoteObjectsMessage extends MessageRoutingMessage implements
        Serializable {

    /**
     * Construct a list message
     * 
     * @param uri
     *            the URI of the remote runtime to be retrieved, only the agent
     *            id part is used for this type of message. The path part is ignored
     * @param agent
     *            the local agent to use to send the message
     */
    public MessageRoutingRegistryListRemoteObjectsMessage(URI uri, Agent agent) {
        super(uri, agent);
    }

    /** Get the list of the objects registered on the remote runtime */
    public URI[] getReturnedObject() {
        return (URI[]) this.returnedObject;
    }

    @Override
    public Object processMessage() {
        if (logger.isTraceEnabled()) {
            logger.trace("Executing a list message");
        }

        return MessageRoutingRegistry.singleton.list();
    }
}
