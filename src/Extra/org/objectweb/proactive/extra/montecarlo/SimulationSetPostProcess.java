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
package org.objectweb.proactive.extra.montecarlo;

import org.objectweb.proactive.annotation.PublicAPI;

import java.io.Serializable;


/**
 * SimulationSetPostProcess
 * <p/>
 * This interface defines a post processing task executed on the result of a SimulationSet.
 * In many cases, raw results from a Monte-Carlo Simulation need to be post-processed.
 * And it's much more efficient to do this directly on the worker, where the simulation is done,
 * than on the master (the amount of data tranferred on the network is much smaller).
 *
 * @author The ProActive Team
 */
@PublicAPI
//@snippet-start montecarlo_simulationsetpostprocess
public interface SimulationSetPostProcess<T extends Serializable, R extends Serializable> {

    /**
     * Defines a post-processing of results received from a simulation set
     * @param experiencesResults results receive from a Simulation Set task
     * @return the result of the post processing
     */
    R postprocess(T experiencesResults);
}
//@snippet-end montecarlo_simulationsetpostprocess
