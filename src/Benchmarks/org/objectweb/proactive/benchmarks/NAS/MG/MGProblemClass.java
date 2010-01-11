/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.benchmarks.NAS.MG;

import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;


/**
 * Kernel MG
 *
 * A simplified multi-grid kernel. It requires highly structured long
 * distance communication and tests both short and long distance data
 * communication.
 * It approximates a solution to the discrete Poisson problem.
 */
public class MGProblemClass extends NASProblemClass {

    public int np;
    public int maxLevel;
    public int nm2;

    public int niter;
    public int nxSz, nySz, nzSz;
    public int[] nx; // 1
    public int[] ny; // 2
    public int[] nz; // 3
    public int lt, lm;
    public int dim, ndim1, ndim2, ndim3;
    public int nm, nv, nr;
}
