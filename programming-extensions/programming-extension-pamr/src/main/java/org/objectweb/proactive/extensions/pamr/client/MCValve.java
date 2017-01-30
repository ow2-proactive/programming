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

import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.extensions.pamr.protocol.message.Message;


public class MCValve implements Valve {
    ConcurrentHashMap<Long, Long> map = new ConcurrentHashMap<Long, Long>();

    public String getInfo() {
        return "Print statistics about calls";
    }

    public Message invokeIncoming(Message message) {
        long messageId = message.getMessageID();
        switch (message.getType()) {
            case DATA_REQUEST:
                break;
            default:
                // We don't care
                break;
        }

        return message;
    }

    public Message invokeOutgoing(Message message) {

        // TODO Auto-generated method stub
        return null;
    }

}
