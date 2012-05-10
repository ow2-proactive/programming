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
package org.objectweb.proactive.examples.webservices.c3dWS.prim;

import org.objectweb.proactive.examples.webservices.c3dWS.geom.Ray;
import org.objectweb.proactive.examples.webservices.c3dWS.geom.Vec;


/**
 * All palpable objects in space should implement this class.
 * As it is now, it only is a container for the surface.
 */
public abstract class Primitive implements java.io.Serializable {
    private Surface surf;

    /**
     * The normal Vector to the Primitve, considered at the given point.
     * The result must be normalized!
     * @param pnt the coordinate on the primitive of which to give a normal.
     * @return the normal vector, ie the one orthogonal to the Primitive
     */
    public abstract Vec normal(Vec pnt);

    /**
     * Given a Ray, find the intersection between the Primitive and the Ray.
     * @param ray the Ray which should intersect the Primitive
     * @return null if no intersection
     */
    public abstract Isect intersect(Ray ray);

    /**
     * Rotate the object along the given three angles.
     * Watch out, performs rotateX, rotateY, then rotateZ [not commutative]
     * @param vec the three angles of rotation, in radians
     */
    public abstract void rotate(Vec vec);

    /**
     * @param surf The surface to apply to this primitive.
     */
    public void setSurface(Surface surf) {
        this.surf = surf;
    }

    /**
     * @return Returns the surface which is mapped onto this Primitive.
     */
    public Surface getSurface() {
        return surf;
    }
}
