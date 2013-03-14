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
package org.objectweb.proactive.core.body.request;

/**
 * <p>
 * A class implementing this interface can process requests.
 * </p><p>
 * It implements one method <code>processRequest</code> that takes a request
 * and returns an int saying whether
 * the request shall be removed and served, removed without serving or kept.
 * </p><p>
 * It is used as a call back interface allowing a custom processing on request
 * stored in the request queue.
 * </p><p>
 * Typically it can be used to serve requests stored in a request queue in a
 * custom manner. When doing custom processing, a request should
 * be REMOVED from the queue BEFORE serving.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public interface RequestProcessor {

    /** Constant indicating that the request shall be removed and served. */
    public final static int REMOVE_AND_SERVE = 1;

    /** Constant indicating that the request shall removed without being served. */
    public final static int REMOVE = 2;

    /** Constant indicating that the request shall be kept. */
    public final static int KEEP = 3;

    /**
     * Returns one of the constants indicating the desired treatment for the request.
     * @param request the request to process
     * @return one of the three constants above
     */
    public int processRequest(Request request);

    /**
     * An exception to throw instead of serving the method
     * @return
     */
    public Throwable getExceptionToThrow();
}
