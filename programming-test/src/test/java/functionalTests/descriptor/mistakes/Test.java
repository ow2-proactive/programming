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
package functionalTests.descriptor.mistakes;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.xml.VariableContractImpl;

import functionalTests.FunctionalTest;


/**
 * Test service: variable support and mistakes in deployment descriptor
 */
public class Test extends FunctionalTest {
    private static String TESTMISTAKES_XML_LOCATION_UNIX = Test.class.getResource("/functionalTests/descriptor/mistakes/testMistakes.xml")
                                                                     .getPath();

    private static String TESTVARIABLES_XML_LOCATION_UNIX = Test.class.getResource("/functionalTests/descriptor/mistakes/testVariables.xml")
                                                                      .getPath();

    ProActiveDescriptor pad;

    ProActiveDescriptor pad1;

    boolean testSuccess = true;

    @org.junit.Test
    public void action() throws Exception {
        // We try to parse an XML Deployment Descriptor with mistakes, an
        // exception must be thrown
        try {
            pad = PADeployment.getProactiveDescriptor(TESTMISTAKES_XML_LOCATION_UNIX);
            testSuccess = false;
        } catch (Exception e) {
            // Mistake found as expected
            //            super.getLogger()
            //                 .debug("Message found as expected\n" + e.getMessage());
        }

        if (pad != null) {
            pad.killall(false);
        }

        assertTrue(testSuccess);

        // We now parse an XML Deployment Descriptor with variables
        // The preceding test resulted in an error during the parsing, if you
        // encounter an endless loop here,
        // it means that the lock of the Variable Contract was not properly
        // released.
        VariableContractImpl variableContract = new VariableContractImpl();
        try {
            pad1 = PADeployment.getProactiveDescriptor(TESTVARIABLES_XML_LOCATION_UNIX, variableContract);
            // Descriptor parsed witout mistakes
        } catch (Exception e) {
            // Mistake found but not expected
            testSuccess = false;
            throw e;
        }

        if (pad1 != null) {
            pad1.killall(false);
        }
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
