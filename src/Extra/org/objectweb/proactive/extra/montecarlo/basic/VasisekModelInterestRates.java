/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
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
 * VasisekModelInterestRates
 *
 * This equation is the exact solution of the one-factor Vasicek interet rate model. In this model interest rates are Normal distributed, and thus can become negative.
 *
 * @author The ProActive Team
 */

@PublicAPI
public class VasisekModelInterestRates implements SimulationSet<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    private double a, b, t;
    private int N;

    /**
     * This equation is the exact solution of the one-factor Vasicek interet rate model. In this model interest rates are Normal distributed, and thus can become negative.
     * @param r0 Initial value at t=0 of the interest rate
     * @param alpha mean reversion rate
     * @param beta mean reversion level
     * @param sigma Volatility
     * @param t maturity date
     * @param N number of experiences
     */
    public VasisekModelInterestRates(double r0, double alpha, double beta, double sigma, double t, int N) {
        this.a = r0 * Math.exp(-alpha * t) + (beta / alpha) * (1 - Math.exp(-alpha * t));
        this.b = sigma * Math.sqrt((1 - Math.exp(-2 * alpha * t)) / (2 * alpha));
        this.t = t;
        this.N = N;

    }

    public double[] simulate(RandomStream rng) {
        double[] answer = new double[N];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        for (int i = 0; i < N; i++) {
            answer[i] = a + b * ngen.nextDouble();
        }
        return answer;
    }
}
