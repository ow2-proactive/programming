/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package functionalTests.descriptor.variablecontract.javapropertiesProgram;

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
 * Tests conditions for variables of type JavaPropertiesProgram
 */
public class Test extends FunctionalTest {
    private static URL XML_LOCATION = Test.class
            .getResource("/functionalTests/descriptor/variablecontract/javapropertiesProgram/Test.xml");
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
        variableContract
                .setVariableFromProgram(map, VariableContractType
                        .getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
        assertTrue(variableContract.getValue("user.home").equals(System.getProperty("user.home")));

        boolean bogus = false;
        try {
            variableContract.setVariableFromProgram("bogus.property", "", VariableContractType
                    .getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
            bogus = true; //shouldn't reach this line
        } catch (Exception e) {
        }
        assertTrue(!bogus);

        variableContract
                .setVariableFromProgram("bogus.property", "bogus_value", VariableContractType
                        .getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
        assertTrue(variableContract.getValue("bogus.property").equals("bogus_value"));

        //Setting from Descriptor
        variableContract
                .setDescriptorVariable("user.home", "/home/userdesc", VariableContractType
                        .getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
        assertTrue(variableContract.getValue("user.home").equals(System.getProperty("user.home")));

        try {
            bogus = false;
            variableContract.setDescriptorVariable("${ilegal.var.name}", "ilegalvariablename",
                    VariableContractType
                            .getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
            bogus = true; //shouldn't reach this line
        } catch (Exception e) {
        }
        assertTrue(!bogus);

        //Setting bogus from program
        variableContract
                .setDescriptorVariable("bogus.property", "", VariableContractType
                        .getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG));
        Assert.assertEquals("bogus_value", variableContract.getValue("bogus.property"));

        gcma = PAGCMDeployment.loadApplicationDescriptor(XML_LOCATION, variableContract);
        variableContract = (VariableContractImpl) gcma.getVariableContract();

        variableContract.getValue("user.home").equals(System.getProperty("user.home"));

        //Empty value in descriptor should have less priority, and not set to empty
        assertTrue(variableContract.isClosed());
    }
}
