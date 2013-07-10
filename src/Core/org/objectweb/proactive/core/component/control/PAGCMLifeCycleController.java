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
package org.objectweb.proactive.core.component.control;

import org.etsi.uri.gcm.api.control.GCMLifeCycleController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Extension of the standard GCM {@link GCMLifeCycleController life cycle controller}.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface PAGCMLifeCycleController extends GCMLifeCycleController {
    /**
     * Terminates the component to which this interface belongs.
     * 
     * @param immediate
     *            If this boolean is true, this method is served as an immediate service. The
     *            termination is then synchronous. The component dies immediately. Else, the kill
     *            request is served as a normal request, it is put on the request queue. The
     *            termination is asynchronous.
     * @throws IllegalLifeCycleException
     *             If the component to which this interface belongs is not in an
     *             appropriate state to perform this operation.
     */
    public void terminateGCMComponent(boolean immediate) throws IllegalLifeCycleException;
}
