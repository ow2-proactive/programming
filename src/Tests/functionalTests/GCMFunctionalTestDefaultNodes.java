/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests;

import java.net.URL;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * If a VariableContract is needed, before() and after() can be overridden.
 * 
 * @author The ProActive Team
 * 
 */
public class GCMFunctionalTestDefaultNodes extends GCMFunctionalTest {

    static final private URL defaultApplicationDescriptor = FunctionalTest.class
            .getResource("/functionalTests/_CONFIG/JunitApp.xml");

    static public final String VN_NAME = "nodes";
    static public final String VAR_DEPDESCRIPTOR = "deploymentDescriptor";
    static public final String VAR_JVMARG = "jvmargDefinedByTest";

    static public final String VAR_HOSTCAPACITY = "hostCapacity";
    int hostCapacity;

    static public final String VAR_VMCAPACITY = "vmCapacity";
    int vmCapacity;

    public GCMFunctionalTestDefaultNodes(int hostCapacity, int vmCapacity) {
        super(defaultApplicationDescriptor);

        this.hostCapacity = hostCapacity;
        this.vmCapacity = vmCapacity;

        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, new Integer(
            hostCapacity).toString(), VariableContractType.DescriptorDefaultVariable);
        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, new Integer(
            vmCapacity).toString(), VariableContractType.DescriptorDefaultVariable);

    }

    public Node getANode() {
        return getANodeFrom(VN_NAME);
    }

    private Node getANodeFrom(String vnName) {
        if (gcmad == null || !gcmad.isStarted()) {
            throw new IllegalStateException("deployment is not started");
        }

        GCMVirtualNode vn = gcmad.getVirtualNode(vnName);
        return vn.getANode();
    }
}
