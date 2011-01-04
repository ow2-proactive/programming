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
package org.objectweb.proactive.benchmarks.NAS;

import org.objectweb.proactive.benchmarks.NAS.CG.CGClasses;
import org.objectweb.proactive.benchmarks.NAS.CG.CGProblemClass;
import org.objectweb.proactive.benchmarks.NAS.EP.EPClasses;
import org.objectweb.proactive.benchmarks.NAS.EP.EPProblemClass;
import org.objectweb.proactive.benchmarks.NAS.FT.FTClasses;
import org.objectweb.proactive.benchmarks.NAS.FT.FTProblemClass;
import org.objectweb.proactive.benchmarks.NAS.IS.ISClasses;
import org.objectweb.proactive.benchmarks.NAS.IS.ISProblemClass;
import org.objectweb.proactive.benchmarks.NAS.MG.MGClasses;
import org.objectweb.proactive.benchmarks.NAS.MG.MGProblemClass;
import org.objectweb.proactive.benchmarks.NAS.util.NpbMath;


public class NASClassesFactory {
    public static final String MG_VERSION = "3.2";
    public static final String EP_VERSION = "3.2";
    public static final String FT_VERSION = "3.2";
    public static final String CG_VERSION = "3.2";
    public static final String IS_VERSION = "3.2";

    private NASClassesFactory() {
    }

    public static NASProblemClass getNASClass(String problem, char clss, int np) {
        if (problem.compareToIgnoreCase("IS") == 0) {
            return NASClassesFactory.getISNASClass(clss, np);
        } else if (problem.compareToIgnoreCase("CG") == 0) {
            return NASClassesFactory.getCGNASClass(clss, np);
        } else if (problem.compareToIgnoreCase("FT") == 0) {
            return NASClassesFactory.getFTNASClass(clss, np);
        } else if (problem.compareToIgnoreCase("EP") == 0) {
            return NASClassesFactory.getEPNASClass(clss, np);
        } else if (problem.compareToIgnoreCase("MG") == 0) {
            return NASClassesFactory.getMGNASClass(clss, np);
        }
        return null;
    }

    private static NASProblemClass getEPNASClass(char clss, int np) {
        EPProblemClass cl = new EPProblemClass();

        switch (clss) {
            case 'S':
                cl.m = EPClasses.S_M;
                break;
            case 'W':
                cl.m = EPClasses.W_M;
                break;
            case 'A':
                cl.m = EPClasses.A_M;
                break;
            case 'B':
                cl.m = EPClasses.B_M;
                break;
            case 'C':
                cl.m = EPClasses.C_M;
                break;
            case 'D':
                cl.m = EPClasses.D_M;
        }

        //common variables        
        cl.KERNEL_NAME = EPClasses.KERNEL_NAME;
        cl.PROBLEM_CLASS_NAME = clss;
        cl.OPERATION_TYPE = EPClasses.OPERATION_TYPE;
        cl.NUM_PROCS = np;
        cl.npm = np;
        cl.ITERATIONS = 1;
        cl.SIZE = cl.m;
        cl.SIZE_STR = "" + cl.m;
        cl.VERSION = EP_VERSION;

        return cl;
    }

    private static NASProblemClass getMGNASClass(char clss, int np) {
        MGProblemClass cl = new MGProblemClass();

        int log2_nprocs = NpbMath.ilog2(np);
        int log2_size = 0;

        switch (clss) {
            case 'S':
                log2_size = NpbMath.ilog2(MGClasses.S_PROBLEM_SIZE);
                cl.nxSz = MGClasses.S_PROBLEM_SIZE;
                cl.nySz = MGClasses.S_PROBLEM_SIZE;
                cl.nzSz = MGClasses.S_PROBLEM_SIZE;
                cl.niter = MGClasses.S_NIT;
                break;
            case 'W':
                log2_size = NpbMath.ilog2(MGClasses.W_PROBLEM_SIZE);
                cl.nxSz = MGClasses.W_PROBLEM_SIZE;
                cl.nySz = MGClasses.W_PROBLEM_SIZE;
                cl.nzSz = MGClasses.W_PROBLEM_SIZE;
                cl.niter = MGClasses.W_NIT;
                break;
            case 'A':
                log2_size = NpbMath.ilog2(MGClasses.A_PROBLEM_SIZE);
                cl.nxSz = MGClasses.A_PROBLEM_SIZE;
                cl.nySz = MGClasses.A_PROBLEM_SIZE;
                cl.nzSz = MGClasses.A_PROBLEM_SIZE;
                cl.niter = MGClasses.A_NIT;
                break;
            case 'B':
                log2_size = NpbMath.ilog2(MGClasses.B_PROBLEM_SIZE);
                cl.nxSz = MGClasses.B_PROBLEM_SIZE;
                cl.nySz = MGClasses.B_PROBLEM_SIZE;
                cl.nzSz = MGClasses.B_PROBLEM_SIZE;
                cl.niter = MGClasses.B_NIT;
                break;
            case 'C':
                log2_size = NpbMath.ilog2(MGClasses.C_PROBLEM_SIZE);
                cl.nxSz = MGClasses.C_PROBLEM_SIZE;
                cl.nySz = MGClasses.C_PROBLEM_SIZE;
                cl.nzSz = MGClasses.C_PROBLEM_SIZE;
                cl.niter = MGClasses.C_NIT;
                break;
            case 'D':
                log2_size = NpbMath.ilog2(MGClasses.D_PROBLEM_SIZE);
                cl.nxSz = MGClasses.D_PROBLEM_SIZE;
                cl.nySz = MGClasses.D_PROBLEM_SIZE;
                cl.nzSz = MGClasses.D_PROBLEM_SIZE;
                cl.niter = MGClasses.D_NIT;
        }

        //common variables
        cl.lt = log2_size;
        cl.lm = log2_size - log2_nprocs / 3;
        cl.dim = cl.lm;
        cl.ndim1 = cl.lm;
        cl.ndim2 = log2_size - (log2_nprocs + 1) / 3;
        cl.ndim3 = log2_size - (log2_nprocs + 2) / 3;

        cl.nm = 2 + (1 << cl.lm);
        cl.nv = (2 + (1 << cl.ndim1)) * (2 + (1 << cl.ndim2)) * (2 + (1 << cl.ndim3));
        cl.nm2 = 2 * cl.nm * cl.nm;
        cl.nr = (8 * (cl.nv + cl.nm * cl.nm + 5 * cl.nm + 7 * cl.lm)) / 7;

        cl.KERNEL_NAME = MGClasses.KERNEL_NAME;
        cl.PROBLEM_CLASS_NAME = clss;
        cl.OPERATION_TYPE = MGClasses.OPERATION_TYPE;
        cl.NUM_PROCS = np;
        cl.np = np;
        cl.ITERATIONS = cl.niter;
        cl.SIZE_STR = cl.nxSz + "x" + cl.nySz + "x" + cl.nzSz;
        cl.SIZE = (long) cl.nxSz * cl.nySz * cl.nzSz;
        cl.VERSION = MG_VERSION;
        cl.maxLevel = MGClasses.MAXLEVEL;

        return cl;
    }

    private static NASProblemClass getFTNASClass(char clss, int np) {
        FTProblemClass cl = new FTProblemClass();

        switch (clss) {
            case 'S':
                cl.nx = FTClasses.S_NX;
                cl.ny = FTClasses.S_NY;
                cl.nz = FTClasses.S_NZ;
                cl.niter = FTClasses.S_NITER;
                cl.vdata_real = FTClasses.vdata_real_s;
                cl.vdata_imag = FTClasses.vdata_imag_s;
                break;
            case 'W':
                cl.nx = FTClasses.W_NX;
                cl.ny = FTClasses.W_NY;
                cl.nz = FTClasses.W_NZ;
                cl.niter = FTClasses.W_NITER;
                cl.vdata_real = FTClasses.vdata_real_w;
                cl.vdata_imag = FTClasses.vdata_imag_w;
                break;
            case 'A':
                cl.nx = FTClasses.A_NX;
                cl.ny = FTClasses.A_NY;
                cl.nz = FTClasses.A_NZ;
                cl.niter = FTClasses.A_NITER;
                cl.vdata_real = FTClasses.vdata_real_a;
                cl.vdata_imag = FTClasses.vdata_imag_a;
                break;
            case 'B':
                cl.nx = FTClasses.B_NX;
                cl.ny = FTClasses.B_NY;
                cl.nz = FTClasses.B_NZ;
                cl.niter = FTClasses.B_NITER;
                cl.vdata_real = FTClasses.vdata_real_b;
                cl.vdata_imag = FTClasses.vdata_imag_b;
                break;
            case 'C':
                cl.nx = FTClasses.C_NX;
                cl.ny = FTClasses.C_NY;
                cl.nz = FTClasses.C_NZ;
                cl.niter = FTClasses.C_NITER;
                cl.vdata_real = FTClasses.vdata_real_c;
                cl.vdata_imag = FTClasses.vdata_imag_c;
                break;
            case 'D':
                cl.nx = FTClasses.D_NX;
                cl.ny = FTClasses.D_NY;
                cl.nz = FTClasses.D_NZ;
                cl.niter = FTClasses.D_NITER;
                cl.vdata_real = FTClasses.vdata_real_d;
                cl.vdata_imag = FTClasses.vdata_imag_d;
        }

        cl.dims = new int[3 + 1][3 + 1];

        if (np == 1) {
            cl.np1 = 1;
            cl.np2 = 1;
            cl.layout_type = FTClasses.LAYOUT_0D;
            for (int i = 1; i <= 3; i++) {
                cl.dims[1][i] = cl.nx;
                cl.dims[2][i] = cl.ny;
                cl.dims[3][i] = cl.nz;
            }
        } else if (np <= cl.nz) {
            cl.np1 = 1;
            cl.np2 = np;
            cl.layout_type = FTClasses.LAYOUT_1D;
            cl.dims[1][1] = cl.nx;
            cl.dims[2][1] = cl.ny;
            cl.dims[3][1] = cl.nz;

            cl.dims[1][2] = cl.nx;
            cl.dims[2][2] = cl.ny;
            cl.dims[3][2] = cl.nz;

            cl.dims[1][3] = cl.nz;
            cl.dims[2][3] = cl.nx;
            cl.dims[3][3] = cl.ny;
        } else {
            cl.np1 = cl.nz;
            cl.np2 = np / cl.nz;
            cl.layout_type = FTClasses.LAYOUT_2D;
            cl.dims[1][1] = cl.nx;
            cl.dims[2][1] = cl.ny;
            cl.dims[3][1] = cl.nz;

            cl.dims[1][2] = cl.ny;
            cl.dims[2][2] = cl.nx;
            cl.dims[3][2] = cl.nz;

            cl.dims[1][3] = cl.nz;
            cl.dims[2][3] = cl.nx;
            cl.dims[3][3] = cl.ny;
        }

        for (int i = 1; i <= 3; i++) {
            cl.dims[2][i] = cl.dims[2][i] / cl.np1;
            cl.dims[3][i] = cl.dims[3][i] / cl.np2;
        }

        int maxdim = cl.nx;
        if (cl.ny > maxdim) {
            maxdim = cl.ny;
        }
        if (cl.nz > maxdim) {
            maxdim = cl.nz;
        }
        cl.maxdim = maxdim;

        //common variables        
        cl.KERNEL_NAME = FTClasses.KERNEL_NAME;
        cl.PROBLEM_CLASS_NAME = clss;
        cl.OPERATION_TYPE = FTClasses.OPERATION_TYPE;
        cl.NUM_PROCS = np;
        cl.np = np;
        cl.ITERATIONS = cl.niter;
        cl.SIZE_STR = cl.nx + "x" + cl.ny + "x" + cl.nz;
        cl.ntotal_f = cl.nx * cl.ny * cl.nz;
        cl.ntdivnp = ((cl.nx * cl.ny) / cl.np) * cl.nz;
        cl.SIZE = (long) cl.ntotal_f;
        cl.VERSION = FT_VERSION;

        return cl;
    }

    private static NASProblemClass getCGNASClass(char clss, int np) {
        CGProblemClass cl = new CGProblemClass();

        switch (clss) {
            case 'S':
                cl.na = CGClasses.S_NA;
                cl.nonzer = CGClasses.S_NON_ZER;
                cl.shift = CGClasses.S_SHIFT;
                cl.niter = CGClasses.S_NITER;
                cl.zeta_verify_value = CGClasses.S_zeta_verify_value;
                break;
            case 'W':
                cl.na = CGClasses.W_NA;
                cl.nonzer = CGClasses.W_NON_ZER;
                cl.shift = CGClasses.W_SHIFT;
                cl.niter = CGClasses.W_NITER;
                cl.zeta_verify_value = CGClasses.W_zeta_verify_value;
                break;
            case 'A':
                cl.na = CGClasses.A_NA;
                cl.nonzer = CGClasses.A_NON_ZER;
                cl.shift = CGClasses.A_SHIFT;
                cl.niter = CGClasses.A_NITER;
                cl.zeta_verify_value = CGClasses.A_zeta_verify_value;
                break;
            case 'B':
                cl.na = CGClasses.B_NA;
                cl.nonzer = CGClasses.B_NON_ZER;
                cl.shift = CGClasses.B_SHIFT;
                cl.niter = CGClasses.B_NITER;
                cl.zeta_verify_value = CGClasses.B_zeta_verify_value;
                break;
            case 'C':
                cl.na = CGClasses.C_NA;
                cl.nonzer = CGClasses.C_NON_ZER;
                cl.shift = CGClasses.C_SHIFT;
                cl.niter = CGClasses.C_NITER;
                cl.zeta_verify_value = CGClasses.C_zeta_verify_value;
                break;
            case 'D':
                cl.na = CGClasses.D_NA;
                cl.nonzer = CGClasses.D_NON_ZER;
                cl.shift = CGClasses.D_SHIFT;
                cl.niter = CGClasses.D_NITER;
                cl.zeta_verify_value = CGClasses.D_zeta_verify_value;
        }

        //common variables        
        cl.KERNEL_NAME = CGClasses.KERNEL_NAME;
        cl.PROBLEM_CLASS_NAME = clss;
        cl.OPERATION_TYPE = CGClasses.OPERATION_TYPE;
        cl.NUM_PROCS = np;
        cl.ITERATIONS = cl.niter;
        cl.SIZE_STR = "" + cl.na;
        cl.SIZE = cl.na;
        cl.VERSION = CG_VERSION;
        cl.rcond = CGClasses.RCOND;

        return cl;
    }

    private static ISProblemClass getISNASClass(char clss, int np) {
        ISProblemClass cl = new ISProblemClass();

        switch (clss) {
            case 'S':
                cl.test_index_array = ISClasses.S_test_index_array;
                cl.test_rank_array = ISClasses.S_test_rank_array;
                cl.TOTAL_KEYS_LOG_2 = ISClasses.S_TOTAL_KEYS_LOG_2;
                cl.MAX_KEY_LOG_2 = ISClasses.S_MAX_KEY_LOG_2;
                cl.NUM_BUCKETS_LOG_2 = ISClasses.S_NUM_BUCKETS_LOG_2;

                break;
            case 'W':
                cl.test_index_array = ISClasses.W_test_index_array;
                cl.test_rank_array = ISClasses.W_test_rank_array;
                cl.TOTAL_KEYS_LOG_2 = ISClasses.W_TOTAL_KEYS_LOG_2;
                cl.MAX_KEY_LOG_2 = ISClasses.W_MAX_KEY_LOG_2;
                cl.NUM_BUCKETS_LOG_2 = ISClasses.W_NUM_BUCKETS_LOG_2;

                break;
            case 'A':
                cl.test_index_array = ISClasses.A_test_index_array;
                cl.test_rank_array = ISClasses.A_test_rank_array;
                cl.TOTAL_KEYS_LOG_2 = ISClasses.A_TOTAL_KEYS_LOG_2;
                cl.MAX_KEY_LOG_2 = ISClasses.A_MAX_KEY_LOG_2;
                cl.NUM_BUCKETS_LOG_2 = ISClasses.A_NUM_BUCKETS_LOG_2;

                break;
            case 'B':
                cl.test_index_array = ISClasses.B_test_index_array;
                cl.test_rank_array = ISClasses.B_test_rank_array;
                cl.TOTAL_KEYS_LOG_2 = ISClasses.B_TOTAL_KEYS_LOG_2;
                cl.MAX_KEY_LOG_2 = ISClasses.B_MAX_KEY_LOG_2;
                cl.NUM_BUCKETS_LOG_2 = ISClasses.B_NUM_BUCKETS_LOG_2;

                break;
            case 'C':
                cl.test_index_array = ISClasses.C_test_index_array;
                cl.test_rank_array = ISClasses.C_test_rank_array;
                cl.TOTAL_KEYS_LOG_2 = ISClasses.C_TOTAL_KEYS_LOG_2;
                cl.MAX_KEY_LOG_2 = ISClasses.C_MAX_KEY_LOG_2;
                cl.NUM_BUCKETS_LOG_2 = ISClasses.C_NUM_BUCKETS_LOG_2;

                break;
            case 'D':
                System.err.println("Warning: Not yet implemented for 1 and 2 worker, over it's ok");
                cl.test_index_array = ISClasses.D_test_index_array;
                cl.test_rank_array = ISClasses.D_test_rank_array;
                cl.TOTAL_KEYS_LOG_2 = ISClasses.D_TOTAL_KEYS_LOG_2;
                cl.MAX_KEY_LOG_2 = ISClasses.D_MAX_KEY_LOG_2;
                cl.NUM_BUCKETS_LOG_2 = ISClasses.D_NUM_BUCKETS_LOG_2;
        }

        //common variables 
        cl.KERNEL_NAME = ISClasses.KERNEL_NAME;
        cl.PROBLEM_CLASS_NAME = clss;
        cl.OPERATION_TYPE = ISClasses.OPERATION_TYPE;
        cl.NUM_PROCS = np;
        cl.TEST_ARRAY_SIZE = ISClasses.TEST_ARRAY_SIZE;
        cl.MAX_ITERATIONS = ISClasses.MAX_ITERATIONS;
        cl.TOTAL_KEYS = (1L << cl.TOTAL_KEYS_LOG_2);
        cl.MAX_KEY = (1 << cl.MAX_KEY_LOG_2);
        cl.NUM_BUCKETS = (1 << cl.NUM_BUCKETS_LOG_2);
        cl.NUM_KEYS = (int) (cl.TOTAL_KEYS / cl.NUM_PROCS); // warning may go out with D class and few procs
        cl.ITERATIONS = cl.MAX_ITERATIONS;
        cl.SIZE_STR = "" + cl.TOTAL_KEYS;
        cl.SIZE = cl.TOTAL_KEYS;
        cl.VERSION = IS_VERSION;

        /** ************************************************************** */

        /* On larger numbers of processors, since the keys are (roughly) */
        /* gaussian distributed, the first and last processor sort keys */
        /* in a large interval, requiring array sizes to be larger. Note */
        /* that for large NUM_PROCS, NUM_KEYS is, however, a small number */

        /** ************************************************************** */
        if (cl.NUM_PROCS < 256) { // warning may go out with D class and few procs
            cl.SIZE_OF_BUFFERS = (3 * cl.NUM_KEYS) / 2;
        } else {
            cl.SIZE_OF_BUFFERS = (3 * cl.NUM_KEYS);
        }

        return cl;
    }

    // don't use please
    public static NASProblemClass getCustomNASClass(String problem, char clss, int np, int totalKeysLog2,
            int maxKeysLog2, int numBucketsLog2, int[] test_index_array, int[] test_rank_array,
            int maxIteration) {
        if (problem.compareToIgnoreCase("IS") == 0) {
            return NASClassesFactory.getCustomISNASClass(clss, np, totalKeysLog2, maxKeysLog2,
                    numBucketsLog2, test_index_array, test_rank_array, maxIteration);
        } else if (problem.compareToIgnoreCase("CG") == 0) {
            return null;
        } else if (problem.compareToIgnoreCase("BG") == 0) {
            return null;
        } else if (problem.compareToIgnoreCase("MG") == 0) {
            return null;
        } else if (problem.compareToIgnoreCase("FT") == 0) {
            return null;
        } else if (problem.compareToIgnoreCase("EP") == 0) {
            return null;
        }
        return null;
    }

    private static ISProblemClass getCustomISNASClass(char clss, int np, int totalKeysLog2, int maxKeysLog2,
            int numBucketsLog2, int[] test_index_array, int[] test_rank_array, int maxIteration) {
        ISProblemClass cl = new ISProblemClass();

        cl.test_index_array = test_index_array;
        cl.test_rank_array = test_rank_array;
        cl.TOTAL_KEYS_LOG_2 = totalKeysLog2;
        cl.MAX_KEY_LOG_2 = maxKeysLog2;
        cl.NUM_BUCKETS_LOG_2 = numBucketsLog2;

        cl.PROBLEM_CLASS_NAME = clss;
        cl.NUM_PROCS = np;
        cl.TEST_ARRAY_SIZE = test_index_array.length;
        cl.MAX_ITERATIONS = maxIteration;
        cl.TOTAL_KEYS = (1 << cl.TOTAL_KEYS_LOG_2);
        cl.MAX_KEY = (1 << cl.MAX_KEY_LOG_2);
        cl.NUM_BUCKETS = (1 << cl.NUM_BUCKETS_LOG_2);
        cl.NUM_KEYS = (int) (cl.TOTAL_KEYS / cl.NUM_PROCS);
        cl.SIZE_OF_BUFFERS = (3 * cl.NUM_KEYS) / 2;

        return cl;
    }
}
