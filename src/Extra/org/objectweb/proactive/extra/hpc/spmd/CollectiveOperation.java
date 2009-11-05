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
package org.objectweb.proactive.extra.hpc.spmd;

import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.group.Group;


/**
 * This class provides a set of <i>collective operations</i> to use among the members of a SPMD
 * program. These operations were designed to take advantage of the underlying topology.<br/>
 * 
 * Here is an example of usage : <code><pre>
 * public class MySPMDClass {
 *   (...)
 *   public void foo() {
 *     CollectiveOperation co = new CollectiveOperation(PASPMD.getMySPMDGroup());
 *     double myValue = Math.random();
 *     double maxValue = co.max(myValue);
 *     // maxValue will contains the maximal value returned by Math.random() on
 *     // each member of the SPMD group
 *   }
 * }
 * </pre></code>
 */
public class CollectiveOperation {
    private Group<Object> group;
    private int myRank;

    //
    // --- CONSTRUCTORS --------------------------------------------------------
    // 
    /**
     * Construct a {@link CollectiveOperation} for a given {@link Group}
     */
    public CollectiveOperation(Group<Object> g) {
        this.group = g;
        this.myRank = PASPMD.getMyRank();
    }

    //
    // --- PUBLIC METHODS ------------------------------------------------------
    // 
    /**
     * Performs a collective operation to compute a sum.
     * 
     * @param val
     *            the value to sum with others
     * @return the sum of all specified values
     */
    public double sum(double val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] srcArray = new double[1];
        double[] dstArray = new double[1];

        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("sumD" + step, destRank, srcArray, 0, dstArray, 0, 1);
            srcArray[0] += dstArray[0];
        } while (step < nbSteps);
        return srcArray[0];
    }

    public int sum(int val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] srcArray = new int[1];
        int[] dstArray = new int[1];
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("sumI" + step, destRank, srcArray, 0, dstArray, 0, 1);
            srcArray[0] += dstArray[0];
        } while (step < nbSteps);
        return srcArray[0];
    }

    /**
     * Performs a collective operation to compute a sum on an array. Each element of<tt>valArray</tt>
     * will be replaced by the sum of the elements at the same position on other SPMD members. It is
     * equivalent to call <tt>sum(double)</tt> on each element of <tt>valArray</tt>.
     * 
     * @param valArray
     *            the array involved in the sum
     */
    public void sum(double[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] dstArray = new double[srcArray.length];
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("sumDArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                srcArray[i] += dstArray[i];
            }
        } while (step < nbSteps);
    }

    public void sum(int[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] dstArray = new int[srcArray.length];
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("sumIArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                srcArray[i] += dstArray[i];
            }
        } while (step < nbSteps);
    }

    /**
     * Performs a collective operation to find a minimum.
     * 
     * @param val
     *            the value to compare.
     * @return the minimum <tt>val</tt> between all SPMD members
     */
    public double min(double val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] srcArray = new double[1];
        double[] dstArray = new double[1];
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("minD" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] < srcArray[0]) { // Look for the min value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    public int min(int val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] srcArray = new int[1];
        int[] dstArray = new int[1];
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("minI" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] < srcArray[0]) { // Look for the min value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    /**
     * Performs a collective operation to get an array of minimums.
     * 
     * @param valArray
     *            the values to compare. It is equivalent to call <tt>min(double)</tt> on each
     *            element of <tt>srcArray</tt>.
     */
    public void min(double[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] dstArray = new double[srcArray.length];
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("minDArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] < srcArray[i]) { // Look for the min value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    public void min(int[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] dstArray = new int[srcArray.length];
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("minIArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] < srcArray[i]) { // Look for the min value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    /**
     * Performs a collective operation to find a maximum.
     * 
     * @param val
     *            the value to compare.
     * @return the maximum <tt>val</tt> between all SPMD members
     */
    public double max(double val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] srcArray = new double[1];
        double[] dstArray = new double[1];
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("maxD" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] > srcArray[0]) { // Look for the max value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    public int max(int val) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] srcArray = new int[1];
        int[] dstArray = new int[1];
        srcArray[0] = val;
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("maxI" + step, destRank, srcArray, 0, dstArray, 0, 1);
            if (dstArray[0] > srcArray[0]) { // Look for the max value...
                srcArray[0] = dstArray[0];
            }
        } while (step < nbSteps);
        return srcArray[0];
    }

    /**
     * Performs a collective operation to get an array of maximum.
     * 
     * @param valArray
     *            the values to compare. It is equivalent to call <tt>max(double)</tt> on each
     *            element of <tt>srcArray</tt>.
     */
    public void max(double[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        double[] dstArray = new double[srcArray.length];
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("maxDArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] > srcArray[i]) { // Look for the max value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    public void max(int[] srcArray) {
        int step = 0;
        int nbSteps = ilog2(group.size());
        int[] dstArray = new int[srcArray.length];
        do {
            int destRank = myRank ^ ipow2(step++);
            PASPMD.exchange("maxIArray" + step, destRank, srcArray, 0, dstArray, 0, srcArray.length);
            for (int i = 0; i < srcArray.length; i++) {
                if (dstArray[i] > srcArray[i]) { // Look for the max value
                    srcArray[i] = dstArray[i];
                }
            }
        } while (step < nbSteps);
    }

    //
    // --- PRIVATE METHODS -----------------------------------------------------
    //  
    private static int ilog2(int n) {
        int nn, lg;

        if (n == 1)
            return 0;

        lg = 1;
        nn = 2;
        while (nn < n) {
            nn *= 2;
            lg += 1;
        }
        return lg;
    }

    private static final int ipow2(int n) {
        return (1 << n);
    }
}
