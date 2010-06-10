/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.examples.webservices.c3dWS.prim;

import org.objectweb.proactive.examples.webservices.c3dWS.geom.Ray;
import org.objectweb.proactive.examples.webservices.c3dWS.geom.Vec;


/**
 * 3D representation of a Sphere, in space.
 */
public class Sphere extends Primitive implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 430L;
    private Vec c;
    private double r;
    private double r2;
    private Vec tmp; // temporary vecs used to minimize the memory load
    private static double mindiff = 1e-6;

    public Sphere() {
    }

    public Sphere(Vec center, double radius) {
        c = center;
        r = radius;
        r2 = r * r;
        tmp = new Vec();
    }

    /**
     * Modified intersection method - creates _many_ less Vecs
     * @author The ProActive Team
     * @author The ProActive Team
     */
    @Override
    public Isect intersect(Ray ray) {
        Isect ip;
        tmp.sub2(c, ray.P);
        double dot = Vec.dot(tmp, ray.D);
        double disc = (dot * dot) - Vec.dot(tmp, tmp) + r2;
        if (disc < 0.0) {
            return null;
        }
        disc = Math.sqrt(disc);
        double t = ((dot - disc) < mindiff) ? (dot + disc) : (dot - disc);
        if (t < mindiff) {
            return null;
        }
        ip = new Isect();
        ip.t = t;
        ip.enter = (Vec.dot(tmp, tmp) > (r2 + mindiff));
        ip.prim = this;
        return ip;
    }

    /**
     * Normal (outwards) vector at point P of the sphere.
     */
    @Override
    public Vec normal(Vec p) {
        Vec normal = Vec.sub(p, c);
        normal.normalize();
        return normal;
    }

    @Override
    public String toString() {
        return "Sphere {" + c.toString() + ", radius " + r + "}";
    }

    public Vec getCenter() {
        return c;
    }

    public double getRadius() {
        return r;
    }

    public void setCenter(Vec c) {
        this.c = c;
    }

    /**
     * Rotates the Sphere.
     * @see org.objectweb.proactive.examples.c3d.prim.Primitive#rotate(org.objectweb.proactive.examples.c3d.geom.Vec)
     */
    @Override
    public void rotate(Vec vec) {
        double phi;
        double l;

        // the X axis rotation
        if (vec.getX() != 0) {
            phi = Math.atan2(c.getZ(), c.getY());
            l = Math.sqrt((c.getY() * c.getY()) + (c.getZ() * c.getZ()));
            c.setY(l * Math.cos(phi + vec.getX()));
            c.setZ(l * Math.sin(phi + vec.getX()));
        }

        // the Y axis rotation
        if (vec.getY() != 0) {
            phi = Math.atan2(c.getZ(), c.getX());
            l = Math.sqrt((c.getX() * c.getX()) + (c.getZ() * c.getZ()));
            c.setX(l * Math.cos(phi + vec.getY()));
            c.setZ(l * Math.sin(phi + vec.getY()));
        }

        // the Z axis rotation
        if (vec.getZ() != 0) {
            phi = Math.atan2(c.getX(), c.getY());
            l = Math.sqrt((c.getY() * c.getY()) + (c.getX() * c.getX()));
            c.setY(l * Math.cos(phi + vec.getZ()));
            c.setX(l * Math.sin(phi + vec.getZ()));
        }
    }
}
