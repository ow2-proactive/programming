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
 * GeometricBorwnianMotionCashDividend : Simulating geometric Brownian motion with a cash dividend before T
 *
 * @author The ProActive Team
 */
@PublicAPI
public class GeometricBrownianMotionMultipleCashDividends implements SimulationSet<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;
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