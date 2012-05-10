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

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>
 * A class implementing this interface can filter requests on some criteria.
 * It implements one method <code>acceptRequest</code> that takes a request
 * and returns a boolean saying whether the request.
 * </p><p>
 * It is used as a call back interface allowing a custom selection on request
 * stored in the request queue
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
@PublicAPI
public interface RequestFilter extends Serializable {

    /**
     * Returns true if and only if the given request can be accepted.
     * @param request the request to test
     * @return true if the request can be accepted, false else.
     */
    public boolean acceptRequest(Request request);
}
