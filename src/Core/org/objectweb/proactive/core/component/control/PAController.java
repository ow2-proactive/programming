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

import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.node.Node;


/**
 * This interface defines some basic services offered by a component controller in the ProActive implementation of the GCM model.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface PAController extends Interface {
    /**
     * This method is called after creation of all functional and non-functional(controller)
     * interfaces. Controllers requiring initialization *after* all interfaces are instantiated
     * can override this method.
     */
    public void initController();

    /**
     * If a controller holds references to active objects which are dependent on it, it needs to
     * trigger the migration of these active objects. This is done by overriding this method.
     *
     * @param node
     * @throws MigrationException
     */
    public void migrateDependentActiveObjectsTo(Node node) throws MigrationException;
}
