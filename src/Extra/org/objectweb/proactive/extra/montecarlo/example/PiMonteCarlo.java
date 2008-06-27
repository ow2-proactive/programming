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

// TODO: tests
package org.objectweb.proactive.extra.montecarlo.example;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.Random;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.PAMonteCarlo;
import org.objectweb.proactive.extra.montecarlo.Simulator;
import org.objectweb.proactive.extra.montecarlo.AbstractExperienceSetOutputFilter;
import org.objectweb.proactive.api.PALifeCycle;
import umontreal.iro.lecuyer.rng.RandomStream;


public class PiMonteCarlo implements EngineTask {

    private int niter = 0;
    private int tasks = 0;

    public PiMonteCarlo(int N, int M) {
        super();
        niter = N;
        tasks = M;
    }

    public class MCPi implements ExperienceSet {
        int N;

        MCPi(final int d) {
            this.N = d;
        }

        public double[] simulate(final RandomStream rng) {
            final double[] experiences = new double[N];
            for (int i = 0; i < N; i++) {
                double x = rng.nextDouble();
                double y = rng.nextDouble();
                experiences[i] = Math.hypot(x, y);
            }
            return experiences;
        }
    }

    public static void main(String[] args) throws ProActiveException, TaskException {
        URL descriptor = PiMonteCarlo.class.getResource("WorkersApplication.xml");
        PAMonteCarlo mc = new PAMonteCarlo(descriptor, null, "Workers");

        // total monte carlo iterations and number tasks

        PiMonteCarlo piMonteCarlo = new PiMonteCarlo(10000, 1000);

        double pi = (Double) mc.run(piMonteCarlo);

        System.out.println(" The value of pi is " + pi);
        mc.terminate();
        PALifeCycle.exitSuccess();
    }

    public Serializable run(Simulator simulator, Executor executor) {

        double pival;
        List<ExperienceSet> sets = new ArrayList<ExperienceSet>();

        for (int i = 0; i < tasks; i++) {
            sets.add(new AbstractExperienceSetOutputFilter(new MCPi(niter)) {
                public Serializable filter(Serializable experiencesResults) {
                    long counter = 0;
                    double[] simulatedCounts = (double[]) experiencesResults;
                    for (double exp : simulatedCounts) {
                        if (exp < 1) {
                            counter++;
                        }
                    }
                    return counter;
                }
            });
        }

        Enumeration<Serializable> simulatedCountList = null;

        try {
            simulatedCountList = simulator.solve(sets);
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }

        long counter = 0;
        while (simulatedCountList.hasMoreElements()) {

            long simulatedCounts = (Long) simulatedCountList.nextElement();
            counter += simulatedCounts;
        }

        pival = (4 * counter) / ((double) (niter * tasks));

        return pival;
    }
}
