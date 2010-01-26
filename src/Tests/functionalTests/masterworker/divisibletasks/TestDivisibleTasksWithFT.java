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

import functionalTests.FunctionalTest;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.masterworker.interfaces.Master;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.junit.Before;
import org.junit.After;
import static junit.framework.Assert.assertTrue;


/**
 * TestDivisibleTasksWithFT
 *
 * @author The ProActive Team
 */
public class TestDivisibleTasksWithFT extends FunctionalTest {
    private URL descriptor = TestDivisibleTasksWithFT.class.getResource("MasterWorkerFT.xml");
    private URL descriptor2 = TestDivisibleTasksWithFT.class.getResource("MasterWorkerFT2.xml");
    private Master<DaCSort, ArrayList<Integer>> master;
    private List<DaCSort> tasks;
    public static final int NB_ELEM = 10000;
    private GCMApplication pad;
    private GCMApplication pad2;
    private GCMVirtualNode vn1;
    private GCMVirtualNode vn2;

    @Before
    public void initTest() throws Exception {

        this.pad = PAGCMDeployment.loadApplicationDescriptor(descriptor,
                (VariableContractImpl) super.vContract.clone());
        this.pad.startDeployment();
        this.vn1 = this.pad.getVirtualNode("VN1");
        this.vn1.waitReady();
        System.out.println("VN1 is ready");
        this.pad2 = PAGCMDeployment.loadApplicationDescriptor(descriptor2,
                (VariableContractImpl) super.vContract.clone());
        this.pad2.startDeployment();
        this.vn2 = this.pad2.getVirtualNode("VN2");
        this.vn2.waitReady();
        System.out.println("VN2 is ready");

        master = new ProActiveMaster<DaCSort, ArrayList<Integer>>();
        master.addResources(vn1.getCurrentNodes());
        master.addResources(vn2.getCurrentNodes());
        master.setResultReceptionOrder(Master.SUBMISSION_ORDER);
        master.setInitialTaskFlooding(1);
        master.setPingPeriod(100);

        tasks = new ArrayList<DaCSort>();
        ArrayList<Integer> bigList = new ArrayList<Integer>();
        for (int i = 0; i < NB_ELEM; i++) {
            bigList.add((int) Math.round(Math.random() * NB_ELEM));
        }
        tasks.add(new DaCSort(bigList));
    }

    @org.junit.Test
    public void action() throws Exception {

        master.solve(tasks);

        Thread.sleep(3000);

        pad2.kill();

        ArrayList<Integer> answer = master.waitOneResult();

        for (int i = 0; i < answer.size() - 1; i++) {
            assertTrue("List sorted", answer.get(i) <= answer.get(i + 1));
        }
        master.solve(tasks);
        Thread.sleep(2000);
        master.clear();

        master.solve(tasks);
        answer = master.waitOneResult();

        for (int i = 0; i < answer.size() - 1; i++) {
            assertTrue("List sorted", answer.get(i) <= answer.get(i + 1));
        }
    }

    @After
    public void endTest() throws Exception {
        master.terminate(false);
        pad.kill();
    }
}
