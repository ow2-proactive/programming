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
package org.objectweb.proactive.core.group;

/**
 * A wrapper for a task to be dispatched (here, task = instance of AbstractProcessForGroup). 
 * 
 * It notifies a monitoring object so that runtime information can be collected
 * about the execution and completion of the task.
 * 
 * Allocation of task to worker is predefined in the task itself and is not changed.
 * 
 * 
 * @author The ProActive Team
 *
 */
public class BufferedTaskContainer implements Runnable {

    AbstractProcessForGroup task;
    DispatchMonitor dispatchMonitor;

    public BufferedTaskContainer(AbstractProcessForGroup task, DispatchMonitor dispatchMonitor) {
        // this.listOfTasks = listOfTasks;
        this.task = task;
        this.dispatchMonitor = dispatchMonitor;
        // System.out.println("[job buffered] " + task.getIndex());
    }

    public void run() {
        if (dispatchMonitor != null) {
            // in other words, dispatch is dynamic
            if (task instanceof ProcessForAsyncCall) {
                ((ProcessForAsyncCall) task).setDispatchMonitor(dispatchMonitor);
            }
            // System.out.println("[job buffered] to " + task.getIndex() + "");
            // dispatcher.jobAllocation(task.getIndex());
            dispatchMonitor.dispatchedTask(task);
        }
        task.run();

    }

}
