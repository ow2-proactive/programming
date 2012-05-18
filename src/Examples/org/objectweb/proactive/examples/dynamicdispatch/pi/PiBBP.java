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
package org.objectweb.proactive.examples.dynamicdispatch.pi;

import java.io.File;
import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * 
 * This program evaluates the PI number using the Bailey-Borwein-Plouffe
 * algorithm. This is the main class, which we have to execute in order to run
 * the pi computation
 * 
 * @author The ProActive Team
 * 
 */
@ActiveObject
public class PiBBP implements Serializable {

    private static final long serialVersionUID = 52;
    protected int nbDecimals;
    private String deploymentDescriptorLocation_;
    private GCMApplication deploymentDescriptor_;
    private boolean ws_ = false;
    protected PiComputer piComputer;
    private int nbIntervals;

    /**
     * Empty constructor
     * 
     */
    public PiBBP() {
    }

    /**
     * Constructor
     * 
     * @param args
     *            the string array containing the arguments which will initalize
     *            the computation
     */
    public PiBBP(String[] args) {
        parseProgramArguments(args);
    }

    /**
     * Sets the number of decimals to compute
     * 
     * @param nbDecimals
     *            the number of decimals
     */
    public void setNbDecimals(int nbDecimals) {
        this.nbDecimals = nbDecimals;
    }

    public String launchComputation() {
        try {
            // *************************************************************
            // * creation of remote nodes
            // *************************************************************/
            System.out.println("\nStarting deployment of virtual nodes");
            // parse the descriptor file
            deploymentDescriptor_ = PAGCMDeployment.loadApplicationDescriptor(new File(
                deploymentDescriptorLocation_));
            deploymentDescriptor_.startDeployment();
            deploymentDescriptor_.waitReady();
            GCMVirtualNode computersVN = deploymentDescriptor_.getVirtualNode("workers");

            // // create the remote nodes for the virtual node computersVN
            // computersVN.activate();
            // *************************************************************
            // * creation of active objects on the remote nodes
            // *************************************************************/
            System.out.println("\nCreating a group of computers on the given virtual node ...");

            // create a group of computers on the virtual node computersVN
            piComputer = (PiComputer) PAGroup.newGroupInParallel(PiComputer.class.getName(),
                    new Object[] { Integer.valueOf(nbDecimals) }, (Node[]) computersVN.getCurrentNodes()
                            .toArray());

            return computeOnGroup(piComputer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            deploymentDescriptor_.kill();
        }
        return "";
    }

    /**
     * Method called when the value of pi has to be computed with a group of "pi
     * computers"
     * 
     * @param piComputers
     *            the group of "pi computers" that will perform the computation
     * @return the value of PI
     */
    public String computeOnGroup(PiComputer piComputers) {
        int nbNodes = PAGroup.getGroup(piComputers).size();
        System.out.println("\nUsing " + nbNodes + " PiComputers for the computation\n");

        // distribution of the intervals to the computers is handled in
        // a private method
        Interval intervals = null;
        try {
            intervals = PiUtil.dividePIByIntervalSize(nbIntervals, nbDecimals);
            System.out.println("we have: " + PAGroup.getGroup(intervals).size() + " intervals and " +
                nbNodes + " workers");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        // scatter group data, so that independent intervals are sent as
        // parameters to each PiComputer instance
        PAGroup.setScatterGroup(intervals);

        PAGroup.setDispatchMode(piComputers, DispatchMode.DYNAMIC, 1);

        // *************************************************************
        // * computation
        // *************************************************************/
        System.out.println("Starting computation ...\n");
        long timeAtBeginningOfComputation = System.currentTimeMillis();

        // invocation on group, parameters are scattered, result is a
        // group
        Result results = piComputers.compute(intervals);
        Group<Result> resultsGroup = PAGroup.getGroup(results);

        // the following is displayed because the "compute" operation is
        // asynchronous (non-blocking)
        System.out.println("Intervals sent to the computers...\n");

        Result total = PiUtil.conquerPI(results);

        long timeAtEndOfComputation = System.currentTimeMillis();

        // *************************************************************
        // * results
        // *************************************************************/
        System.out.println("\nComputation finished ...");
        System.out.println("Computed PI value is : " + total.getNumericalResult().toString());
        System.out.println("Time waiting for result : " +
            (timeAtEndOfComputation - timeAtBeginningOfComputation) + " ms");
        System.out.println("Cumulated time from all computers is : " + total.getComputationTime() + " ms");
        System.out
                .println("Ratio for " +
                    resultsGroup.size() +
                    " processors is : " +
                    (((double) total.getComputationTime() / ((double) (timeAtEndOfComputation - timeAtBeginningOfComputation))) * 100) +
                    " %");
        return total.getNumericalResult().toString();
    }

    /**
     * This method decides which version of pi application has to be launched
     * 
     */
    public void start() {
        System.out.println("Evaluation of Pi will be performed with " + nbDecimals + " decimals");

        launchComputation();

    }

    public static void main(String[] args) {
        try {
            // PiBBP piApplication = new PiBBP(args);
            // piApplication.start();
            PiBBP piApplication = PAActiveObject.newActive(PiBBP.class, new Object[] { args });

            piApplication.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the computation with the arguments found in the args array
     * 
     * @param args
     *            The initialization arguments
     */
    private void parseProgramArguments(String[] args) {
        nbDecimals = new Integer(args[0]).intValue();
        nbIntervals = new Integer(args[1]).intValue();
        deploymentDescriptorLocation_ = args[2];
        System.err.println("PiBBP : deploymentDescriptorLocation = " + deploymentDescriptorLocation_);
        System.exit(0);
    }
}
