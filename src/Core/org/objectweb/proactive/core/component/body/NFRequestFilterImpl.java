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
package org.objectweb.proactive.core.component.body;

import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.util.NonFunctionalServices;


/**
 * This class is a filter for non functional component requests : it can separate
 * component controller requests from component functional requests.
 *
 * @author The ProActive Team
 */
public class NFRequestFilterImpl implements RequestFilter, java.io.Serializable {
    public NFRequestFilterImpl() {
    }

    /**
     * This methods verifies whether a request is a component controller request.
     * @param request the request to filter
     * @return true if the request is a component controller request, false otherwise
     */
    public boolean acceptRequest(Request request) {
        if (request instanceof ComponentRequest) {
            return acceptRequest((ComponentRequest) request);
        } else if (request.getMethodName().equals(NonFunctionalServices.terminateAOMethod.getName())) {
            // special case for terminating the component
            return true;
        } else {
            // standard requests cannot be component controller requests
            return false;
        }
    }

    /**
     * This methods verifies whether a component request is a component controller request.
     * @param request the component request to filter
     * @return true if the request is a component controller request, false otherwise
     */
    public boolean acceptRequest(ComponentRequest request) {
        return request.isControllerRequest();
    }
}
