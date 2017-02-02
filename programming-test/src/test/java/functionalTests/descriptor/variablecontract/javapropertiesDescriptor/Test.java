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
package functionalTests.descriptor.variablecontract.javapropertiesDescriptor;

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
 * Tests conditions for variables of type JavaPropertiesDescriptor
 */
public class Test extends FunctionalTest {
    private static URL XML_LOCATION = Test.class.getResource("/functionalTests/descriptor/variablecontract/javapropertiesDescriptor/Test.xml");

    GCMApplication gcma;

    boolean bogusFromProgram;

    boolean bogusFromDescriptor;

    @Before
    public void initTest() throws Exception {
        bogusFromDescriptor = true;
        bogusFromProgram = true;
    }

    @org.junit.Test
    public void action() throws Exception {
        VariableContractImpl variableContract = new VariableContractImpl();

        //Setting from Program
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("user.home", "/home/userprogram");
        variableContract.setVariableFromProgram(map,
                                                VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
        variableContract.setVariableFromProgram("bogus.property",
                                                "",
                                                VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));

        assertTrue(variableContract.getValue("user.home").equals(System.getProperty("user.home")));

        //Setting from Descriptor
        variableContract.setDescriptorVariable("user.home",
                                               "/home/userdesc",
                                               VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
        assertTrue(variableContract.getValue("user.home").equals(System.getProperty("user.home")));

        //Setting bogus from program
        boolean bogus = false;
        try {
            variableContract.setDescriptorVariable("bogus.property",
                                                   "",
                                                   VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
            bogus = true; //shouldn't reach this line
        } catch (Exception e) {
        }
        assertTrue(!bogus);

        gcma = PAGCMDeployment.loadApplicationDescriptor(XML_LOCATION, variableContract);
        variableContract = (VariableContractImpl) gcma.getVariableContract();
        Assert.assertEquals(System.getProperty("user.home"), variableContract.getValue("user.home"));

        assertTrue("Variable Contract should be closed", variableContract.isClosed());
    }
}
