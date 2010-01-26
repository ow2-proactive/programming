/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
 * GeometricBrownianMotion : Simulating geometric Brownian motion.<br/>
 * This equation is the exact solution of the geometrix brownian motion SDE.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class GeometricBrownianMotion implements SimulationSet<double[]> {

    private double s0, mu, sigma, t;
    private int N;

    //@snippet-start montecarlo_geometricbrownianmotion
    /**
     * Simulating geometric Brownian motion. This equation is the exact solution of the geometrix brownian motion SDE.
     * @param s0 Initial value at t=0 of geometric Brownian
     * @param mu Drift term
     * @param sigma Volatility
     * @param t time
     * @param N number of experiences
     */
    public GeometricBrownianMotion(double s0, double mu, double sigma, double t, int N)
    //@snippet-end montecarlo_geometricbrownianmotion            
    {
        this.s0 = s0;
        this.mu = mu;
        this.sigma = sigma;
        this.t = t;
        this.N = N;

    }

    public double[] simulate(RandomStream rng) {
        double[] answer = new double[N];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        for (int i = 0; i < N; i++) {
            answer[i] = s0 *
                Math.exp((mu - 0.5 * sigma * sigma) * t + sigma * Math.sqrt(t) * ngen.nextDouble());
        }
        return answer;
    }
}
