/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.annotation.PublicAPI;

import java.io.Serializable;


/**
 * EngineTask
 *
 * An engine task represents any general task, which is not Monte-Carlo specific.
 *
 * An engine task have access to two interfaces:
 * <ul>
 *   <li>
 *  The Simulator: provides the ability to submit to the engine a bunch of Monte-Carlo parallel simulations.
 *   </li>
 *   <li>
 *   The Executor: provides the ability to submit to the engine other engine tasks
 *   </li>
 * </ul>
 *
 * A specific case of engine task is the top-level task. This task is the very first one submitted to the engine and should contain the main code of the algorithm.
 *
 * @author The ProActive Team
 */
@PublicAPI
//@snippet-start montecarlo_enginetask
public interface EngineTask<T extends Serializable> extends Serializable {

    /**
     * Defines a general purpose task which can be run by the Monte-Carlo framework
     *
     * @param simulator gives the possibility to schedule children simulation sets
     * @param executor  gives the possibility to schedule children engine tasks
     * @return the result of this task
     */
    public T run(Simulator simulator, Executor executor);

}
//@snippet-end montecarlo_enginetask
