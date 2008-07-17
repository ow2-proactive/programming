/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.SimulationSet;
import org.objectweb.proactive.extra.montecarlo.Simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;


/**
 * SimulatorImpl
 *
 * @author The ProActive Team
 */
public class SimulatorImpl implements Simulator {

    private SubMaster<SimulationSetTask, Serializable> master;
    private SubMasterLock lock;

    public SimulatorImpl(SubMaster master, SubMasterLock lock) {
        this.master = master;
        this.lock = lock;
    }

    public <R extends Serializable> Enumeration<R> solve(List<SimulationSet<R>> simulationSets)
            throws TaskException {
        lock.useSimulator();
        ArrayList<SimulationSetTask> adapterTasks = new ArrayList<SimulationSetTask>(simulationSets.size());
        for (SimulationSet eset : simulationSets) {
            adapterTasks.add(new SimulationSetTask(eset));
        }
        master.setResultReceptionOrder(SubMaster.COMPLETION_ORDER);
        master.solve(adapterTasks);
        return new OutputEnumeration<R>(lock, simulationSets.size());
    }

    public class OutputEnumeration<R extends Serializable> implements Enumeration<R> {

        private LinkedList<R> buffer = new LinkedList<R>();
        private SubMasterLock lock;
        private int pendingTasks;

        public OutputEnumeration(SubMasterLock lock, int pendingTasks) {
            this.lock = lock;
            this.pendingTasks = pendingTasks;
        }

        public boolean hasMoreElements() {
            return !buffer.isEmpty() || (pendingTasks > 0);
        }

        public R nextElement() {
            if (buffer.isEmpty()) {
                if (pendingTasks > 0) {
                    try {
                        List<R> res = (List<R>) master.waitSomeResults();
                        pendingTasks -= res.size();
                        buffer.addAll(res);
                    } catch (TaskException e) {
                        throw new RuntimeException(e);
                    }

                } else {
                    throw new ArrayIndexOutOfBoundsException("No more elements");
                }
                if (pendingTasks == 0) {
                    lock.releaseSimulator();
                }

            }

            return buffer.poll();
        }
    }
}
