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
package org.objectweb.proactive.extra.montecarlo.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;


/**
 * ExecutorImpl
 *
 * @author The ProActive Team
 */
public class ExecutorImpl implements Executor {

    SubMaster<EngineTaskAdapter<Serializable>, Serializable> master;
    private SubMasterLock lock;

    public ExecutorImpl(SubMaster<EngineTaskAdapter<Serializable>, Serializable> master, SubMasterLock lock) {
        this.master = master;
        this.lock = lock;
    }

    public <R extends Serializable> Enumeration<R> solve(List<EngineTask<R>> engineTasks)
            throws TaskException {
        List<EngineTaskAdapter<Serializable>> adapterTasks = new ArrayList<EngineTaskAdapter<Serializable>>(
            engineTasks.size());
        for (EngineTask<R> etask : engineTasks) {
            adapterTasks.add(new EngineTaskAdapter<Serializable>(etask));
        }
        lock.useExecutor();
        master.setResultReceptionOrder(SubMaster.SUBMISSION_ORDER);
        master.solve(adapterTasks);
        return new OutputEnumeration<R>(lock, adapterTasks.size());
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
            return buffer.size() > 0 || pendingTasks > 0;
        }

        @SuppressWarnings("unchecked")
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
