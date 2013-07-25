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
package org.objectweb.proactive.core.component.adl.interceptors;

import org.objectweb.proactive.core.component.interception.Interceptor;


/**
 * A builder interface for adding interceptors to functional interfaces.
 *
 * @author The ProActive Team
 */
public interface InterceptorBuilder {
    /**
     * Adds at the last position the {@link Interceptor interceptor} with the specified ID to the interface
     * with the specified name owned by the specified component.
     * 
     * @param component Component owning the interface on which to add the {@link Interceptor interceptor}.
     * @param interfaceName Name of the interface on which to add the {@link Interceptor interceptor}.
     * @param interceptorID ID of the {@link Interceptor interceptor} to add.
     * @throws Exception If there is an error while adding the {@link Interceptor interceptor} to the interface.
     */
    public void addInterceptor(Object component, String interfaceName, String interceptorID) throws Exception;
}
