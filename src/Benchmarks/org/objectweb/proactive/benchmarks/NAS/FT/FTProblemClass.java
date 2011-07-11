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
package org.objectweb.proactive.benchmarks.NAS.FT;

import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;


/**
 * Kernel FT
 * 
 * A 3-D partial differential equation solution using FFTs. This kernel performs the essence of many
 * "spectral" codes. It is a rigorous test of long-distance communication performance.
 */
public class FTProblemClass extends NASProblemClass {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    public int np1;
    public int np2;
    public int np;

    // layout
    public int layout_type;
    public int[][] dims; // 3*3

    public int niter;
    public int nx; // 1
    public int ny; // 2
    public int nz; // 3
    public int maxdim;
    public double[] vdata_real;
    public double[] vdata_imag;
    public double ntotal_f; // 1. * nx * ny * nz
    public int ntdivnp;
}
