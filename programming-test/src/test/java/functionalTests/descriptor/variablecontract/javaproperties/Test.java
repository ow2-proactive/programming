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
package functionalTests.descriptor.variablecontract.javaproperties;

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
 * Tests conditions for variables of type JavaProperties
 */
public class Test extends FunctionalTest {
    private static URL XML_LOCATION = Test.class.getResource("/functionalTests/descriptor/variablecontract/javaproperties/Test.xml");

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
        map.put("user.home", "");
        variableContract.setVariableFromProgram(map,
                                                VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG));

        //Setting Bogus from program
        try {
            variableContract.setVariableFromProgram("bogus.property",
                                                    "value",
                                                    VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG));
        } catch (Exception e) {
            bogusFromProgram = false;
        }

        //Setting from Descriptor
        variableContract.setDescriptorVariable("user.dir",
                                               "",
                                               VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG));
        //Setting bogus from program
        try {
            variableContract.setDescriptorVariable("bogus.property",
                                                   "value",
                                                   VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG));
        } catch (Exception e) {
            bogusFromDescriptor = false;
        }

        gcma = PAGCMDeployment.loadApplicationDescriptor(XML_LOCATION, variableContract);

        variableContract = (VariableContractImpl) gcma.getVariableContract();
        //System.out.println(variableContract);
        Assert.assertFalse(bogusFromProgram);
        Assert.assertFalse(bogusFromDescriptor);
        Assert.assertEquals(System.getProperty("user.home"), variableContract.getValue("user.home"));
        Assert.assertEquals(System.getProperty("user.dir"), variableContract.getValue("user.dir"));
        Assert.assertEquals(System.getProperty("user.name"), variableContract.getValue("user.name"));
        Assert.assertTrue(variableContract.isClosed());
    }
}
