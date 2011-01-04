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
package org.objectweb.proactive.extra.montecarlo;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.masterworker.TaskException;


/**
 * Executor
 *
 * This interface represents an access to the Monte-Carlo engine for running a list of engine task in parallel
 *  
 *
 * @author The ProActive Team
 */
@PublicAPI
//@snippet-start montecarlo_executor
public interface Executor {

    /**
     * Asks the engine to run a list of engine tasks in parallel.<br/>
     * As each engine task returns a Serializable object the general result of the parallel tasks<br/>
     * are a list of these objects, with coherent ordering.
     * @param engineTasks list of tasks to run in parallel
     * @return a list of objects as output
     * @throws TaskException is an exception occured during the execution of the user code 
     */
    public <T extends Serializable> Enumeration<T> solve(List<EngineTask<T>> engineTasks)
            throws TaskException;
}
//@snippet-end montecarlo_executor
