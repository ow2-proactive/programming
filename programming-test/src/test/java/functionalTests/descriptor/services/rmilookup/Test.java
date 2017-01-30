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
package functionalTests.descriptor.services.rmilookup;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractImpl;

import functionalTests.FunctionalTest;
import functionalTests.TestDisabler;


/**
 * Test service:  JVM acquisition with RMI in deployment descriptor
 *
 * @author The ProActive Team
 * @version 1.0 6 aout 2004
 * @since ProActive 2.0.1
 */
public class Test extends FunctionalTest {
    /*
     * Only work for RMI. There is no easy way to write this test for HTTP/PAMR
     */

    private static String ONEVM_XML_LOCATION_UNIX = Test.class.getResource("/functionalTests/descriptor/services/rmilookup/OneVM.xml")
                                                              .getPath();

    private static String LOOK_XML_LOCATION_UNIX = Test.class.getResource("/functionalTests/descriptor/services/rmilookup/LookupRMI.xml")
                                                             .getPath();

    Node node;

    ProActiveDescriptor pad;

    ProActiveDescriptor pad1;

    @Before
    final public void disable() {
        TestDisabler.supportedProtocols(Constants.RMI_PROTOCOL_IDENTIFIER, Constants.RMISSH_PROTOCOL_IDENTIFIER);
    }

    @org.junit.Test
    public void action() throws Exception {
        pad = PADeployment.getProactiveDescriptor(ONEVM_XML_LOCATION_UNIX,
                                                  (VariableContractImpl) super.getVariableContract().clone());
        pad.activateMappings();
        Thread.sleep(5000);
        pad1 = PADeployment.getProactiveDescriptor(LOOK_XML_LOCATION_UNIX,
                                                   (VariableContractImpl) super.getVariableContract().clone());
        pad1.activateMappings();
        VirtualNode vn = pad1.getVirtualNode("VnTest");
        node = vn.getNode();

        assertTrue(node.getProActiveRuntime().getVMInformation().getName().equals("PA_JVM1"));
    }

    @After
    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }
        if (pad1 != null) {
            pad1.killall(false);
        }
    }

    public static void main(String[] args) {
        Test test = new Test();

        try {
            test.action();
            test.endTest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
