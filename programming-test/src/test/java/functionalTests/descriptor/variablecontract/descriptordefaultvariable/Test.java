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
package functionalTests.descriptor.variablecontract.descriptordefaultvariable;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorConstants;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import functionalTests.FunctionalTest;


/**
 * Tests conditions for variables of type DescriptorDefaultVariable
 */
public class Test extends FunctionalTest {
    private static URL XML_LOCATION = Test.class.getResource("/functionalTests/descriptor/variablecontract/descriptordefaultvariable/Test.xml");

    GCMApplication gcma;

    boolean bogusFromDescriptor;

    boolean bogusFromProgram;

    @Before
    public void initTest() throws Exception {
        bogusFromDescriptor = true;
        bogusFromProgram = true;
    }

    @org.junit.Test
    public void action() throws Exception {
        VariableContractImpl variableContract = new VariableContractImpl();

        //Setting from Descriptor
        variableContract.setDescriptorVariable("test_var1",
                                               "value1",
                                               VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));

        //Setting bogus from descriptor (this should fail)
        try {
            variableContract.setDescriptorVariable("test_empty",
                                                   "",
                                                   VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        } catch (Exception e) {
            bogusFromDescriptor = false;
        }

        //Setting from Program
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("test_var2", "value2a");
        variableContract.setVariableFromProgram(map,
                                                VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        //The following value should not be set, because Descriptor is default and therefore has lower priority
        variableContract.setDescriptorVariable("test_var2",
                                               "value2b",
                                               VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));

        //Setting bogus variable from Program (this should fail)
        try {
            variableContract.setVariableFromProgram("bogus_from_program",
                                                    "",
                                                    VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        } catch (Exception e) {
            bogusFromProgram = false;
        }

        //test_var3=value3
        gcma = PAGCMDeployment.loadApplicationDescriptor(XML_LOCATION, variableContract);

        variableContract = (VariableContractImpl) gcma.getVariableContract();

        //System.out.println(variableContract);
        Assert.assertFalse(bogusFromDescriptor);
        Assert.assertFalse(bogusFromProgram);
        Assert.assertEquals("value1", variableContract.getValue("test_var1"));
        Assert.assertEquals("value2a", variableContract.getValue("test_var2"));
        Assert.assertEquals("value3", variableContract.getValue("test_var3"));
        assertTrue(variableContract.isClosed());
        assertTrue(variableContract.checkContract());
    }
}
