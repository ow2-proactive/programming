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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An object implementing this interface provides the minimum service a body offers
 * remotely or locally. This interface is the generic version that is used remotely
 * and locally. A body accessed from the same JVM offers all services of this interface,
 * plus the services defined in the Body interface.
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 * @see org.objectweb.proactive.Body
 * @see org.objectweb.proactive.core.body.rmi.RmiBodyAdapter
 */
//@snippet-start universalbody
public interface UniversalBody extends Serializable {
    public static Logger bodyLogger = ProActiveLogger.getLogger(Loggers.BODY);

    public static Logger sendReplyExceptionsLogger = ProActiveLogger.getLogger(Loggers.EXCEPTIONS_SEND_REPLY);

    /**
     * Receives a request for later processing. The call to this method is non blocking
     * unless the body cannot temporary receive the request.
     * @param request the request to process
     * @exception java.io.IOException if the request cannot be accepted 
     */
    public void receiveRequest(Request request) throws java.io.IOException;

    /**
     * Receives a reply in response to a former request.
     * @param r the reply received
     * @exception java.io.IOException if the reply cannot be accepted
     */
    public void receiveReply(Reply r) throws java.io.IOException;

    /**
     * Returns the url of the node this body is associated to
     * The url of the node can change if the active object migrates
     * @return the url of the node this body is associated to
     */
    public String getNodeURL();

    /**
     * Returns the UniqueID of this body
     * This identifier is unique accross all JVMs
     * @return the UniqueID of this body
     */
    public UniqueID getID();

    /**
     * Return the name of this body, which generally contains the class name of the reified object
     * @return the body name
     */
    public String getName();

    /**
     * Signals to this body that the body identified by id is now to a new
     * remote location. The body given in parameter is a new stub pointing
     * to this new location. This call is a way for a body to signal to his
     * peer that it has migrated to a new location
     * @param id the id of the body
     * @param body the stub to the new location
     * @exception java.io.IOException if a pb occurs during this method call
     */
    public void updateLocation(UniqueID id, UniversalBody body) throws java.io.IOException;

    /**
     * Returns the remote friendly version of this body
     * @return the remote friendly version of this body
     */
    public UniversalBody getRemoteAdapter();

    /**
     * Returns the name of the class of the reified object
     * @return the name of the class of the reified object
     */
    public String getReifiedClassName();

    /**
     * Enables automatic continuation mechanism for this body
     * @exception java.io.IOException if a problem occurs during this method call
     */
    public void enableAC() throws java.io.IOException;

    /**
     * Disables automatic continuation mechanism for this body
     * @exception java.io.IOException if a problem occurs during this method call
     */
    public void disableAC() throws java.io.IOException;

    /**
     * For sending a non functional heartbeat message.
     * @return depends on the message meaning
     * @exception java.io.IOException if a problem occurs during this method call
     */
    public Object receiveHeartbeat() throws IOException;

    public String registerByName(String name, boolean rebind) throws IOException, ProActiveException;

    /**
     * Tries to interrupt the current service on this body
     * This call must not done from within an active object method, as it would interrupt itself.
     *
     * Depending on how the runActivity is implemented, it may or may not terminate the ActiveObject.
     *
     * In standard fifo or lifo serving, it will not terminate the AO
     * @throws IllegalStateException if the body is inactive
     */
    public void interruptService() throws IllegalStateException;

    /**
     * @return The URL of this body (using the default remote object factory)
     */
    public String getUrl();

    /**
     * @return in case of multi protocols, all urls of this body
     */
    public String[] getUrls();

    public String registerByName(String name, boolean rebind, String protocol) throws IOException, ProActiveException;
}
//@snippet-end universalbody
