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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
//@snippet-start synch_cma_full
//@snippet-start synch_cma_skeleton
package org.objectweb.proactive.examples.userguide.cmagent.synch;

import java.io.Serializable;
import java.util.Vector;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.examples.userguide.cmagent.initialized.CMAgentInitialized;
import org.objectweb.proactive.examples.userguide.cmagent.simple.State;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class CMAgentChained extends CMAgentInitialized implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    private CMAgentChained previousNeighbour;
    private CMAgentChained nextNeighbour;

    //@snippet-start synch_getstub_usage 
    public void setPreviousNeighbour(CMAgentChained neighbour) {
        this.previousNeighbour = neighbour;
        //TODO 1. Pass a remote reference of this object to the neighbour
        // Hint: This object is "nextNeighbour" for previous neighbour if not null
        //@snippet-break synch_cma_skeleton
        //@tutorial-break
        if (neighbour.getNextNeigbour() == null)
            neighbour.setNextNeighbour((CMAgentChained) PAActiveObject.getStubOnThis());
        //@tutorial-resume
        //@snippet-resume synch_cma_skeleton
    }

    public void setNextNeighbour(CMAgentChained neighbour) {
        this.nextNeighbour = neighbour;
        //TODO 2. Pass a remote reference of this object to the neighbour
        // Hint: This object is "previousNeighbour" for next neighbour if not null
        //@snippet-break synch_cma_skeleton
        //@tutorial-break
        if (neighbour.getPreviousNeigbour() == null)
            neighbour.setPreviousNeighbour((CMAgentChained) PAActiveObject.getStubOnThis());
        //@tutorial-resume
        //@snippet-resume synch_cma_skeleton
    }

    //@snippet-end synch_getstub_usage 

    public CMAgentChained getPreviousNeigbour() {
        return previousNeighbour;
    }

    public CMAgentChained getNextNeigbour() {
        return nextNeighbour;
    }

    public Vector<State> getAllPreviousStates() {
        System.out.println(PAActiveObject.getStubOnThis());

        if (this.previousNeighbour != null) {
            System.out.println("Passing the call to the previous neighbour...");
            // wait-by-necessity
            Vector<State> states = this.previousNeighbour.getAllPreviousStates();
            // states is a future

            // TODO 3. Is this explicit synchronization mandatory ? (NO the wait was removed)
            //@snippet-break synch_cma_skeleton
            //@tutorial-break
            states.add(this.getCurrentState());
            //@tutorial-resume
            //@snippet-resume synch_cma_skeleton

            return states;
        } else {
            System.out.println("No more previous neighbours..");
            Vector<State> states = new Vector<State>();
            states.add(this.getCurrentState());
            return states;
        }
    }

    public Vector<State> getAllNextStates() {
        System.out.println(PAActiveObject.getStubOnThis());
        if (this.nextNeighbour != null) {
            // wait-by-necessity
            System.out.println("Passing the call to the next neighbour..");
            Vector<State> states = this.nextNeighbour.getAllNextStates();
            // states is a future

            // TODO 4. Is this explicit synchronization mandatory ?	(NO the wait was removed)
            //@snippet-break synch_cma_skeleton
            //@tutorial-break
            states.add(this.getCurrentState());
            //@tutorial-resume
            //@snippet-resume synch_cma_skeleton

            return states;
        } else {
            System.out.println("No more next neighbours");
            Vector<State> states = new Vector<State>();
            states.add(this.getCurrentState());
            return states;
        }
    }
}
//@snippet-end synch_cma_skeleton
//@snippet-end synch_cma_full
//@tutorial-end