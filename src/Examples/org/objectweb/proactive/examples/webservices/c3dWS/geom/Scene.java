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
package org.objectweb.proactive.examples.webservices.c3dWS.geom;

import java.util.Vector;

import org.objectweb.proactive.examples.webservices.c3dWS.prim.Light;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Primitive;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.View;


/**
 * Represents a 3D scene, with objects, lights and a view
 */
public class Scene implements java.io.Serializable {

    private static final long serialVersionUID = 52;
    private Vector lights = new Vector();
    private Vector objects = new Vector();
    private View view = new View();

    public Scene() {
    }

    public void addLight(Light l) {
        lights.addElement(l);
    }

    public Light getLight(int number) {
        return (Light) lights.elementAt(number);
    }

    public int getNbLights() {
        return lights.size();
    }

    /** Add an object to the scene */
    public void addPrimitive(Primitive object) {
        objects.addElement(object);
    }

    /** Swap an object of the scene for another one */
    public void setPrimitive(Primitive object, int pos) {
        objects.setElementAt(object, pos);
    }

    public Primitive getPrimitive(int number) {
        return (Primitive) objects.elementAt(number);
    }

    public int getNbPrimitives() {
        return objects.size();
    }

    public void setView(View view) {
        this.view = view;
    }

    public View getView() {
        return view;
    }
}
