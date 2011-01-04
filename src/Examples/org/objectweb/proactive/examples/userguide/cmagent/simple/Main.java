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
//@snippet-start CMA_Main
//@snippet-start simple_CMA_skeleton
//@tutorial-start
package org.objectweb.proactive.examples.userguide.cmagent.simple;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.node.NodeException;


public class Main {
    public static void main(String args[]) {
        try {
            String currentState = new String();
            //@snippet-start CMA_instantiation
            //TODO 1. Create the active object
            //@tutorial-break
            //@snippet-break simple_CMA_skeleton
            CMAgent ao = (CMAgent) PAActiveObject.newActive(CMAgent.class.getName(), null);
            //@tutorial-resume
            //@snippet-end CMA_instantiation
            //@snippet-start CMA_call
            //@snippet-resume simple_CMA_skeleton
            //TODO 2. Get the current state
            //@snippet-break simple_CMA_skeleton
            //@tutorial-break
            currentState = ao.getCurrentState().toString();
            //@tutorial-resume
            //@snippet-resume simple_CMA_skeleton
            //TODO 3. Print the state
            //@snippet-break simple_CMA_skeleton
            //@tutorial-break
            System.out.println(currentState);
            //@tutorial-resume
            //@snippet-end CMA_call
            //@snippet-start CMA_terminate_call
            //@snippet-resume simple_CMA_skeleton
            //TODO 4. Stop the active object and
            //        terminate the application
            //@snippet-break simple_CMA_skeleton
            //@tutorial-break
            PAActiveObject.terminateActiveObject(ao, true);
            PALifeCycle.exitSuccess();
            //@tutorial-resume
            //@snippet-resume simple_CMA_skeleton
            //@snippet-end CMA_terminate_call
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        } catch (ActiveObjectCreationException aoExcep) {
            System.err.println(aoExcep.getMessage());
        }
    }
}
//@tutorial-end
//@snippet-end CMA_Main
//@snippet-end simple_CMA_skeleton
