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
package org.objectweb.proactive.extra.montecarlo.basic;

import org.objectweb.proactive.extra.montecarlo.SimulationSet;
import org.objectweb.proactive.annotation.PublicAPI;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.RandomStream;


/**
 * GeometricBorwnianMotionStockDividend : Simulating geometric Brownian motion with a stock dividend.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class GeometricBrownianMotionStockDividend implements SimulationSet<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    private double s0, Y, r, sigma, D;
    private int T, N;
    private NormalDist normal = null;

    /**
     * Simulating geometric Brownian motion with a stock dividend
     *
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param Y Yield of the underlying, for stocks Y=r (interest rate), futures Y=0, currencies Y=(domestic interest rate-foreign interest rate)
     * @param r Continuous compounded interest rate
     * @param sigma Volatility
     * @param D Stock dividend percentage
     * @param T final time
     * @param N number of experiences
     */
    public GeometricBrownianMotionStockDividend(double s0, double Y, double r, double sigma, double D, int T,
            int N) {
        this.s0 = s0;
        this.Y = Y;
        this.r = r;
        this.sigma = sigma;
        this.D = D;
        this.T = T;
        this.N = N;
    }

    public double[] simulate(RandomStream rng) {
        double[] answer = new double[N];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        for (int i = 0; i < N; i++) {
            answer[i] = (s0 / (1 + D)) *
                Math.exp((Y - 0.5 * sigma * sigma) * T + sigma * Math.sqrt(T) * ngen.nextDouble());
        }
        return answer;
    }
}