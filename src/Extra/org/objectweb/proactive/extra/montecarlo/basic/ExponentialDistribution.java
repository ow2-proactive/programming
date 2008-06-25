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
package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.ExperienceSet;

import java.util.Random;


/**
 * ExponentialDistribution
 *
 * @author The ProActive Team
 */
public class ExponentialDistribution implements ExperienceSet {

    private double lambda;
    private int N;

    /**
     * Generates exponential distributed samples from a uniform distributed variable.
     *
     * @param lambda Exponential distribution parameter
     */
    public ExponentialDistribution(double lambda, int N) {
        this.lambda = lambda;
        this.N = N;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = -lambda * Math.log(rng.nextDouble());
        }

        return answer;
    }
}
