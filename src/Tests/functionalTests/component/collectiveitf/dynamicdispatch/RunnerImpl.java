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
package functionalTests.component.collectiveitf.dynamicdispatch;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;


public class RunnerImpl implements RunnerItf, org.objectweb.fractal.api.control.BindingController {

    RequiredService services;
    int nbTasks = 10;

    public boolean runTest() {

        List<Task> tasks = new ArrayList<Task>();
        for (int i = 0; i < nbTasks; i++) {
            tasks.add(new Task(i));
        }
        List<Result> results = services.execute(tasks);

        Assert.assertEquals(nbTasks, results.size());

        int nbTasksForWorker0 = 0;
        int nbTasksForWorker1 = 0;
        for (int i = 0; i < nbTasks; i++) {
            if (results.get(i).getWorkerIndex() == 0) {
                nbTasksForWorker0++;
            } else if (results.get(i).getWorkerIndex() == 1) {
                nbTasksForWorker1++;
            }
        }
        System.out.println("worker 0: " + nbTasksForWorker0);
        System.out.println("worker 1: " + nbTasksForWorker1);
        Assert.assertTrue(nbTasksForWorker0 == 1);
        Assert.assertTrue(nbTasksForWorker1 == (nbTasks - 1));

        //		// run unicast test
        //		
        //		List<Integer> parameters = new ArrayList<Integer>();
        //		parameters.add(1);
        //		parameters.add(10);
        //		
        //		// first dispatch
        //		Assert.assertEquals(new IntWrapper(11), services.method1(parameters));
        //		
        //		// second dispatch
        ////		Assert.assertEquals("server 2 received parameter 2", services.method1(parameters));
        //		
        //		// has been executed
        return true;
    }

    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        if (clientItfName.equals("requiredServiceItf")) {
            services = (RequiredService) serverItf;
        } else {
            throw new NoSuchInterfaceException(clientItfName);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#listFc()
     */
    public String[] listFc() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#lookupFc(java.lang.String)
     */
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        if ("requiredServiceItf".equals(clientItfName)) {
            return services;
        }
        throw new NoSuchInterfaceException(clientItfName);
    }

    /*
     * @see org.objectweb.fractal.api.control.BindingController#unbindFc(java.lang.String)
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {
        throw new RuntimeException("not implemented");
    }

}
