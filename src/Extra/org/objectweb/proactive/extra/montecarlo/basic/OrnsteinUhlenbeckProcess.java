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
import org.objectweb.proactive.annotation.PublicAPI;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.RandomStream;


/**
 * OrnsteinUhlenbeckProcess : Generate scenarios that follow the Ornstein-Uhlenbeck process.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class OrnsteinUhlenbeckProcess implements ExperienceSet<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 40L;
    private int N;
    private double base;
    private double factor;

    /**
     * 
     * @param s0 The present value of the asset
     * @param mu Mean reversion level
     * @param lambda Mean reversion rate
     * @param sigma volatility
     * @param t time
     * @param n number of experiences
     */
    public OrnsteinUhlenbeckProcess(double s0, double mu, double lambda, double sigma, int t, int n) {
        N = n;
        base = s0 * Math.exp(-lambda * t) + mu * (1 - Math.exp(-lambda * t));
        factor = sigma * Math.sqrt((1 - Math.exp(-2 * lambda * t)) / (2 * lambda));
    }

    public double[] simulate(RandomStream rng) {
        double[] answer = new double[N];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        for (int i = 0; i < N; i++) {
            answer[i] = base + factor * ngen.nextDouble();
        }

        return answer;
    }
}
