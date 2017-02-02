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
package org.objectweb.proactive.extensions.pamr.remoteobject.message;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.extensions.pamr.client.Agent;
import org.objectweb.proactive.extensions.pamr.remoteobject.util.PAMRRegistry;


/** Represents a MessageRoutingMessage. 
 * 
 * When processed, this message list all the remote objects registered on the receiver.
 *
 * @since ProActive 4.1.0
 */

public class PAMRRegistryListRemoteObjectsMessage extends PAMRMessage implements Serializable {

    /**
     * Construct a list message
     * 
     * @param uri
     *            the URI of the remote runtime to be retrieved, only the agent
     *            id part is used for this type of message. The path part is ignored
     * @param agent
     *            the local agent to use to send the message
     */
    public PAMRRegistryListRemoteObjectsMessage(URI uri, Agent agent) {
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

        return PAMRRegistry.singleton.list();
    }
}
