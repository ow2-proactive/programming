/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.examples.c3d.geom;

/**
 * A class for making rays (lines in 3D), which have a start point, and a direction.
 */
final public class Ray implements java.io.Serializable {
    public Vec P;
    public Vec D;

    public Ray(Vec pnt, Vec dir) {
        P = new Vec(pnt.x, pnt.y, pnt.z);
        D = new Vec(dir.x, dir.y, dir.z);
        D.normalize();
    }

    /** This is very dangerous to use, as a 0,0 line is not a line ! */
    public Ray() {
        P = new Vec();
        D = new Vec();
    }

    /**
     * Works out the point which lies on this line, at distance t from origine.
     * @returns V = P + D * t
     */
    public Vec point(double t) {
        return new Vec(P.x + (D.x * t), P.y + (D.y * t), P.z + (D.z * t));
    }

    @Override
    public String toString() {
        return "{ Po = " + P.toString() + " dir= " + D.toString() + "}";
    }
}
