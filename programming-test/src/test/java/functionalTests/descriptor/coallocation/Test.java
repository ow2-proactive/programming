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
package functionalTests.descriptor.coallocation;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualMachine;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;

import functionalTests.FunctionalTest;


/**
 * Test coallocation in deployment descriptors
 */
public class Test extends FunctionalTest {
    ProActiveDescriptor proActiveDescriptor;

    private static String AGENT_XML_LOCATION_UNIX = Test.class.getResource("/functionalTests/descriptor/coallocation/coallocation.xml")
                                                              .getPath();

    Node node1;

    Node node2;

    @org.junit.Test
    public void action() throws Exception {
        proActiveDescriptor = PADeployment.getProactiveDescriptor("file:" + AGENT_XML_LOCATION_UNIX,
                                                                  super.getVariableContract());
        // We activate the mapping in reverse order
        // when two vns refer to the same vm, the first vn which creates the vm becomes the creator of the vm
        // we want to verify this behavior (in addition to coallocation)
        proActiveDescriptor.activateMapping("covn2");
        proActiveDescriptor.activateMapping("covn1");
        VirtualNode vn1 = proActiveDescriptor.getVirtualNode("covn1");
        VirtualNode vn2 = proActiveDescriptor.getVirtualNode("covn2");
        node1 = vn1.getNode();
        node2 = vn2.getNode();

        vn1 = proActiveDescriptor.getVirtualNode("covn1");
        VirtualMachine vm = vn1.getVirtualNodeInternal().getVirtualMachine();
        assertTrue(node1.getProActiveRuntime().getURL().equals(node2.getProActiveRuntime().getURL()) &&
                   vm.getCreatorId().equals("covn2"));
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
