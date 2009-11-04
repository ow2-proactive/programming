/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
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
 * An infinite Plane, represented by the cartesian equation  ax+by+cz =d .
 * The base vector represents the normal vector, and d the distance to the origin.
 */
public class Plane extends Primitive {
    private Vec base;
    private float d;
    private static double mindiff = 1e-6;

    public Plane(Vec abc, float d) {
        this.base = abc;
        this.d = d;
    }

    /**
     * The normal vector at the point pnt on the plane.
     *@see org.objectweb.proactive.examples.c3d.prim.Primitive#normal(org.objectweb.proactive.examples.c3d.geom.Vec)
     */
    @Override
    public Vec normal(Vec pnt) {
        Vec normal = new Vec(base.getX(), base.getY(), base.getZ());
        normal.normalize();
        return normal;
    }

    /**
     * @see org.objectweb.proactive.examples.c3d.prim.Primitive#intersect(org.objectweb.proactive.examples.c3d.geom.Ray)
     */
    @Override
    public Isect intersect(Ray ray) {
        double div = (ray.D.getX() * base.getX()) + (ray.D.getY() * base.getY()) +
            (ray.D.getZ() * base.getZ());
        if (div == 0) {
            return null;
        }
        double t = (d - (base.getX() * ray.P.getX()) - (base.getY() * ray.P.getY()) - (base.getZ() * ray.P
                .getZ())) /
            div;
        if (t > mindiff) {
            Isect ip = new Isect();
            ip.t = t;
            ip.enter = true; // I don't know what value to give to 'enter'.
            ip.prim = this;
            return ip;
        }
        return null;
    }

    @Override
    public String toString() {
        return base + " d=" + d;
    }

    /**
     * Rotate the Plane.
     * @see org.objectweb.proactive.examples.c3d.prim.Primitive#rotate(org.objectweb.proactive.examples.c3d.geom.Vec)
     */
    @Override
    public void rotate(Vec vec) {
        double phi;
        double l;

        // the X axis rotation
        if (vec.getX() != 0) {
            phi = Math.atan2(base.getZ(), base.getY());
            l = Math.sqrt((base.getY() * base.getY()) + (base.getZ() * base.getZ()));
            base.setY(l * Math.cos(phi + vec.getX()));
            base.setZ(l * Math.sin(phi + vec.getX()));
        }

        // the Y axis rotation
        if (vec.getY() != 0) {
            phi = Math.atan2(base.getZ(), base.getX());
            l = Math.sqrt((base.getX() * base.getX()) + (base.getZ() * base.getZ()));
            base.setX(l * Math.cos(phi + vec.getY()));
            base.setZ(l * Math.sin(phi + vec.getY()));
        }

        // the Z axis rotation
        if (vec.getZ() != 0) {
            phi = Math.atan2(base.getX(), base.getY());
            l = Math.sqrt((base.getY() * base.getY()) + (base.getX() * base.getX()));
            base.setY(l * Math.cos(phi + vec.getZ()));
            base.setX(l * Math.sin(phi + vec.getZ()));
        }
    }
}
