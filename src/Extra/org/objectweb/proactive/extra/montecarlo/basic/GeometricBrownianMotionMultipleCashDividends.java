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

import org.objectweb.proactive.extra.montecarlo.SimulationSet;
import org.objectweb.proactive.annotation.PublicAPI;
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.RandomStream;


/**
 * GeometricBorwnianMotionCashDividend : Simulating geometric Brownian motion with a cash dividend before T
 *
 * @author The ProActive Team
 */
@PublicAPI
public class GeometricBrownianMotionMultipleCashDividends implements SimulationSet<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 40L;
    private double Y, sigma;
    private double T;
    private int N;

    private double alpha;

    /**
     * Simulating geometric Brownian motion with a cash dividend before T
     *
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param Y Yield of the underlying, for stocks Y=r (interest rate), futures Y=0, currencies Y=(domestic interest rate-foreign interest rate)
     * @param r Continuous compounded interest rate
     * @param sigma Volatility
     * @param Dti Cash dividends amount at time ti
     * @param ti time of the cash dividend
     * @param T final time
     * @param N number of experiences
     */
    public GeometricBrownianMotionMultipleCashDividends(double s0, double Y, double r, double sigma,
            double[] Dti, double[] ti, double T, int N) {
        this.Y = Y;
        this.sigma = sigma;
        this.T = T;
        this.N = N;
        if (ti.length != Dti.length) {
            throw new IllegalArgumentException("ti and Dti array lengths differ");
        }
        double sum = 0;
        for (int i = 0; i < ti.length; i++) {
            sum += Dti[i] * Math.exp(-r * ti[i]);
        }
        alpha = s0 - sum;
    }

    public double[] simulate(RandomStream rng) {
        double[] answer = new double[N];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        for (int i = 0; i < N; i++) {
            answer[i] = alpha *
                Math.exp((Y - 0.5 * sigma * sigma) * T + sigma * Math.sqrt(T) * ngen.nextDouble());
        }
        return answer;
    }
}