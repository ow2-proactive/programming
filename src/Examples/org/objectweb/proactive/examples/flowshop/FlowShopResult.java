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
package org.objectweb.proactive.examples.flowshop;

import java.io.Serializable;


/**
 * This class contains a FlowShop result : the permutation and this associated
 * makespan.
 *
 * Other informations can be added for statistic, ...
 *
 * @author The ProActive Team
 *
 */
public class FlowShopResult implements Comparable<FlowShopResult>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 430L;

    /**
     * A job permutation
     */
    public int[] permutation;

    /**
     * The makespan of the permutation
     */
    public long makespan;

    /**
     * For information, the number of permutation tested in this task.
     */
    public long nbPermutationTested;
    public long time;
    public int[] makespanCut;

    public FlowShopResult() {
    }

    /**
     * Associate a permutation and this makespan.
     *
     * @param permutation
     * @param makespan
     */
    public FlowShopResult(int[] permutation, long makespan) {
        this(permutation, makespan, -1, -1);
    }

    public FlowShopResult(int[] permutation, long makespan, long nbPermutationTested, long time) {
        this.permutation = permutation;
        this.makespan = makespan;
        this.nbPermutationTested = nbPermutationTested;
        this.time = time;
    }

    public FlowShopResult(int[] permutation, long makespan, long nbPermutationTested, long time,
            int[] cutbacks) {
        this(permutation, makespan, nbPermutationTested, time);
        this.makespanCut = cutbacks;
    }

    /**
     * @return Returns the makespan.
     */
    public long getMakespan() {
        return makespan;
    }

    /**
     * @param makespan The makespan to set.
     */
    public void setMakespan(int makespan) {
        this.makespan = makespan;
    }

    /**
     * @return Returns the permutation.
     */
    public int[] getPermutation() {
        return permutation;
    }

    /**
     * @param permutation The permutation to set.
     */
    public void setPermutation(int[] permutation) {
        this.permutation = permutation;
    }

    /**
     * Compare two FlowShopResult on them makespan value.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(FlowShopResult fsr) {
        if (fsr == null) {
            return -1;
        }
        if (makespan > fsr.makespan) {
            return 1;
        } else if (makespan < fsr.makespan) {
            return -1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "FSR : permutation " + Permutation.string(permutation) + ", makespan " + makespan +
            " and time " + time;
    }

    /**
     * @return Returns the nbPermutationTested.
     */
    public long getNbPermutationTested() {
        return nbPermutationTested;
    }

    /**
     * @return Returns the time.
     */
    public long getTime() {
        return time;
    }

    public int[] getMakespanCut() {
        return makespanCut;
    }

    public void setMakespanCut(int[] makespanCut) {
        this.makespanCut = makespanCut;
    }
}
