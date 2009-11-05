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

/**
 * Representation of the intersection between a Ray and a Primitive.
 * Would be set to null if Ray does not hit Primitive.
 */
public class Isect implements java.io.Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;

    /**
     * Remember, the ray has two vecs that define it : P and D.
     * This t is the value so that P + tD = point of collision which Primitive
     */
    public double t;

    /**
     * The Primitive which was checked for intersection
     */
    public Primitive prim;

    /**
     * Is this a ray that comes frmo the inside of the Primitive, or from the outside?
     * enter = true means from outside -->  inside
     * HERM, sort of... In fact, I'm not sure what this is...
     */
    public boolean enter;
}
