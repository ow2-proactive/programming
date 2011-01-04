/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.core.security;

import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


/**
 * A message implementing this interface can be handled by the security
 * mechanism.
 *
 * This interface <b>must</b> be implemented by all messages exchanged
 * between security entities. It includes Request, Reply, BodyRequest, etc.
 *
 *
 */
public interface Securizable {

    /**
     * @return return true if the message is ciphered
     */
    public boolean isCiphered();

    /**
     * @return return session Id identifying the security
     * session
     */
    public long getSessionId();

    /**
     * @param psm ProActiveSecurityManager
     * @return true if decrypt method succeeds
     * @throws RenegotiateSessionException This exception is thrown when a session
     * corresponding to the sessionId is not found. The error must be returned to the sender
     * in order to renegociate a new session and re-send the message
     */
    public boolean decrypt(ProActiveSecurityManager psm) throws RenegotiateSessionException;

    /**
     * @param psm the proactiveSecurityManager of the entity
     * @return true the message can be emitted.
     * @throws RenegotiateSessionException
     */
    public boolean crypt(ProActiveSecurityManager psm, SecurityEntity destinationBody)
            throws RenegotiateSessionException;
}
