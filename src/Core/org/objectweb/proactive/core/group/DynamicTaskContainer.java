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
 * Compared to {@link BufferedTaskContainer}, it not only notifies a monitoring object
 * about execution and completion of the task, it also dynamically allocates the task to
 * a worker.
 * 
 * 
 * 
 * @author The ProActive Team
 *
 */
public class DynamicTaskContainer implements Runnable {

    AbstractProcessForGroup task;
    DispatchMonitor dispatchMonitor;

    public DynamicTaskContainer(AbstractProcessForGroup task, DispatchMonitor dispatchMonitor) {
        this.task = task;
        this.dispatchMonitor = dispatchMonitor;
    }

    public void run() {
        Integer slot;
        // index assigned to task is reassigned for dynamic behavior
        slot = dispatchMonitor.getWorker();
        // System.out.println("got " + slot);
        task.setGroupIndex(slot);
        if (task instanceof ProcessForAsyncCall) {
            ((ProcessForAsyncCall) task).setDispatchMonitor(dispatchMonitor);
        }
        // System.out.println("retreived [" + job.getIndex() + "]");
        dispatchMonitor.dispatchedTask(task);
        task.run();
        // System.out.print("...[" + dispatcher.getAvailableSlots().size() + "]
        // " + DispatcherBasic.counter++ +"...");

    }
}
