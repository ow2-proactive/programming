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
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * This class represents a group by a cycling one-dimensional topology.
 *
 * @author The ProActive Team
 */
public class Ring<E> extends TopologyGroup<E> { // implements Topology1D {

    /** size of the one-dimensional topology group */
    protected int width;

    /**
     * Construtor. The members of <code>g</code> are used to fill the topology group.
     * @param g - the group used a base for the new group (topology)
     * @param size - the dimension (max number of member in the topolody group)
     * @throws ConstructionOfReifiedObjectFailedException
     */
    public Ring(Group<E> g, int size) throws ConstructionOfReifiedObjectFailedException {
        super(g, size);
        this.width = size;
    }

    /**
     * Return the max size of the Ring
     * @return the max size of the one-dimensional topology group (i.e. the Ring)
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the position of the specified object
     * @param o - the object
     * @return the position of the object in the Ring
     */
    public int getX(Object o) {
        return this.indexOf(o);
    }

    /**
     * Returns the object at the left of the specified object in the one-dimensional topology group
     * @param o - the specified object
     * @return the object at the left of <code>o<code>. If there is no object at the left of <code>o</code>, return <code>null</code>
     */
    public Object left(Object o) {
        int position = this.indexOf(o);
        if (position != 0) {
            return this.get(position - 1);
        } else {
            return this.get(this.getWidth() - 1);
        }
    }

    /**
     * Returns the object at the right of the specified object in the one-dimensional topology group
     * @param o - the specified object
     * @return the object at the right of <code>o<code>. If there is no object at the right of <code>o</code>, return <code>null</code>
     */
    public Object right(Object o) {
        int position = this.indexOf(o);
        if (position != (this.getWidth() - 1)) {
            return this.get(position + 1);
        } else {
            return this.get(0);
        }
    }
}
