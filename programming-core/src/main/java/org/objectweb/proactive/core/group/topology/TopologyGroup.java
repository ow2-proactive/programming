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
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * Topologies are groups. They just give special access to their members or (sub)groups members.
 *
 * @author The ProActive Team
 */

public abstract class TopologyGroup<E> extends ProxyForGroup<E> {

    /**
     * Constructor : a Topology is build with a group with the specified size
     * @param g - the group used a base for the new group (topology)
     * @param size - the number of member of g used to build the topology
     * @throws ConstructionOfReifiedObjectFailedException
     */
    public TopologyGroup(Group<E> g, int size) throws ConstructionOfReifiedObjectFailedException {
        super(g.getTypeName());
        for (int i = 0; i < size; i++) {
            this.add(g.get(i));
        }
    }
}
