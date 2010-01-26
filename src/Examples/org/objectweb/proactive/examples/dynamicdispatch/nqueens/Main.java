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
package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

        String deploymentDescriptorLocation = args[0];
        String virtualNodeName = args[1];
        int boardSize = Integer.parseInt(args[2]);
        int depth = Integer.parseInt(args[3]);

        ProActiveDescriptor desc = PADeployment.getProactiveDescriptor(deploymentDescriptorLocation);
        desc.activateMappings();
        WorkerItf workers = (WorkerItf) PAGroup.newGroupInParallel(Worker.class.getName(),
                new Object[] { "worker " }, desc.getVirtualNode(virtualNodeName).getNodes());
        //		Worker workers = (Worker)PAGroup.newGroup(Worker.class.getName(), new Object[][]{{"1"}, {"2"},  {"3"}});
        System.out.println("waiting for input...");
        //		System.console().readLine();
        PAGroup.setDispatchMode(workers, DispatchMode.DYNAMIC, 2);
        //		// Getting command line parameters
        //        instance.init(args, 2, " nqueen_board_size nqueen_algorithm_depth");
        //
        //        // Creating the Master
        //        instance.master = new ProActiveMaster<QueryExtern, Pair<Long, Long>>();
        //        // Register shutdown process
        //        instance.registerHook();
        //        instance.master.addResources(instance.descriptor_url, instance.vn_name);

        System.out.println("Launching NQUEENS solutions finder for n = " + boardSize + " with a depth of " +
            depth);

        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%% pinging...");
        BooleanWrapper bw = workers.ping();
        //        System.out.println(PAGroup.getGroup(bw).get(0));

        long sumResults = 0;
        long sumTime = 0;
        long startTime = System.currentTimeMillis();

        // Generating the queries for the NQueens
        Vector<Query> unresolvedqueries = QueryGenerator.generateQueries(boardSize, depth);
        System.out.println("\n" + unresolvedqueries.size() + " queries to execute");
        //		System.out.println(Arrays.toString(unresolvedqueries.toArray()));

        // Splitting Queries
        // refactor this and use only one method !
        Query toSolve = (Query) PAGroup.newGroup(Query.class.getName());
        //        while (!unresolvedqueries.isEmpty()) {
        //            Query query = unresolvedqueries.remove(0);
        //            Vector<Query> split = QueryGenerator.splitAQuery(query);
        //            if (!split.isEmpty()) {
        //                for (Query splitquery : split) {
        //                    PAGroup.getGroup(toSolve).add(splitquery);
        //                }
        //            } else {
        //            	PAGroup.getGroup(toSolve).add(query);
        //            }
        //        }
        PAGroup.getGroup(toSolve).addAll(unresolvedqueries);
        PAGroup.setScatterGroup(toSolve);

        Result result = workers.solve(toSolve);

        long finalValue = 0;
        long totalComputationTime = 0;
        List<Result> results = (List<Result>) PAGroup.getGroup(result);
        for (Iterator<Result> iterator = results.iterator(); iterator.hasNext();) {
            Result next = iterator.next();
            finalValue += next.getComputedValue();
            totalComputationTime += next.getComputationTime();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("final result is " + finalValue);
        System.out.println("total time  " + (endTime - startTime) + " ms");
        System.out.println("actual computation time  " + totalComputationTime + " ms");

        workers.printNbSolvedQueries();
    }

}
