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
import org.objectweb.proactive.extensions.pamr.protocol.message.Message.MessageType;


/** A {@link MessageType#DATA_REQUEST} message
 * 
 * @since ProActive 4.1.0
 */
public class DataRequestMessage extends DataMessage {

    /** Create a {@link MessageType#DATA_REQUEST} message
     * 
     * @param sender
     * 		sender of the request
     * @param recipient
     * 		recipient of the request
     * @param msgID
     * 		an unique ID per sender.
     * @param data
     * 		the payload
     */
    public DataRequestMessage(AgentID sender, AgentID recipient, long msgID, byte[] data) {
        super(MessageType.DATA_REQUEST, sender, recipient, msgID, data);
    }

    /** Create a {@link MessageType#DATA_REQUEST} message from a byte array
     * 
     * @param buf 
     *		a buffer which contains a message
     * @param offset 
     * 		the offset at which the message begins  
     * @throws MalformedMessageException
     * 		If the buffer does not match message requirements (proto ID, length etc.)
     */
    public DataRequestMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset);

        if (this.getType() != MessageType.DATA_REQUEST) {
            throw new MalformedMessageException("Malformed" + MessageType.DATA_REQUEST + " message:" +
                                                "Invalid value for " + Message.Field.MSG_TYPE + " field:" +
                                                this.getType());
        }
    }
}
