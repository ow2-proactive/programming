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
package org.objectweb.proactive.benchmarks.NAS.IS;

import org.objectweb.proactive.benchmarks.NAS.NASProblemClass;

import java.io.Serializable;


/**
 * Kernel IS
 * 
 * A large integer sort. This kernel performs a sorting operation that is
 * important in "particle method" codes. It tests both integer computation
 * speed and communication performance.
 */
public class ISProblemClass extends NASProblemClass implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;
    public int[] test_index_array;
    public int[] test_rank_array;

    /**
     * Class instance of kernel IS
     */
    public int NUM_KEYS;

    /**
     * Number of iteration
     */
    public int MAX_ITERATIONS;

    /**
     * Array's size for partial verification
     */
    public int TEST_ARRAY_SIZE;
    public int TOTAL_KEYS_LOG_2;
    public int MAX_KEY_LOG_2;
    public int NUM_BUCKETS_LOG_2;
    public long TOTAL_KEYS;
    public int MAX_KEY;
    public int NUM_BUCKETS;
    public int SIZE_OF_BUFFERS;
}
