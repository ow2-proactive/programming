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
package functionalTests.activeobject.request.forgetonsend;

import static junit.framework.Assert.assertTrue;

import java.net.URL;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.utils.OperatingSystem;

import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.ft.AbstractFTTezt;
import functionalTests.ft.cic.TestCIC;


public class TestFaultTolerance extends AbstractFTTezt {

    private JVMProcessImpl server;
    private static URL FT_XML_LOCATION_UNIX = TestCIC.class
            .getResource("/functionalTests/ft/cic/testFT_CIC.xml");

    /**
     * We will try to perform a failure during a sending, and then verify that the sending restart
     * from the new location
     * 
     * @throws Exception
     */
    @org.junit.Test
    public void action() throws Exception {

        this.startFTServer("cic");

        GCMApplication gcma;
        GCMVirtualNode vnode;

        vContract.setVariableFromProgram("os", OperatingSystem.getOperatingSystem().name(),
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, "4",
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, "1",
                VariableContractType.DescriptorDefaultVariable);

        //	create nodes

        gcma = PAGCMDeployment.loadApplicationDescriptor(FT_XML_LOCATION_UNIX, vContract);
        gcma.startDeployment();
        vnode = gcma.getVirtualNode("Workers");
        Node[] nodes = new Node[2];
        nodes[0] = vnode.getANode();
        nodes[1] = vnode.getANode();

        FTObject a = PAActiveObject.newActive(FTObject.class, new Object[] { "a" }, nodes[0]);
        FTObject b = (FTObject) PAActiveObject.newActive(FTObject.class.getName(), new Object[] { "b" },
                nodes[1]);

        // Fault tolerance issue: checkpoint is triggered communication
        // A non communicating appli cannot be correctly checkpointed -> add some pings to
        // trigger minimal activity

        a.ping();
        b.ping();

        a.init(b); // Will produce b.a(), b.b() and b.c()

        Thread.sleep(10000);

        // second checkpoint ...
        a.ping();
        b.ping();

        Thread.sleep(10000);

        try {
            nodes[0].getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        Thread.sleep(3000);
        boolean result = b.getServices().equals("abc");

        // cleaning
        this.stopFTServer();

        assertTrue(result);
    }
}
