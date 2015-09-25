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
package functionalTests.descriptor.variablecontract.externalfiles;

import java.io.File;
import java.net.URL;

import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import functionalTests.FunctionalTest;
import org.junit.Before;

import static org.junit.Assert.assertTrue;


/**
 * Tests conditions for external files
 */
public class Test extends FunctionalTest {
    private static URL XML_LOCATION = Test.class
            .getResource("/functionalTests/descriptor/variablecontract/externalfiles/Test.xml");
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
        URL url = Test.class
                .getResource("/functionalTests/descriptor/variablecontract/externalfiles/Test.properties");
        String path = new File(url.toURI()).getAbsolutePath();
        variableContract.setVariableFromProgram("RPATH", path, VariableContractType.ProgramVariable);

        /*
         * //Setting from Program HashMap map = new HashMap(); map.put("test_var1", "value1");
         * variableContract.setVariableFromProgram(map,
         * XMLPropertiesType.getType("ProgramDefaultVariable"));
         * 
         * //Setting bogus from Program (this should fail) try{
         * variableContract.setVariableFromProgram("test_empty", "",
         * XMLPropertiesType.getType("ProgramDefaultVariable")); }catch (Exception e){
         * bogusFromProgram=false; }
         * 
         * //Setting from Program variableContract.setDescriptorVariable("test_var2", "value2a",
         * XMLPropertiesType.getType("ProgramDefaultVariable")); //The following value should not be
         * set, because Program is default and therefore has lower priority
         * variableContract.setVariableFromProgram("test_var2", "value2b",
         * XMLPropertiesType.getType("ProgramDefaultVariable"));
         * 
         * //Setting bogus variable from Descriptor (this should fail) try{
         * variableContract.setDescriptorVariable("bogus_from_descriptor", "",
         * XMLPropertiesType.getType("ProgramDefaultVariable")); }catch (Exception e){
         * bogusFromDescriptor=false; }
         * 
         * //test_var3=value3
         */
        gcma = PAGCMDeployment.loadApplicationDescriptor(XML_LOCATION, variableContract);
        variableContract = (VariableContractImpl) gcma.getVariableContract();

        //System.out.println(variableContract);
        assertTrue(variableContract.getValue("test_var0").equals("value0"));
        assertTrue(variableContract.getValue("test_var1").equals("value1"));
        assertTrue(variableContract.getValue("test_var2").equals("value2"));
        assertTrue(variableContract.getValue("test_var3").equals("value3"));

        // these are variable from the xml file, which we no longer support
        //
        //        assertTrue(variableContract.getValue("test_var4").equals("value4"));
        //        assertTrue(variableContract.getValue("test_var5").equals("value5"));
        //        assertTrue(variableContract.getValue("test_var6").equals("value6"));
        assertTrue(variableContract.isClosed());
        assertTrue(variableContract.checkContract());
    }
}
