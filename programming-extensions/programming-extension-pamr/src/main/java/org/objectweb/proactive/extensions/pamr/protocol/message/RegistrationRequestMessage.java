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
package org.objectweb.proactive.extensions.pamr.protocol.message;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.TypeHelper;
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;


/** A {@link MessageType#REGISTRATION_REQUEST} message
 * 
 * A such message is sent when a client connect or reconnect to the router.
 * 
 * A message request is a registration message with an additional field: The hostname of the agent
 * to help troubbleshooting. If the agent is behind a NAT it is hard to understand who is the agent from
 * the router point of view. For backward compatibility this field is optional.
 * 
 * @since ProActive 4.1.0
 */
public class RegistrationRequestMessage extends RegistrationMessage {
    // Optional agent hostname field to help troubbleshouting at runtime
    byte[] agentHostname = null;

    final int AGENT_HOSTNAME_OFFSET = Message.Field.getTotalOffset() + RegistrationMessage.Field.getTotalOffset();

    /** Create a {@link MessageType#REGISTRATION_REQUEST} message
     * 
     * @param agentID
     * 		The client {@link AgentID}, or null if not known 
     * @param messageId
     * 		An unique message ID per sender.
     */
    public RegistrationRequestMessage(AgentID agentID, long messageId, long routerId, MagicCookie magicCookie) {
        super(MessageType.REGISTRATION_REQUEST, messageId, agentID, routerId, magicCookie, 0);

        try {
            this.agentHostname = InetAddress.getLocalHost().getHostName().getBytes();
            this.setLength(AGENT_HOSTNAME_OFFSET + Integer.SIZE + this.agentHostname.length);
        } catch (UnknownHostException e) {
        }

    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     * @param byteArray the byte array from which to read
     * @param offset the offset at which to find the message in the byte array
     * @throws MalformedMessageException
     */
    public RegistrationRequestMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset);

        if (this.getType() != MessageType.REGISTRATION_REQUEST) {
            throw new MalformedMessageException("Malformed " + MessageType.REGISTRATION_REQUEST + " message:" +
                                                "Invalid value for the " + Message.Field.MSG_TYPE + " field:" +
                                                this.getType());
        }

        if (this.getRouterID() < 0 && this.getRouterID() != Long.MIN_VALUE) {
            throw new MalformedMessageException("Malformed " + MessageType.REGISTRATION_REQUEST + " message:" +
                                                "Invalid value for the " + Field.ROUTER_ID + " field:" +
                                                this.getRouterID());
        }

        if (this.getLength() > AGENT_HOSTNAME_OFFSET) {
            int l = TypeHelper.byteArrayToInt(byteArray, AGENT_HOSTNAME_OFFSET);
            if (l < 0) {
                throw new MalformedMessageException("Malformed " + MessageType.REGISTRATION_REQUEST + " message:" +
                                                    "Invalid length for the hostname field:" + l);
            }

            agentHostname = new byte[l];
            System.arraycopy(byteArray, AGENT_HOSTNAME_OFFSET + Integer.SIZE, agentHostname, 0, l);
        }

    }

    @Override
    public byte[] toByteArray() {
        byte[] buf = super.toByteArray();

        // Add the optional agent hostname field
        // The data are prefixed by the field length to allow further extension of this class. 
        if (agentHostname != null) {
            TypeHelper.intToByteArray(agentHostname.length, buf, AGENT_HOSTNAME_OFFSET);
            System.arraycopy(agentHostname, 0, buf, AGENT_HOSTNAME_OFFSET + Integer.SIZE, agentHostname.length);
        }

        return buf;
    }

    public String getAgentHostname() {
        if (agentHostname != null)
            return new String(agentHostname);

        return null;
    }
}
