/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.ft;

import java.io.IOException;
import java.net.URL;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.process.JVMProcessImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.FunctionalTest;
import functionalTests.GCMFunctionalTestDefaultNodes;


/**
 * Common methods for FT non functional tests
 */
public class AbstractFTTezt extends FunctionalTest {

    protected JVMProcessImpl server;
    public static int AWAITED_RESULT = 1771014405;

    /**
     * Create a FT server on localhost:1100
     */
    protected void startFTServer(String protocol) throws IOException {
        this.server = new JVMProcessImpl(
            new org.objectweb.proactive.core.process.AbstractExternalProcess.StandardOutputMessageLogger());
        this.server.setClassname("org.objectweb.proactive.core.body.ft.servers.StartFTServer");
        this.server.setJvmOptions(super.getJvmParameters());
        this.server.setParameters("-proto " + protocol);
        this.server.startProcess();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Kill the FT Server
     */
    protected void stopFTServer() {
        this.server.stopProcess();
    }

    /**
     * Deploy two agents and start a dummy computation
     * @param gcmApplicationFile the deployment file  
     * @return the result of the computation
     */
    protected int deployAndStartAgents(URL gcmApplicationFile) throws ProActiveException {
        GCMApplication gcma;
        GCMVirtualNode vnode;

        //	create nodes
        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, "4",
                VariableContractType.DescriptorDefaultVariable);
        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, "1",
                VariableContractType.DescriptorDefaultVariable);

        gcma = PAGCMDeployment.loadApplicationDescriptor(gcmApplicationFile, vContract);
        gcma.startDeployment();
        vnode = gcma.getVirtualNode("Workers");
        Node[] nodes = new Node[2];
        nodes[0] = vnode.getANode();
        nodes[1] = vnode.getANode();

        Agent a = PAActiveObject.newActive(Agent.class, new Object[0], nodes[0]);
        Agent b = PAActiveObject.newActive(Agent.class, new Object[0], nodes[1]);

        // not ft !
        Collector c = PAActiveObject.newActive(Collector.class, new Object[0]);

        a.initCounter(1);
        b.initCounter(1);
        a.setNeighbour(b);
        b.setNeighbour(a);
        a.setLauncher(c);

        c.go(a, 1000);

        //failure in 11 sec...
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        try {
            nodes[1].getProActiveRuntime().killRT(false);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        ReInt r = c.getResult();
        return r.getValue();
    }

}
