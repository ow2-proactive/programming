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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;


/**
 *
 *
 * Remote interface for a remote object.
 *
 * It is declared Serializable as the stub to this RemoteObject will be transferred via the network.
 * On the contrary the RemoteObject interface -  which should exist only on the client side - is not Serializable.
 * It is similar to RMI Remote(Not Serializable) and Remote Object (Serializable)
 *
 *
 */
public interface RemoteRemoteObject extends Serializable {

    /**
     * Send a message containing a reified method call to a remote object. the target of the message
     * could be either the reified object or the remote object itself
     * @param message the reified method call
     * @return a reply containing the result of the method call
     * @throws ProActiveException
     * @throws IOException if the message transfer has failed
     */
    public Reply receiveMessage(Request message) throws ProActiveException, IOException;

    /**
     * Return the protocol dependant URI of the remote remote object
     * @return uri of the protocol dependant RO
     * @throws ProActiveException
     * @throws IOException
     */
    public URI getURI() throws ProActiveException, IOException;
}
