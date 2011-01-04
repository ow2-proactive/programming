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
package functionalTests.masterworker.userexception;

import static junit.framework.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;

import functionalTests.FunctionalTest;
import functionalTests.masterworker.A;


/**
 * Test load balancing
 */
public class TestUserEx extends FunctionalTest {
    private URL descriptor = TestUserEx.class
            .getResource("/functionalTests/masterworker/TestMasterWorker.xml");
    private Master<A, Integer> master;
    private List<A> tasks;
    public static final int NB_TASKS = 4;

    @org.junit.Test
    public void action() throws Exception {
        boolean catched = false;

        master.solve(tasks);
        try {
            List<Integer> ids = master.waitAllResults();
            // we don't care of the results
            ids.clear();
        } catch (TaskException e) {
            assertTrue("Expected exception is the cause", e.getCause() instanceof ArithmeticException);
            System.out.println("Expected exception catched, here it is : ");
            e.printStackTrace();
            catched = true;
        }
        assertTrue("Exception caught as excepted", catched);
    }

    @Before
    public void initTest() throws Exception {
        tasks = new ArrayList<A>();
        for (int i = 0; i < NB_TASKS; i++) {
            // tasks that throw an exception
            A t = new A(i, 0, true);
            tasks.add(t);
        }
        //@snippet-start master_creation        
        master = new ProActiveMaster<A, Integer>();
        //@snippet-end master_creation   
        master.addResources(descriptor, super.vContract);
    }

    @After
    public void endTest() throws Exception {
        master.terminate(true);
    }
}
