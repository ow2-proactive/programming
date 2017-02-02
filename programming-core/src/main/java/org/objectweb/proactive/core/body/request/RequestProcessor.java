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
