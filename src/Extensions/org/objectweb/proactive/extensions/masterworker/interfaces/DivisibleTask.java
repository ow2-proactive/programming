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
package org.objectweb.proactive.extensions.masterworker.interfaces;

import org.objectweb.proactive.annotation.PublicAPI;

import java.io.Serializable;


/**
 * A task which can be divided into subtasks by submitting new tasks to the master
 * and collecting results
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface DivisibleTask<R extends Serializable> extends Task<R> {

    /**
     * A task to be executed<br/>
     * @param memory access to the worker memory
     * @param master access to the master, to submit new task 
     * @return the result
     * @throws Exception any exception thrown by the task
     */
    R run(WorkerMemory memory, SubMaster master) throws Exception;
}
