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
//@snippet-start class_Simulation
package org.objectweb.proactive.examples.documentation.classes;

import org.objectweb.proactive.Service;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PALifeCycle;


public class Simulation implements RunActive {
    private boolean stoppedSimulation = false;
    private boolean startedSimulation = false;
    private boolean suspendedSimulation = false;
    private boolean notStarted = true;

    public void startSimulation() {
        // Simulation starts
        System.out.println("Simulation started...");
        notStarted = false;
        startedSimulation = true;
    }

    public void restartSimulation() {
        // Simulation is restarted
        System.out.println("Simulation restarted...");
        startedSimulation = true;
        suspendedSimulation = false;
    }

    public void suspendSimulation() {
        // Simulation is suspended
        System.out.println("Simulation suspended...");
        suspendedSimulation = true;
        startedSimulation = false;
    }

    public void stopSimulation() {
        // Simulation is stopped
        System.out.println("Simulation stopped...");
        stoppedSimulation = true;
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            // If the simulation is not yet started wait until startSimulation
            // method
            if (notStarted)
                service.blockingServeOldest("startSimulation");
            // If the simulation is started serve request with FIFO
            if (startedSimulation)
                service.blockingServeOldest();
            // If simulation is suspended wait until restartSimulation method
            if (suspendedSimulation)
                service.blockingServeOldest("restartSimulation");
            // If simulation is stopped, exit
            if (stoppedSimulation) {
                body.terminate();
                PALifeCycle.exitSuccess();
            }
        }
    }
}
//@snippet-end class_Simulation

