/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.group;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import org.objectweb.proactive.core.mop.MethodCall;


/**
 * Specifies the partitioning of data into tasks that are mapped to available workers, 
 * according to a scheme defined either through annotation on the invoked method, or directly on the group proxy.
 * 
 * 
 * @author The ProActive Team
 *
 */
public interface TaskFactory {

    /**
     * 
     * @param methodCalls maps each method call generated to the index of the target member it should be assigned to
     * This is overridden when performing dynamic dispatch.
     * @param result
     * @param exceptionList
     * @param doneSignal
     * @param originalReifiedMethod TODO
     * @param groupProxy TODO
     * @return
     */
    public Queue<AbstractProcessForGroup> generateTasks(MethodCall originalMethodCall, List<MethodCall> methodCalls,
            Object result, ExceptionListException exceptionList, CountDownLatch doneSignal,
            ProxyForGroup<?> groupProxy);

    /**
     * 
     * @param mc
     * @return
     * @throws InvocationTargetException
     */
    public List<MethodCall> generateMethodCalls(MethodCall mc) throws InvocationTargetException;

    /**
     * Static mapping of a task to a worker
     * @param mc a method call
     * @param partitioningIndex index allocated in partitioning step,overridable in dispatch step
     * @param groupSize number of workers
     * @return
     */
    public int getTaskIndex(MethodCall mc, int partitioningIndex, int groupSize);
}
