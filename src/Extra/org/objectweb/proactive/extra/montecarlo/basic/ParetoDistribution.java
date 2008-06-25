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
 * ParetoDistribution : This transform is used to generate Pareto distributed samples form uniform distributed samples.
 *
 * @author The ProActive Team
 */
public class ParetoDistribution implements ExperienceSet {

    private int N;
    private double alpha, beta;

    /**
     * This transform is used to generate Pareto distributed samples form uniform distributed samples.
     *
     * @param alpha Pareto distribution parameter
     * @param beta 	Pareto distribution parameter
     * @param n number of samples to generate

     */
    public ParetoDistribution(double beta, double alpha, int n) {
        this.beta = beta;
        N = n;
        this.alpha = alpha;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = beta / (-Math.pow(Math.log(rng.nextDouble()), 1 / alpha));
        }

        return answer;
    }
}
