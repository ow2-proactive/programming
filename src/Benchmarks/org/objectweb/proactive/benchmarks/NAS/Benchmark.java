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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.benchmarks.NAS;

import java.io.File;

import org.objectweb.proactive.benchmarks.NAS.CG.KernelCG;
import org.objectweb.proactive.benchmarks.NAS.EP.KernelEP;
import org.objectweb.proactive.benchmarks.NAS.FT.KernelFT;
import org.objectweb.proactive.benchmarks.NAS.IS.KernelIS;
import org.objectweb.proactive.benchmarks.NAS.MG.KernelMG;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.timitspmd.TimIt;
import org.objectweb.proactive.extensions.timitspmd.util.Startable;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


public class Benchmark implements Startable {
    public static String DEFAULT_LOCAL_DESCRIPTOR = "descriptors/local-1.xml";

    private static Kernel kernel;
    private GCMApplication gcma;
    private BenchmarkArgs benchArgs;
    private NASProblemClass pcl;

    /**
     * Invoked only when running in a standalone mode.
     * 
     * @param args
     *              The command line arguments
     */
    public static void main(String[] args) {
        // This run is a standalone one (not launched by TimIt)
        TimIt.standaloneMode();

        Startable startableBenchmark = new Benchmark();
        startableBenchmark.start(args);
        startableBenchmark.kill();
        System.exit(0);
    }

    /**
     * To launch the selected by args kernel. Part of the Startable implementation.
     * 
     * @param args
     *            The command line arguments.
     */
    public void start(String[] args) {
        gcma = null;
        benchArgs = parseArgs(args);
        pcl = NASClassesFactory.getNASClass(benchArgs.getKernel(), benchArgs.getClss(), benchArgs.getNp());

        if (pcl == null) {
            System.err.println(benchArgs.getKernel() + " is not yet implemented or does not exist !");
            System.exit(1);
        }
        try {
            System.out.println("GCMApplication is : " + benchArgs.getDesc());
            gcma = PAGCMDeployment.loadApplicationDescriptor(new File(benchArgs.getDesc()));

            // Launch the adequate kernel
            this.launchKernel();

        } catch (ProActiveException e) {
            System.err.println("An error occur during the deployment of the application");
            e.printStackTrace();
            gcma.kill();
            System.exit(1);
        }
    }

    /**
     * Calls the current kernel killKernel method. Part of the Startable implementation.
     */
    public void kill() {
        if (Benchmark.kernel == null) {
            System.err.println("The kernel was not specified !  "
                + "Please check if you have called the start method before.");
            System.exit(1);
        }
        Benchmark.kernel.killKernel();
        gcma.kill();
    }

    /**
     * Called between each benchmark
     */
    public void masterKill() {
    }

    /**
     * Parses the arguments array.
     * 
     * @param args
     *            The string array of arguments.
     * @return The BenchmarkArgs instance that contains all parsed information.
     */
    public BenchmarkArgs parseArgs(String[] args) {
        BenchmarkArgs benchArgs = new BenchmarkArgs();
        for (int i = 0; i < args.length; i++) {
            if (args[i].compareToIgnoreCase("-np") == 0) {
                benchArgs.setNp(Integer.parseInt(args[++i]));
            } else if (args[i].compareToIgnoreCase("-class") == 0) {
                if (args[++i].length() == 1) {
                    benchArgs.setClss(args[i].toUpperCase().charAt(0));
                } else {
                    System.err
                            .println("Parse args error: " + args[i - 1] + " must have a one char parameter");
                    System.exit(1);
                }
            } else if (args[i].compareToIgnoreCase("-kernel") == 0) {
                benchArgs.setKernel(args[++i]);
            } else if (args[i].compareToIgnoreCase("-descriptor") == 0) {
                benchArgs.setDesc(args[++i]);
            } else if (args[i].compareToIgnoreCase("-help") == 0) {
                Benchmark.help();
                System.exit(0);
            }
        }

        if (benchArgs.getKernel() == null) {
            System.err.println("Parse args error: missing -kernel. -help for more information.");
            Benchmark.help();
            System.exit(1);
        }

        return benchArgs;
    }

    /**
     * Sets the selected kernel and launches it.
     */
    public void launchKernel() {
        if (new String(pcl.KERNEL_NAME).compareToIgnoreCase("IS") == 0) {
            kernel = new KernelIS(pcl, gcma);
        } else if (pcl.KERNEL_NAME.compareToIgnoreCase("CG") == 0) {
            kernel = new KernelCG(pcl, gcma);
        } else if (pcl.KERNEL_NAME.compareToIgnoreCase("FT") == 0) {
            kernel = new KernelFT(pcl, gcma);
        } else if (pcl.KERNEL_NAME.compareToIgnoreCase("EP") == 0) {
            kernel = new KernelEP(pcl, gcma);
        } else if (pcl.KERNEL_NAME.compareToIgnoreCase("MG") == 0) {
            kernel = new KernelMG(pcl, gcma);
        } else {
            System.err.println("Kernel not yet implemented.");
        }
        try {
            kernel.runKernel();
        } catch (Exception pe) {
            pe.printStackTrace();
            return;
        }
    }

    /**
     * Prints some help information.
     */
    private static void help() {
        System.err.println("usage: Benchmark -kernel [IS|...] [-np n|-class c|-descriptor file]");
        System.err.println("       Benchmark -help");
        System.err.println("  -kernel      the appropriate kernel (EP, MG, CG, FT, IS).");
        System.err.println("  -np          number of process, default is 1.");
        System.err.println("  -class       class of data (S, A, B, C, D, W), default is S the sample class.");
        System.err.println("  -descriptor  file descriptor of deployment. Default is " +
            Benchmark.DEFAULT_LOCAL_DESCRIPTOR + ".");
    }

    /**
     * This class contains the benchmark related information.
     */
    public static class BenchmarkArgs {
        private int np = 1; // default serial execution
        private char clss = 'S'; // default sample class
        private String kernel = null;
        private String desc = null;

        public char getClss() {
            return clss;
        }

        public void setClss(char clss) {
            this.clss = clss;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getKernel() {
            return kernel;
        }

        public void setKernel(String kernel) {
            this.kernel = kernel;
        }

        public int getNp() {
            return np;
        }

        public void setNp(int np) {
            this.np = np;
        }
    }
}
