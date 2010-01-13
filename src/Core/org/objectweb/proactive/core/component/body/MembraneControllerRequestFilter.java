/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * A request filter for a component in the Membrane stopped state
 * @author The ProActive Team
 *
 */
public class MembraneControllerRequestFilter implements RequestFilter {

    /**
     * 
     */
    private static final long serialVersionUID = 420L;

    public boolean acceptRequest(Request request) {
        if (request instanceof ComponentRequest) {
            MethodCall mc = ((ComponentRequest) request).getMethodCall();
            String itfName = mc.getComponentMetadata().getComponentInterfaceName();
            return itfName.equals(Constants.MEMBRANE_CONTROLLER) ||
                itfName.equals(Constants.CONTENT_CONTROLLER) || itfName.equals(Constants.NAME_CONTROLLER) ||
                itfName.equals(Constants.SUPER_CONTROLLER) || itfName.equals(Constants.BINDING_CONTROLLER);
        }
        return false;
    }

}
