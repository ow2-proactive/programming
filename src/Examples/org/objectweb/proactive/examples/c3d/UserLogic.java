/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.examples.c3d;

import org.objectweb.proactive.examples.c3d.geom.Vec;


/** These are the methods accessible by the User Gui classes, which somewhat control the User active
 * object. The implementation will often simply forward the call to the dispatcher */
public interface UserLogic {

    /** Exit the application */
    public void terminate();

    /** Displays the list of users connected to the dispatcher */
    public void getUserList();

    /** Ask the dispatcher to revert to original scene*/
    public void resetScene();

    /** Ask the dispatcher to add a sphere*/
    public void addSphere();

    /**  Send a mesage to a given other user, or to all */
    public void sendMessage(String message, String recipientName);

    /**
     * ask for the scene to be rotated by some angle
     * @param rotationAngle = <x y z> means rotate x radians along the x axis,
     *         then y radians along the y axis, and finally  z radians along the z axis
     */
    public void rotateScene(Vec rotationAngle);
}
