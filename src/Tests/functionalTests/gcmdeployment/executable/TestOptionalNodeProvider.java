/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.gcmdeployment.executable;

import java.io.File;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.GCMFunctionalTest;
import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestOptionalNodeProvider extends GCMFunctionalTest {

    String cookie = Long.valueOf(ProActiveRandom.nextLong()).toString();
    File tmpDir = new File(ProActiveConfiguration.getInstance().getProperty("java.io.tmpdir") +
        File.separator + this.getClass().getName() + cookie + File.separator);

    public TestOptionalNodeProvider() {
        super(AbstractTExecutable.class.getResource("TestOptionalNodeProvider.xml"));
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, "1",
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, "1",
                VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram("tmpDir", tmpDir.toString(),
                VariableContractType.DescriptorDefaultVariable);

        System.out.println("Temporary directory is: " + tmpDir.toString());
        Assert.assertTrue(tmpDir.mkdir());
    }

    @Test(timeout = 10000)
    public void test() {
        while (2 != tmpDir.listFiles().length) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @After
    public void after() {
        for (File file : tmpDir.listFiles()) {
            file.delete();
        }
        tmpDir.delete();
    }

}
