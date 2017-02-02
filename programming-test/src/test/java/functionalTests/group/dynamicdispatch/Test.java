/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionalTests.group.dynamicdispatch;

import org.junit.Assert;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;
import functionalTests.descriptor.defaultnodes.TestNodes;


// dispatch n tasks between 2 workers with n>2
// task 1 on worker 1 sleeps for a while
// --> check that worker 2 processed n-1 tasks
public class Test extends FunctionalTest {
    int nbTasks = 10;

    @org.junit.Test
    public void action() throws Exception {
        TestNodes tn = new TestNodes();
        tn.action();

        Object[][] params = { { 0 }, { 1 } };

        Node[] nodes = { TestNodes.getSameVMNode(), TestNodes.getLocalVMNode(), TestNodes.getRemoteVMNode() };

        Task tasks = (Task) PAGroup.newGroup(Task.class.getName());
        Group<Task> taskGroup = PAGroup.getGroup(tasks);
        for (int i = 0; i < nbTasks; i++) {
            taskGroup.add(new Task(i));
        }

        Worker workers = (Worker) PAGroup.newGroup(Worker.class.getName(), params, nodes);

        //PAGroup.setScatterGroup(taskGroup); // grrr...: this simply does NOT work! 
        PAGroup.setScatterGroup(tasks); // we have to use the TYPED group...
        PAGroup.setDispatchMode(workers, DispatchMode.DYNAMIC, 1);

        Task results = workers.executeTask(tasks);
        validateDynamicallyDispatchedTasks(results);
        //		

        // test with annotation
        Worker workers2 = (Worker) PAGroup.newGroup(Worker.class.getName(), params, nodes);

        // do not use api for setting balancing mode

        tasks = (Task) PAGroup.newGroup(Task.class.getName());
        taskGroup = PAGroup.getGroup(tasks);
        for (int i = 0; i < nbTasks; i++) {
            taskGroup.add(new Task(i));
        }
        PAGroup.setScatterGroup(tasks); // we have to use the TYPED group...
        results = workers2.executeDynamically(tasks);
        validateDynamicallyDispatchedTasks(results);

    }

    private void validateDynamicallyDispatchedTasks(Task results) {
        Group<Task> resultGroup = PAGroup.getGroup(results);

        Assert.assertTrue(resultGroup.size() == nbTasks);

        PAGroup.waitAll(results);
        int nbTasksForWorker0 = 0;
        int nbTasksForWorker1 = 0;
        for (int i = 0; i < nbTasks; i++) {
            if (resultGroup.get(i).getExecutionWorker() == 0) {
                nbTasksForWorker0++;
            } else if (resultGroup.get(i).getExecutionWorker() == 1) {
                nbTasksForWorker1++;
            }
        }
        System.out.println("worker 0: " + nbTasksForWorker0);
        System.out.println("worker 1: " + nbTasksForWorker1);
        Assert.assertTrue(nbTasksForWorker0 == 1);
        Assert.assertTrue(nbTasksForWorker1 == (nbTasks - 1));
    }

}
