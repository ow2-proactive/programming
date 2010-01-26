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
package functionalTests.masterworker.divisibletasks;

import org.objectweb.proactive.extensions.masterworker.interfaces.DivisibleTask;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;


/**
 * A merge-sort like task
 * splits the list into 2 sublists and merge results from subtasks
 *
 @author The ProActive Team
 */
public class DaCSort implements DivisibleTask<ArrayList<Integer>> {

    /**
     * 
     */
    private static final long serialVersionUID = 420L;

    public static final int MIN_LIST_TO_SPLIT = 1000;

    private ArrayList<Integer> input;

    public DaCSort(ArrayList<Integer> input) {
        this.input = input;
    }

    public ArrayList<Integer> run(WorkerMemory memory,
            SubMaster<Task<ArrayList<Integer>>, ArrayList<Integer>> master) throws Exception {
        ArrayList<Integer> l1 = new ArrayList<Integer>(input.subList(0, input.size() / 2));
        ArrayList<Integer> l2 = new ArrayList<Integer>(input.subList(input.size() / 2 + 1, input.size()));

        if (l1.size() < MIN_LIST_TO_SPLIT) {
            ArrayList<Task<ArrayList<Integer>>> tasks = new ArrayList<Task<ArrayList<Integer>>>();
            tasks.add(new FinalSort(l1));
            tasks.add(new FinalSort(l2));
            master.solve(tasks);
        } else {
            ArrayList<Task<ArrayList<Integer>>> tasks = new ArrayList<Task<ArrayList<Integer>>>();
            tasks.add(new DaCSort(l1));
            tasks.add(new DaCSort(l2));
            master.solve(tasks);
        }

        List<ArrayList<Integer>> results = master.waitAllResults();
        if (!master.isEmpty()) {
            throw new IllegalStateException("Master is not empty");
        }
        if (master.countAvailableResults() != 0) {
            throw new IllegalStateException("Master is not empty");
        }
        return merge(results.get(0), results.get(1));

    }

    private static ArrayList<Integer> merge(ArrayList<Integer> l1, ArrayList<Integer> l2) {
        // merging
        Iterator<Integer> it1 = l1.iterator();
        Iterator<Integer> it2 = l2.iterator();

        ArrayList<Integer> answer = new ArrayList<Integer>();

        int a = it1.next();
        int b = it2.next();

        do {
            while ((a <= b) && answer.add(a) && it1.hasNext()) {
                a = it1.next();
            }
            while ((a > b) && answer.add(b) && it2.hasNext()) {
                b = it2.next();
            }
        } while (it1.hasNext() && it2.hasNext());

        while (it1.hasNext()) {
            answer.add(it1.next());
        }
        while (it2.hasNext()) {
            answer.add(it2.next());
        }
        return answer;
    }

    public static void main(String[] args) {
        ArrayList<Integer> l1 = new ArrayList<Integer>();
        ArrayList<Integer> l2 = new ArrayList<Integer>();
        for (int i = 0; i < 100; i++) {
            l1.add((int) Math.round(Math.random() * 1000));
            l2.add((int) Math.round(Math.random() * 1000));
        }
        Collections.sort(l1);
        Collections.sort(l2);
        ArrayList<Integer> l3 = merge(l1, l2);
        for (int i = 0; i < l3.size() - 1; i++) {
            if (l3.get(i) > l3.get(i + 1)) {
                throw new IllegalStateException("List not sorted");
            }
        }
    }

    public ArrayList<Integer> run(WorkerMemory memory) throws Exception {
        throw new UnsupportedOperationException();
    }
}
