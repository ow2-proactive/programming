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
import umontreal.iro.lecuyer.probdist.NormalDist;
import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.rng.RandomStream;


/**
 * @author vddoan
 *
 */
public class BrownianBridge implements ExperienceSet<double[]> {

    /**
     * 
     */
    private static final long serialVersionUID = 40L;
    private double w0, wT, t, T;

    /**
     * @param w0 Known fixed at time 0 value of the Brownian motion
     * @param wT Know fixed at time T value of the Brownian motion
     * @param t  A given time
     * @param T Maturity date
     */
    public BrownianBridge(double w0, double wT, double t, double T) {
        super();
        this.w0 = w0;
        this.wT = wT;
        this.t = t;
        this.T = T;
    }

    /**
     * The Brownian Bridge is used to generate new samples between two known samples of a Brownian motion path.
     * i.e generate sample at time t using sample at time 0 and at T, with 0<t<T
     */
    public double[] simulate(RandomStream rng) {
        final double[] answer = new double[1];
        NormalGen ngen = new NormalGen(rng, new NormalDist());
        answer[0] = w0 + (t / T) * (wT - w0) + Math.sqrt(t * (T - t) / T) * ngen.nextDouble();
        return answer;
    }

}
