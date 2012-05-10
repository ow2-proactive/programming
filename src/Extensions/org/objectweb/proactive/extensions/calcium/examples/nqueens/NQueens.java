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
package org.objectweb.proactive.extensions.calcium.examples.nqueens;

import java.io.Serializable;

import org.objectweb.proactive.extensions.calcium.Calcium;
import org.objectweb.proactive.extensions.calcium.Stream;
import org.objectweb.proactive.extensions.calcium.environment.Environment;
import org.objectweb.proactive.extensions.calcium.environment.EnvironmentFactory;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1.DivideBT1;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt1.SolveBT1;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2.DivideBT2;
import org.objectweb.proactive.extensions.calcium.examples.nqueens.bt2.SolveBT2;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.futures.CalFuture;
import org.objectweb.proactive.extensions.calcium.monitor.Monitor;
import org.objectweb.proactive.extensions.calcium.monitor.SimpleLogMonitor;
import org.objectweb.proactive.extensions.calcium.skeletons.DaC;
import org.objectweb.proactive.extensions.calcium.skeletons.Fork;
import org.objectweb.proactive.extensions.calcium.skeletons.Seq;
import org.objectweb.proactive.extensions.calcium.skeletons.Skeleton;


public class NQueens implements Serializable {
    public Skeleton<Board, Result> root;

    public static void main(String[] args) throws Exception {
        NQueens nq = new NQueens();

        if (args.length != 5) {
            System.out.println("Wrong number of arguments");
            System.out.println("[Usage] org.objectweb.proactive.extensions.calcium.examples.nqueens.NQueens"
                + " descriptor virtualnode boardSize solvableSize times");
            System.exit(-1);
            //            nq.solve(16, 14, 5, NQueens.class.getResource("GCMEnvironmentApplication.xml").getPath(),
            //                    "local");
        } else {
            nq.solve(Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]),
                    args[0], args[1]);
        }

        System.exit(0);
    }

    @SuppressWarnings("unchecked")
    public NQueens() {
        Skeleton<Board, Result> BT1 = new DaC<Board, Result>(new DivideBT1(), new DivideCondition(),
            new Seq<Board, Result>(new SolveBT1()), new ConquerBoard());

        Skeleton<Board, Result> BT2 = new DaC<Board, Result>(new DivideBT2(), new DivideCondition(),
            new Seq<Board, Result>(new SolveBT2()), new ConquerBoard());

        root = new Fork<Board, Result>(new ConquerBoard(), BT1, BT2);
    }

    public void solve(int boardSize, int solvableSize, int times, String descriptor, String virtualNode)
            throws Exception {

        //Environment environment = EnvironmentFactory.newMultiThreadedEnvironment(2);
        //Environment environment = EnvironmentFactory.newProActiveEnvironment(descriptor);
        Environment environment = EnvironmentFactory.newProActiveEnviromentWithGCMDeployment(descriptor);
        //Environment environment = ProActiveSchedulerEnvironment.factory("localhost","chri", "chri");

        Calcium calcium = new Calcium(environment);
        // @snippet-start calcium_simpleLogMonitor
        Monitor monitor = new SimpleLogMonitor(calcium, 1);
        monitor.start();
        // ...
        // @snippet-break calcium_simpleLogMonitor
        calcium.boot();

        Stream<Board, Result> stream = calcium.newStream(root);

        for (int i = 0; i < times; i++) {
            stream.submit(new Board(boardSize, solvableSize));
        }

        try {
            for (; times > 0; times--) {
                CalFuture<Result> future = stream.retrieve();
                Result res = future.get();
                System.out.println(res);
                System.out.println(future.getStats());
            }
        } catch (MuscleException e) {
            e.printStackTrace();
        }
        calcium.shutdown();
        // @snippet-resume calcium_simpleLogMonitor
        monitor.stop();
        // @snippet-end calcium_simpleLogMonitor
    }
}