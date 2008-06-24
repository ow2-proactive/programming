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

import java.util.ArrayList;
import java.util.Random;
import java.io.Serializable;


/**
 * ExperienceSet
 *
 * This interface defines a Monte-Carlo set of successive experiences, using the provided random generator
 *
 * @author The ProActive Team
 */
public interface ExperienceSet extends Serializable {

    /**
     * Defines a Monte-Carlo set of successive experiences, a Random generator is given and will be used
     *
     * A list of double values is expected as output, result of the successive experiences.
     * These experiences can be independant or correlated, this choice is left to the user inside the implementation of this method.
     *
     * @param rng random number generator
     * @return a list of double values
     */
    double[] simulate(final Random rng);
}
