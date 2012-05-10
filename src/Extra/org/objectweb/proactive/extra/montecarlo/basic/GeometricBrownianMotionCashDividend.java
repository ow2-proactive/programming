/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * GeometricBrownianMotionCashDividend : Simulating geometric Brownian motion with a cash dividend before T
 *
 * @author The ProActive Team
 */
@PublicAPI
public class GeometricBrownianMotionCashDividend implements SimulationSet<double[]> {

    private double s0, Y, r, sigma, Dt, t, T;
    private int N;

    /**
     * Simulating geometric Brownian motion with a cash dividend before T
     *
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param Y Yield of the underlying, for stocks Y=r (interest rate), futures Y=0, currencies Y=(domestic interest rate-foreign interest rate)
     * @param r Continuous compounded interest rate
     * @param sigma Volatility
     * @param Dt Cash dividend amount at time t
     * @param t time of the cash dividend
     * @param T final time
     * @param N number of experiences
     */
    public GeometricBrownianMotionCashDividend(double s0, double Y, double r, double sigma, double Dt,
            double t, double T, int N) {
        this.s0 = s0;
        this.Y = Y;
        this.r = r;
        this.sigma = sigma;
        this.Dt = Dt;
        this.t = t;
        this.T = T;
        this.N = N;
    }

    public double[] simulate(RandomStream rng) {
        double[] answer = new double[N];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        for (int i = 0; i < N; i++) {
            answer[i] = (s0 - Dt * Math.exp(-r * t)) *
                Math.exp((Y - 0.5 * sigma * sigma) * T + sigma * Math.sqrt(T) * ngen.nextDouble());
        }
        return answer;
    }
}
