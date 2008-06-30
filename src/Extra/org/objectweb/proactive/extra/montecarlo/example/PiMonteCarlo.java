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

import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.AbstractExperienceSetPostProcess;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.PAMonteCarlo;
import org.objectweb.proactive.extra.montecarlo.Simulator;
import umontreal.iro.lecuyer.rng.RandomStream;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class PiMonteCarlo implements EngineTask<Double> {

    private int niter = 0;
    private int tasks = 0;

    public PiMonteCarlo(int N, int M) {
        super();
        niter = N;
        tasks = M;
    }

    /**
     * Definition of Monte-Carlo simulations to compute pi
     */
    public class MCPi implements ExperienceSet<double[]> {
        int N;

        MCPi(final int n) {
            this.N = n;
        }

        /**
         * Generates N points in [0,1]*[0,1] and returns the distances between these points and the origin
         * @param rng
         * @return distance of each sample to the origin
         */
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

        // Get the example descriptor
        URL descriptor = PiMonteCarlo.class.getResource("WorkersApplication.xml");

        // initialize the framework
        PAMonteCarlo<Double> mc = new PAMonteCarlo<Double>(descriptor, null, "Workers");

        // initialization of the top-level task
        PiMonteCarlo piMonteCarlo = new PiMonteCarlo(100000, 1000);

        // starts the top-level task
        double pi = mc.run(piMonteCarlo);

        // print out the result
        System.out.println(" The value of pi is " + pi);
        mc.terminate();
        PALifeCycle.exitSuccess();
    }

    /**
     * Top-level task submits Monte-Carlo PI experiences, and analyse the distances returned to compute PI
     * @param simulator
     * @param executor
     * @return
     */
    public Double run(Simulator simulator, Executor executor) {

        double pival;

        // Definition of Monte-Carlo experiences sets
        // The experience sets are defined by the class MCPi
        // A post-process method is added to test, for each distance computed by MCPi, wether the point is inside the unit circle or not
        // the method then returns the number of successful experiences.
        // It's a good practice to add post-process methods to analyse the results produced by an experience set.
        // The main reason is that experience sets will produce a large amount of data as output.
        // Without a post-process method, this large output would be transferred through the network and induce a big overhead.
        // The post-process method allow (depending on the problem of course) to directly compute some statistics and return a much smaller output through the network.
        List<ExperienceSet<Long>> sets = new ArrayList<ExperienceSet<Long>>();
        for (int i = 0; i < tasks; i++) {
            sets.add(new AbstractExperienceSetPostProcess<double[], Long>(new MCPi(niter)) {
                public Long postprocess(double[] experiencesResults) {
                    long counter = 0;
                    double[] simulatedCounts = experiencesResults;
                    for (double exp : simulatedCounts) {
                        if (exp < 1) {
                            counter++;
                        }
                    }
                    return counter;
                }
            });
        }

        Enumeration<Long> simulatedCountList = null;

        try {
            // Submitting these experience sets to the master
            simulatedCountList = simulator.solve(sets);
        } catch (TaskException e) {
            throw new RuntimeException(e);
        }

        long counter = 0;
        // iterates over the results, the number of successful experiences are accumulated
        while (simulatedCountList.hasMoreElements()) {
            long simulatedCounts = (Long) simulatedCountList.nextElement();
            counter += simulatedCounts;
        }

        // Final computation of pi
        pival = (4 * counter) / ((double) (niter * tasks));

        return pival;
    }
}
