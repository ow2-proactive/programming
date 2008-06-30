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
package org.objectweb.proactive.extra.montecarlo;

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
public interface EngineTask<T extends Serializable> extends Serializable {

    public T run(Simulator simulator, Executor executor);

}
