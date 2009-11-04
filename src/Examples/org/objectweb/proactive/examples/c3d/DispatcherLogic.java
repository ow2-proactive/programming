/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.c3d;

/**
 * This interface describes methods which are accessible by classes
 * internal to the Dispatcher functionality, for example DispatcherGUI.
 * These methods have to be exposed as public to be accessible, but not by
 * classes which use the Dispatcher for its rendering and chatting capabilities.
 * So this interface is created, to avoid errors in using the class C3DDispatcher
 * directly, which is way too permissive.
 */
public interface DispatcherLogic {

    /** Sends a [log] message to given user */
    public void userLog(int i_user, String s_message);

    /** Ask users & dispatcher to log s_message, except one  */
    public void allLogExcept(int i_user, String s_message);

    /** Ask all users & dispatcher to log s_message */
    public void allLog(String s_message);

    /** Shut down everything, send warning messages to users */
    public void exit();

    /** See how well the simulation improves with more renderers */
    public void doBenchmarks();

    /** Makes the engine participate in the computation of images */
    public void turnOnEngine(String engineName);

    /** Stops the engine from participating in the computation of images*/
    public void turnOffEngine(String engineName);
}
