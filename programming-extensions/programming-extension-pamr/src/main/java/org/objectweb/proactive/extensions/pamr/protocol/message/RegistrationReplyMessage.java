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

import org.objectweb.proactive.extensions.pamr.exceptions.MalformedMessageException;
import org.objectweb.proactive.extensions.pamr.protocol.AgentID;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;


/** 
 * 
 * @since ProActive 4.1.0
 */
public class RegistrationReplyMessage extends RegistrationMessage {

    public RegistrationReplyMessage(AgentID agentID, long messageId, long routerId, MagicCookie magicCookie,
            int heartbeatPeriod) {
        super(MessageType.REGISTRATION_REPLY, messageId, agentID, routerId, magicCookie, heartbeatPeriod);
    }

    /**
     * Construct a message from the data contained in a formatted byte array.
     * @param byteArray the byte array from which to read
     * @param offset the offset at which to find the message in the byte array
     * @throws MalformedMessageException if the buffer does not contain a valid RegistrationReplyMessage
     */
    public RegistrationReplyMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset);

        if (this.getType() != MessageType.REGISTRATION_REPLY) {
            throw new MalformedMessageException("Malformed " + MessageType.REGISTRATION_REPLY + " message:" +
                                                "Invalid value for the " + Message.Field.MSG_TYPE + " field:" +
                                                this.getType());
        }

        if (this.getRouterID() <= 0) {
            throw new MalformedMessageException("Malformed " + MessageType.REGISTRATION_REPLY + " message:" +
                                                "Invalid value for the " + Field.ROUTER_ID + " field:" +
                                                this.getRouterID());
        }
    }
}
