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
package org.objectweb.proactive.extra.montecarlo.example;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extra.montecarlo.AbstractSimulationSetPostProcess;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;
import org.objectweb.proactive.extra.montecarlo.PAMonteCarlo;
import org.objectweb.proactive.extra.montecarlo.SimulationSet;
import org.objectweb.proactive.extra.montecarlo.Simulator;

import umontreal.iro.lecuyer.rng.RandomStream;


public class PiMonteCarlo implements EngineTask<Double> {

    public static final String DEFAULT_DESCRIPTOR = "WorkersApplication.xml";
    public static final String DEFAULT_WORKERS_NAME = "Workers";
    public static final int DEFAULT_NITER = 1000;
    public static final int DEFAULT_NB_TASKS = 10000;

    private static int niter = 0;
    private static int tasks = 0;
    private int ni = 0;
    private int t = 0;
    private static URL descriptor_url;
    private static String vn_name;
    private static String master_vn_name;

    public PiMonteCarlo(int N, int M) {
        super();
        ni = N;
        t = M;
    }

    /**
     * Definition of Monte-Carlo simulations to compute pi
     */
    public class MCPi implements SimulationSet<double[]> {
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

    /**
     * Init the example with command line arguments
     * @param args
     * @throws MalformedURLException
     */
    public static void init(String[] args) throws MalformedURLException {

        Options command_options = new Options();
        command_options.addOption("d", true, "descriptor in use");
        command_options.addOption("w", true, "workers virtual node name");
        command_options.addOption("m", true, "master virtual node name");
        command_options.addOption("i", true, "number of iterations");
        command_options.addOption("e", true, "number of Monte-Carlo experience on each iteration");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(command_options, args);
        } catch (ParseException e) {
            System.err.println("Parsing failed, reason, " + e.getMessage());
            System.exit(1);
        }

        // get descriptor option value
        String descPath = cmd.getOptionValue("d");

        if (descPath == null) {
            descriptor_url = PiMonteCarlo.class.getResource(DEFAULT_DESCRIPTOR);
            if (descriptor_url == null) {
                System.err.println("Couldn't find internal ressource: " + DEFAULT_DESCRIPTOR);
                System.exit(1);
            }
        } else {
            // check provided descriptor
            File descriptorFile = new File(descPath);
            if (!descriptorFile.exists()) {
                System.err.println("" + descriptorFile + " does not exist");
                System.exit(1);
            } else if (!descriptorFile.canRead()) {
                System.err.println("" + descriptorFile + " can't be read");
                System.exit(1);
            } else if (!descriptorFile.isFile()) {
                System.err.println("" + descriptorFile + " is not a regular file");
                System.exit(1);
            }
            descriptor_url = descriptorFile.toURI().toURL();
        }

        // get vn option value
        vn_name = cmd.getOptionValue("w");
        if (vn_name == null) {
            vn_name = DEFAULT_WORKERS_NAME;
        }

        master_vn_name = cmd.getOptionValue("m");

        String niter_string = cmd.getOptionValue("i");
        if (niter_string == null) {
            niter = DEFAULT_NITER;
        } else {
            niter = Integer.parseInt(niter_string);
        }
        String ntasks_string = cmd.getOptionValue("e");
        if (ntasks_string == null) {
            tasks = DEFAULT_NB_TASKS;
        } else {
            tasks = Integer.parseInt(ntasks_string);
        }

    }

    public static void main(String[] args) throws ProActiveException, TaskException, MalformedURLException {

        init(args);

        // initialize the framework
        PAMonteCarlo<Double> mc = new PAMonteCarlo<Double>(descriptor_url, master_vn_name, vn_name);

        // initialization of the top-level task
        PiMonteCarlo piMonteCarlo = new PiMonteCarlo(tasks, niter);

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
        List<SimulationSet<Long>> sets = new ArrayList<SimulationSet<Long>>();
        for (int i = 0; i < t; i++) {
            sets.add(new AbstractSimulationSetPostProcess<double[], Long>(new MCPi(ni)) {
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
        pival = (4 * counter) / ((double) (ni * t));

        return pival;
    }
}
