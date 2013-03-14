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
package org.objectweb.proactive.core.body;

import java.io.IOException;

import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.body.future.FuturePool;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


/**
 * An object implementing this interface is an implementation of one part
 * of the local view of the body of an active object. This interface define
 * only one part of the local view and is used to be able to change easily the
 * strategy of a body. Typically, after a body migrates, it is necessary to change
 * the its local implementation.
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 */
public interface LocalBodyStrategy {

    /**
     * Returns the future pool of this body
     * @return the future pool of this body
     */
    public FuturePool getFuturePool();

    /**
     * Returns the request queue associated to this body
     * @return the request queue associated to this body
     */
    public BlockingRequestQueue getRequestQueue();

    /**
     * Returns the reified object that body is for
     * The reified object is the object that has been turned active.
     * @return the reified object that body is for
     */
    public Object getReifiedObject();

    /**
     * Sends the request <code>request</code> with the future <code>future</code> to the local body
     * <code>body</code>.
     * @param methodCall the methodCall to send
     * @param future the future associated to the request
     * @param destinationBody the body the request is sent to
     * @exception java.io.IOException if the request cannot be sent to the destination body
     * @throws CommunicationForbiddenException
     */
    public void sendRequest(MethodCall methodCall, Future future, UniversalBody destinationBody)
            throws IOException, RenegotiateSessionException, CommunicationForbiddenException;

    /**
     * Serves the request <code>request</code> by the invoking the targeted method on the
     * reified object. Some specific type of request may involve special processing that
     * does not trigger a method on the reified object.
     * @param request the request to serve
     */
    public void serve(Request request);

    /**
     * Instead of serving the request <code>request</code>, returns the exception given in parameter to the caller
     *
     * @param request the request to serve
     * @param request the exception to return
     */
    public void serveWithException(Request request, Throwable exception);

    /**
     * Returns a unique identifier that can be used to tag a future, a request
     * @return a unique identifier that can be used to tag a future, a request.
     */
    public long getNextSequenceID();
}
