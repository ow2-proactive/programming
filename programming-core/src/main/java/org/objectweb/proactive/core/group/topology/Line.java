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
package org.objectweb.proactive.core.group.topology;

import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ConstructionOfReifiedObjectFailedException;


/**
 * This class represents a group by a one-dimensional topology.
 *
 * @author The ProActive Team
 */
public class Line<E> extends TopologyGroup<E> {

    private static final long serialVersionUID = 62L; // implements Topology1D {

    /** size of the one-dimensional topology group */
    protected int width;

    /**
     * Construtor. The members of <code>g</code> are used to fill the topology group.
     * @param g - the group used a base for the new group (topology)
     * @param size - the dimension (max number of member in the topolody group)
     * @throws ConstructionOfReifiedObjectFailedException
     */
    public Line(Group<E> g, int size) throws ConstructionOfReifiedObjectFailedException {
        super(g, size);
        this.width = size;
    }

    /**
     * Return the max size of the line
     * @return the max size of the one-dimensional topology group (i.e. the line)
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Returns the position of the specified object
     * @param o - the object
     * @return the position of the object in the line
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
            return null;
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
            return null;
        }
    }
}
