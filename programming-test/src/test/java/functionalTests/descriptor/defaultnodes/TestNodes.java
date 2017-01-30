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
package functionalTests.descriptor.defaultnodes;

import java.io.File;
import java.net.URL;

import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;


/**
 * Test reading a descriptor file to create nodes needed by other tests.
 * @author The ProActive Team
 *
 */
public class TestNodes extends FunctionalTest {
    private static URL XML_LOCATION;

    static {
        String value = System.getProperty("functionalTests.descriptor.defaultnodes.file");
        if (value != null) {
            XML_LOCATION = TestNodes.class.getResource(value);
        } else {
            XML_LOCATION = TestNodes.class.getResource("/functionalTests/descriptor/defaultnodes/Nodes.xml");
        }
    }

    private static ProActiveDescriptor proActiveDescriptor = null;

    private static VirtualNode[] virtualNodes = null;

    private static Node sameVMNode = null;

    private static Node localVMNode = null;

    private static Node remoteVMNode = null;

    private static Node remoteACVMNode = null;

    private static String remoteHostname = "localhost";

    @Ignore
    @Test
    public void action() throws Exception {
        proActiveDescriptor = PADeployment.getProactiveDescriptor(new File(XML_LOCATION.toURI()).getAbsolutePath(),
                                                                  super.getVariableContract());
        proActiveDescriptor.activateMappings();
        TestNodes.virtualNodes = proActiveDescriptor.getVirtualNodes();
        for (int i = 0; i < virtualNodes.length; i++) {
            VirtualNode virtualNode = virtualNodes[i];
            if (virtualNode.getName().compareTo("Dispatcher") == 0) {
                sameVMNode = virtualNode.getNode();
            } else if (virtualNode.getName().compareTo("Dispatcher1") == 0) {
                localVMNode = virtualNode.getNode();
            } else if (virtualNode.getName().compareTo("Dispatcher3-AC") == 0) {
                remoteACVMNode = virtualNode.getNode();
            } else {
                remoteVMNode = virtualNode.getNode();
            }
        }
        remoteHostname = remoteVMNode.getVMInformation().getHostName();
    }

    /**
     * TODO : remove this method, as it is used as a walkaround to use deployed nodes
     * outside of the first packageGroup group of the testsuite.xml
     * (the endTest() method, that kills all the deployed nodes, is indeed called at the end of each
     * group of tests)
     *
     */

    public Object[] action(Object[] parameters) throws Exception {
        action();
        return null;
    }

    /**
     * @return
     */
    public static Node getLocalVMNode() {
        return localVMNode;
    }

    /**
     * @return
     */
    public static Node getRemoteVMNode() {
        return remoteVMNode;
    }

    /**
     * @return
     */
    public static Node getSameVMNode() {
        return sameVMNode;
    }

    /**
     * @return
     */
    public static String getRemoteHostname() {
        return remoteHostname;
    }

    /**
     * @return the node with automatic continuations enabled
     */
    public static Node getRemoteACVMNode() {
        return remoteACVMNode;
    }
}
