/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;


/**
 * ExecutorImpl
 *
 * @author The ProActive Team
 */
public class ExecutorImpl implements Executor {

    SubMaster<EngineTaskAdapter, Serializable> master;
    private SubMasterLock lock;

    public ExecutorImpl(SubMaster master, SubMasterLock lock) {
        this.master = master;
        this.lock = lock;
    }

    public Enumeration<Serializable> solve(List<EngineTask> engineTasks) throws TaskException {
        ArrayList<EngineTaskAdapter> adapterTasks = new ArrayList<EngineTaskAdapter>(engineTasks.size());
        for (EngineTask etask : engineTasks) {
            adapterTasks.add(new EngineTaskAdapter(etask));
        }
        lock.useExecutor();
        master.setResultReceptionOrder(SubMaster.SUBMISSION_ORDER);
        master.solve(adapterTasks);
        return new OutputEnumeration(lock, adapterTasks.size());
    }

    public class OutputEnumeration implements Enumeration<Serializable> {

        private LinkedList<Serializable> buffer = new LinkedList<Serializable>();
        private SubMasterLock lock;
        private int pendingTasks;

        public OutputEnumeration(SubMasterLock lock, int pendingTasks) {
            this.lock = lock;
            this.pendingTasks = pendingTasks;
        }

        public boolean hasMoreElements() {
            return buffer.size() > 0 || pendingTasks > 0;
        }

        public Serializable nextElement() {
            if (buffer.isEmpty()) {
                if (pendingTasks > 0) {
                    try {
                        List<Serializable> res = master.waitSomeResults();
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
