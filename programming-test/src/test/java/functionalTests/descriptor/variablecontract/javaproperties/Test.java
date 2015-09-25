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
    private static URL XML_LOCATION = Test.class
            .getResource("/functionalTests/descriptor/variablecontract/javaproperties/Test.xml");
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
            variableContract.setVariableFromProgram("bogus.property", "value",
                    VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG));
        } catch (Exception e) {
            bogusFromProgram = false;
        }

        //Setting from Descriptor
        variableContract.setDescriptorVariable("user.dir", "",
                VariableContractType.getType(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG));
        //Setting bogus from program
        try {
            variableContract.setDescriptorVariable("bogus.property", "value",
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
