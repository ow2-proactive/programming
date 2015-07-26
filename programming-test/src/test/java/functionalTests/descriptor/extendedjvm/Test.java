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
package functionalTests.descriptor.extendedjvm;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;

import functionalTests.FunctionalTest;


/**
 * Jvm extension in deployment descriptor
 */
@Ignore
public class Test extends FunctionalTest {
    ProActiveDescriptor descriptor;
    A a1;
    A a2;
    A a3;

    @org.junit.Test
    public void action() throws Exception {
        VirtualNode vn1 = descriptor.getVirtualNode("evn1");
        VirtualNode vn2 = descriptor.getVirtualNode("evn2");
        VirtualNode vn3 = descriptor.getVirtualNode("evn3");
        a1 = PAActiveObject.newActive(A.class, new Object[] {}, vn1.getNode());
        a2 = PAActiveObject.newActive(A.class, new Object[] {}, vn2.getNode());
        a3 = PAActiveObject.newActive(A.class, new Object[] {}, vn3.getNode());

        assertTrue(a2.getTiti() == null);
        assertTrue(a2.getTata() != null);
        assertTrue(a3.getTiti() != null);
        assertTrue(a3.getToto() != null);

        assertTrue(a2.getClassPath().contains("ProActive.jar"));
        assertTrue(a2.getPolicy().contains("test"));
    }

    @Before
    public void initTest() throws Exception {
        String fileName = "JVMExtension";

        URL url = getClass().getResource("/functionalTests/descriptor/extendedjvm/" + fileName + ".xml");
        String oldFilePath = new File(url.toURI()).getAbsolutePath();
        String newFilePath = oldFilePath.replaceFirst(fileName + ".xml", fileName + "-tmp.xml");

        // if tests are run from the /compile directory : getParent for root directory 
        File userDir = new File(System.getProperty("user.dir"));
        String proactiveDir;
        if (userDir.getName().equals("compile")) {
            proactiveDir = userDir.getParent();
        } else {
            proactiveDir = userDir.getPath();
        }
        searchAndReplace(oldFilePath, newFilePath, "proactive.home", proactiveDir);
        descriptor = PADeployment.getProactiveDescriptor(
                getClass().getResource("/functionalTests/descriptor/extendedjvm/" + fileName + "-tmp.xml")
                        .getPath(), super.getVariableContract());
        descriptor.activateMappings();
    }

    @After
    public void endTest() throws Exception {
        descriptor.killall(false);
    }

    private void searchAndReplace(String oldFilePath, String newFilePath, String oldString, String newString) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(oldFilePath));
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFilePath));
            while (true) {
                String oldLine = reader.readLine();
                if (oldLine == null) {
                    break;
                }
                String newLine = oldLine.replace(oldString, newString);
                writer.write(newLine);
                writer.newLine();
            }
            reader.close();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.initTest();
            test.action();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
