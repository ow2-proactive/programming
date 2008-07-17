/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.benchmarks.NAS.EP;

/**
 * Kernel EP
 * 
 * An "Embarrassingly Parallel" kernel. It provides an estimate of the
 * upper achievable limits for floating point performance, i.e., the
 * performance without significant interprocessor communication.
 */
public interface EPClasses {

    /**
     * Benchmark name
     */
    public static String KERNEL_NAME = "EP";
    public String OPERATION_TYPE = "Random numbers generated";

    /**
     * Common definitions
     */
    /*************/

    /*  CLASS S  */

    /*************/
    public static final char S_CLASS_NAME = 'S';
    public static final int S_M = 24;

    /*************/

    /*  CLASS W  */

    /*************/
    public static final char W_CLASS_NAME = 'W';
    public static final int W_M = 25;

    /*************/

    /*  CLASS A  */

    /*************/
    public static final char A_CLASS_NAME = 'A';
    public static final int A_M = 28;

    /*************/

    /*  CLASS B  */

    /*************/
    public static final char B_CLASS_NAME = 'B';
    public static final int B_M = 30;

    /*************/

    /*  CLASS C  */

    /*************/
    public static final char C_CLASS_NAME = 'C';
    public static final int C_M = 32;

    /*************/

    /*  CLASS D  */

    /*************/
    public static final char D_CLASS_NAME = 'D';
    public static final int D_M = 36;
}
