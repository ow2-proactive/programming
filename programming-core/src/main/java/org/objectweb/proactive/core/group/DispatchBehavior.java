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

import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * Specifies the mapping between tasks and workers.
 * 
 * @author The ProActive Team
 *
 */
@PublicAPI
public interface DispatchBehavior {

    /**
     * Maps a list of method calls (corresponding to tasks to be executed on workers), to
     * indexes of the workers (no information is available from workers).
     * 
     * @param originalMethodCall the reified method call invoked on the group
     * @param generatedMethodCalls the reified method calls generated according to the partitioning scheme, and
     * that should be invoked on workers
     * @param nbWorkers the number of available workers
     * @return the mapping tasks --> worker index, for the given list of tasks
     */
    public List<Integer> getTaskIndexes(MethodCall originalMethodCall, final List<MethodCall> generatedMethodCalls,
            int nbWorkers);

}
