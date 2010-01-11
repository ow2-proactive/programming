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
package org.objectweb.proactive.examples.webservices.c3dWS;

import org.objectweb.proactive.core.util.wrapper.StringMutableWrapper;
import org.objectweb.proactive.examples.webservices.c3dWS.geom.Vec;
import org.objectweb.proactive.examples.webservices.c3dWS.prim.Sphere;


/** Services proposed by a Dispatcher Active Object, without all the GUI stuff */
public interface Dispatcher {

    /** Rotate every object by the given angle */
    public void rotateScene(int i_user, Vec angles);

    public void addSphere(Sphere s);

    public void resetScene();

    /** Register a user, so he can join the fun */
    //SYNCHRONOUS CALL. All [active object calls back to caller] in this method happen AFTER the int is returned
    public int registerUser(User c3duser, String userName);

    public void registerMigratedUser(int userNumber);

    /** removes user from userList, so he cannot receive any more messages or images */
    public void unregisterConsumer(int number);

    /** Get the list of users, entries being separated by \n */
    public StringMutableWrapper getUserList();

    /** Find the name of the machine this Dispatcher is running on */
    public String getMachineName();

    /** Find the name of the OS the Dispatcher is running on */
    public String getOSString();

    /** send message to all users except one */
    public void userWriteMessageExcept(int i_user, String s_message);

    /** Shows a message to a user*/
    public void userWriteMessage(int i_user, String s_message);
}
