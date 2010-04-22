/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
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
package org.objectweb.proactive.extra.messagerouting.protocol.message;

import org.objectweb.proactive.extra.messagerouting.exceptions.MalformedMessageException;


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
                "Invalid value for the " + Message.Field.MSG_TYPE + " field:" + this.getType());
        }

        if (getSrcAgentId() != null) {
            throw new MalformedMessageException("Invalid field " + HeartbeatMessage.Field.SRC_AGENT_ID +
                " must be null");
        }
    }

}
