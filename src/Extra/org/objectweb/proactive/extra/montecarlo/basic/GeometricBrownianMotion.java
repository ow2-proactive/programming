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
 * GeometricBrownianMotion
 *
 * @author The ProActive Team
 */
public class GeometricBrownianMotion implements ExperienceSet {

    private double s0, mu, sigma, t;
    private int N;

    /**
     * Simulating geometric Brownian motion. This equation is the exact solution of the geometrix brownian motion SDE.
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param mu Drift term
     * @param sigma Volatility
     * @param t time
     * @param N number of experiences
     */
    public GeometricBrownianMotion(double s0, double mu, double sigma, double t, int N) {
        this.s0 = s0;
        this.mu = mu;
        this.sigma = sigma;
        this.t = t;
        this.N = N;
    }

    public double[] simulate(Random rng) {
        double[] answer = new double[N];
        for (int i = 0; i < N; i++) {
            answer[i] = s0 *
                Math.exp((mu - 0.5 * sigma * sigma) * t + sigma * Math.sqrt(t) * rng.nextGaussian());
        }
        return answer;
    }
}
