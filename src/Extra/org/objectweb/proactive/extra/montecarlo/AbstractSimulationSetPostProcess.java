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
package org.objectweb.proactive.extra.montecarlo;

import umontreal.iro.lecuyer.rng.RandomStream;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * AbstractSimulationSetPostProcess
 *
 * A user wanting to define a SimulationSetPostProcess should inherit from this abstract class.
 *
 * @author The ProActive Team
 */
@PublicAPI
public abstract class AbstractSimulationSetPostProcess<T extends Serializable, R extends Serializable>
        implements SimulationSetPostProcess<T, R>, SimulationSet<R> {

    private SimulationSet<T> simulationSet;

    public AbstractSimulationSetPostProcess(SimulationSet<T> simulationSet) {
        this.simulationSet = simulationSet;
    }

    public R simulate(final RandomStream rng) {
        T results = simulationSet.simulate(rng);
        return postprocess(results);
    }

}
