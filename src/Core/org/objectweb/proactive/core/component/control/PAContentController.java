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

import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.exceptions.ContentControllerExceptionListException;


/**
 * Extension of the standard Fractal {@link ContentController content controller}.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface PAContentController extends ContentController {
    /**
     * Adds a list of sub components, possibly in parallel. This method delegates the addition
     * of individual components to the
     * {@link ContentController#addFcSubComponent(org.objectweb.fractal.api.Component)}
     * method, and implementations of this method can parallelize
     * the addition of the members of the list to the content of this component.
     *
     * @param subComponents the components to be added inside this component.
     * @throws ContentControllerExceptionListException if the addition of one or several components
     *         failed. This exception lists the components that were not added and the
     *         exception that occurred.
     */
    public void addFcSubComponent(List<Component> subComponents)
            throws ContentControllerExceptionListException;

    /**
     * Removes a list of sub-components from this component, possibly in parallel.
     * This method delegates the removal of individual components to the
     * {@link ContentController#removeFcSubComponent(org.objectweb.fractal.api.Component)}
     * method, and implementations of this method can parallelize the
     * removal of the members of the list from the content of this component.
     *
     * @param subComponents the list of components to be removed from this component.
     * @throws ContentControllerExceptionListException if the addition of one or several components
     *         failed. This exception lists the components that were not added and the
     *         exception that occurred.
     */
    void removeFcSubComponent(List<Component> subComponents) throws ContentControllerExceptionListException;
}
