/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;

import java.io.IOException;
import java.net.URI;


/**
 *
 *
 * Remote interface for a remote object.
 *
 *
 */
public interface RemoteRemoteObject {

    /**
     * Send a message containing a reified method call to a remote object. the target of the message
     * could be either the reified object or the remote object itself
     * @param message the reified method call
     * @return a reply containing the result of the method call
     * @throws ProActiveException
     * @throws RenegotiateSessionException if the security infrastructure needs to (re)initiate the session
     * @throws IOException if the message transfer has failed
     */
    public Reply receiveMessage(Request message) throws ProActiveException, IOException,
            RenegotiateSessionException;

    /**
     * Return the protocol dependant URI of the remote remote object
     * @return uri of the protocol dependant RO
     * @throws ProActiveException
     * @throws IOException
     */
    public URI getURI() throws ProActiveException, IOException;
}
