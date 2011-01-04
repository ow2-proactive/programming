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
package org.objectweb.proactive.examples.dynamicdispatch.nqueens;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public class Worker implements WorkerItf {

    String name;
    int counter = 0;
    long startTime;
    static int instance = 0;

    public Worker() {
    }

    public Worker(String name) {
        try {
            this.name = name + InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("created worker " + this.name + " " + instance++);

    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.dynamicdispatch.nqueens.WorkerItf2#solve(org.objectweb.proactive.examples.dynamicdispatch.nqueens.Query)
     */
    public Result solve(Query query) {
        long initTime = System.currentTimeMillis();
        try {
            if (InetAddress.getLocalHost().getHostName().contains("rahue")) {
                Thread.sleep(1000);
            }
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (counter == 0) {
            startTime = System.currentTimeMillis();
        }
        counter++;
        //		System.out.println("[worker " + name + "] ["+counter+"] solving query " + query);
        //		System.out.println("[worker " + name + "] ["+counter+"] solving query ");

        long result = query.run();
        try {
            System.out.println(InetAddress.getLocalHost().getHostName() + " solving query " + counter + "  ");
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        long totalTime = System.currentTimeMillis() - initTime;
        //		System.out.println("+++++++++ " + query.toString() + " result is : " + result);

        File resultFile = new File("nqueen-partial-results.txt");
        //		try {
        ////			resultFile.createNewFile();
        //		} catch (IOException e1) {
        //			// TODO Auto-generated catch block
        //			e1.printStackTrace();
        //		}
        try {
            PrintWriter pw = new PrintWriter(resultFile).append("" + result);
            //			pw.append("sdfasdfasdfasfdasdfsdf");
            pw.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(query + " computation time = " + totalTime);

        return new Result(result, totalTime);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.dynamicdispatch.nqueens.WorkerItf2#getName()
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.dynamicdispatch.nqueens.WorkerItf2#printNbSolvedQueries()
     */
    public void printNbSolvedQueries() {
        System.out.println(name + " solved " + counter + " queries in " +
            (System.currentTimeMillis() - startTime) / 1000 + " seconds");
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.examples.dynamicdispatch.nqueens.WorkerItf2#ping()
     */
    public BooleanWrapper ping() throws Exception {
        System.out.println(name + " pinged");
        return new BooleanWrapper(true);
    }

}
