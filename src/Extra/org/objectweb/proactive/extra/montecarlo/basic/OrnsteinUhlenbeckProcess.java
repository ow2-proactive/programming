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
package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extra.montecarlo.SimulationSet;

import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.RandomStream;


/**
 * OrnsteinUhlenbeckProcess : Generate scenarios that follow the Ornstein-Uhlenbeck process.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class OrnsteinUhlenbeckProcess implements SimulationSet<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;
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
