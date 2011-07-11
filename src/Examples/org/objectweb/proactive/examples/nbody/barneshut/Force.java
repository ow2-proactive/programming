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
package org.objectweb.proactive.examples.nbody.barneshut;

/**
 * Class implementing physical gravitation force between bodies.
 * In this Barnes-Hut package, building a force may yield an exception
 * if the info has a too big diameter.
 */
import java.io.Serializable;


public class Force implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;

    /** Vector of the force */
    public double x = 0;

    /** Vector of the force */
    public double y = 0;

    /** Vector of the force */
    public double z = 0;

    /** Gravitational constant */
    public final double G = 9.81;

    //    public Force() {
    //    }

    /**
     * Creation of a new Force
     * @param ux parameter x
     * @param uy parameter y
     * @param uz parameter z
     */
    public Force(double ux, double uy, double uz) {
        x = ux;
        y = uy;
        z = uz;
    }

    /**
     * Adds up the force of the parameter force to this.
     * @param f the force to be added to this
     */
    public void add(Force f) {
        x += f.x;
        y += f.y;
        z += f.z;
    }

    @Override
    public String toString() {
        return "<" + (int) x + " " + (int) y + " " + (int) z + ">";
    }
}
