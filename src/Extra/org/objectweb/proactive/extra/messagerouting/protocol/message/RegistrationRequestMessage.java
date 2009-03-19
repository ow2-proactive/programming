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
package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.extra.messagerouting.protocol.AgentID;


/** A {@link MessageType#REGISTRATION_REQUEST} message
 * 
 * A such message is sent when a client connect or reconnect to the router.
 * 
 * @since ProActive 4.1.0
 */
public class RegistrationRequestMessage extends RegistrationMessage {

    /** Create a {@link MessageType#REGISTRATION_REQUEST} message
     * 
     * @param agentID
     * 		The client {@link AgentID}, or null if not known 
     * @param messageId
     * 		An unique message ID per sender.
     */
    public RegistrationRequestMessage(AgentID agentID, long messageId) {
        super(MessageType.REGISTRATION_REQUEST, messageId, agentID);
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     * @param byteArray the byte array from which to read
     * @param offset the offset at which to find the message in the byte array
     * @throws InstantiationException
     */
    public RegistrationRequestMessage(byte[] byteArray, int offset) throws IllegalArgumentException {
        super(byteArray, offset);

        if (this.getType() != MessageType.REGISTRATION_REQUEST) {
            throw new IllegalArgumentException("Invalid message type " + this.getType());
        }
    }

}
