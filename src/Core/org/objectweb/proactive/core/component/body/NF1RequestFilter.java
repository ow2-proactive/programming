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

import org.etsi.uri.gcm.api.control.PriorityController;
import org.etsi.uri.gcm.api.control.PriorityController.RequestPriority;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestFilter;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.util.NonFunctionalServices;


/**
 * A request filter for prioritized component requests.
 * (experimental)
 *
 * @author The ProActive Team
 */
public class NF1RequestFilter implements RequestFilter {
    private PriorityController pc;

    public NF1RequestFilter(PriorityController pc) {
        this.pc = pc;
    }

    public boolean acceptRequest(Request request) {
        if (request instanceof ComponentRequest) {
            try {
                return (((ComponentRequest) request).isControllerRequest() &&
                    !pc.getGCMPriority(null, request.getMethodName(), null).equals(RequestPriority.NF2) && !pc
                        .getGCMPriority(null, request.getMethodName(), null).equals(RequestPriority.NF3)) ||
                    (request.getMethodName().equals(NonFunctionalServices.terminateAOMethod.getName()));
            } catch (NoSuchInterfaceException e) {
                // ignore
                return false;
            } catch (NoSuchMethodException e) {
                // ignore
                return false;
            }

        } else {
            // standard requests cannot be component controller requests
            return false;
        }
    }
}
