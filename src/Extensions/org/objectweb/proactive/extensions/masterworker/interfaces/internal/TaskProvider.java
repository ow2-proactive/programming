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
package org.objectweb.proactive.extensions.masterworker.interfaces.internal;

import java.io.Serializable;
import java.util.List;
import java.util.Queue;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * A Task Provider provides tasks to be executed and excepts results of these tasks (i.e. the Master from the worker point of view)
 * @author The ProActive Team
 *
 * @param <R> the type of the result client object
 */
public interface TaskProvider<R extends Serializable> {
    /**
     * Returns an array of tasks which need to be executed
     * @param worker the worker object which asks the tasks (stub)
     * @param workerName the name of the worker which asks the tasks
     * @return a list of new tasks to compute
     */
    Queue<TaskIntern<R>> getTasks(Worker worker, String workerName, boolean reflooding);

    /**
     * Returns the result of a task to the provider and ask for new ones
     * @param result the result of the completed task
     * @param workerName the name of the worker sending the result
     * @param reflooding that means the worker's stack is empty and it asks for a set a tasks bigger than one
     * @return a list of new tasks to compute
     */
    Queue<TaskIntern<R>> sendResultAndGetTasks(ResultIntern<R> result, String workerName, boolean reflooding);

    /**
     * Returns the result of a task to the provider
     * @param result the result of the completed task
     * @param workerName the name of the worker sending the result
     * @return an aknowledgement (for synchronization)
     */
    BooleanWrapper sendResult(ResultIntern<R> result, String workerName);

    /**
     * Returns the results of several tasks at once to the provider
     * @param results the results of the completed tasks
     * @param workerName the name of the worker sending the result
     * @return an aknowledgement (for synchronization)
     */
    BooleanWrapper sendResults(List<ResultIntern<R>> results, String workerName);

    /**
     * Returns the results of several tasks at once to the provider
     * @param results the results of the completed tasks
     * @param workerName the name of the worker sending the result
     * @return an aknowledgement (for synchronization)
     */
    Queue<TaskIntern<R>> sendResultsAndGetTasks(List<ResultIntern<R>> results, String workerName,
            boolean reflooding);

    /**
     * Happens when a worker has forwarded a task to another worker (to handle DivisibleTask)
     * @param taskId id of the forwarded task
     * @param newWorkerName name of the worker which now handles it
     */
    BooleanWrapper forwardedTask(Long taskId, String oldWorkerName, String newWorkerName);

    /**
     * Callback function sent from the workers to the master to acknowledge
     * That their activity is cleared
     * @param worker worker sending the message
     */
    void isCleared(Worker worker);
}
