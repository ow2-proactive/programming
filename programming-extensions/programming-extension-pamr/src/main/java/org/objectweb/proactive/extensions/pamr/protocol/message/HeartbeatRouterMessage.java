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


/**
 * An heartbeat sends by the router to the clients.
 *
 * {@link HeartbeatMessage.Field#SRC_AGENT_ID} is always null (see the magic number} since
 * the router does not have an AgentID
 *
 * @since ProActive 4.3.0
 */
public class HeartbeatRouterMessage extends HeartbeatMessage {

    public HeartbeatRouterMessage(long heartbeatId) {
        super(MessageType.HEARTBEAT_ROUTER, heartbeatId, null);
    }

    public HeartbeatRouterMessage(byte[] byteArray, int offset) throws MalformedMessageException {
        super(byteArray, offset);

        if (this.getType() != MessageType.HEARTBEAT_ROUTER) {
            throw new MalformedMessageException("Malformed" + MessageType.HEARTBEAT_ROUTER + " message:" +
                                                "Invalid value for the " + Message.Field.MSG_TYPE + " field:" +
                                                this.getType());
        }

        if (getSrcAgentId() != null) {
            throw new MalformedMessageException("Invalid field " + HeartbeatMessage.Field.SRC_AGENT_ID +
                                                " must be null");
        }
    }

}
