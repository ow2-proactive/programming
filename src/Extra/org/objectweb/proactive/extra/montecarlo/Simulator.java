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
 * Simulator
 *
 * This interface represents an access to the Monte-Carlo engine for solving a list of experience sets in parallel.
 *
 * @author The ProActive Team
 */
@PublicAPI
//@snippet-start montecarlo_simulator
public interface Simulator {

    /**
     * Asks the engine to solve a list of experience sets which will be run in parallel
     * @param simulationSets list of experience sets to solve
     * @return a list of double which is a concatenation of each list of double produced by each experience set. The order of the ouput list is guarantied to be coherent with the order of the experience list.
     * @throws TaskException if an exception occured inside the user code
     */
    public <T extends Serializable> Enumeration<T> solve(List<SimulationSet<T>> simulationSets)
            throws TaskException;
}
//@snippet-end montecarlo_simulator
