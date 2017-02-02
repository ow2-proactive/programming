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
package functionalTests.gcmdeployment.descriptorurl;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import functionalTests.FunctionalTest;


public class TestGCMDescriptorURL extends FunctionalTest {
    GCMApplication gcma;

    GCMApplication gcma2;

    GCMApplication gcma3;

    GCMApplication gcma4;

    //    GCMApplication gcma3;

    @Test
    public void test()
            throws ProActiveException, FileNotFoundException, MalformedURLException, CloneNotSupportedException {
        /**** Testing classical File url ****/
        URL descriptor1 = getClass().getResource("application/TestVirtualNodeRelative.xml");
        System.out.println("Using descriptor at URL :");
        System.out.println(descriptor1);

        /**** Testing Jar url Relative ****/
        URL jarfileurl = getClass().getResource("descriptors.jar");
        URL descriptor2 = new URL("jar:" + jarfileurl.toExternalForm() + "!/application/TestVirtualNodeRelative.xml");
        System.out.println("Using descriptor at URL :");
        System.out.println(descriptor2);

        /**** Testing HTTP+Jar URL !!! ***/
        //        URL jarfileurl2 = new URL("http://proactive.inria.fr/userfiles/file/apps/descriptors.jar");
        //        URL descriptor3 = new URL("jar:"+jarfileurl2.toExternalForm()+"!/application/TestVirtualNodeRelative.xml");
        //        System.out.println(descriptor3);
        gcma = PAGCMDeployment.loadApplicationDescriptor(descriptor1,
                                                         (VariableContractImpl) super.getVariableContract().clone());
        gcma2 = PAGCMDeployment.loadApplicationDescriptor(descriptor2,
                                                          (VariableContractImpl) super.getVariableContract().clone());
        //        gcma3 = PAGCMDeployment.loadApplicationDescriptor(descriptor3);
        Assert.assertFalse(gcma.isStarted());
        Assert.assertEquals(1, gcma.getVirtualNodes().size());

        Assert.assertFalse(gcma2.isStarted());
        Assert.assertEquals(1, gcma2.getVirtualNodes().size());

        URL descriptor3 = getClass().getResource("application/TestVirtualNodeWindowsPath.xml");
        System.out.println("Using descriptor at URL :");
        System.out.println(descriptor3);

        try {
            gcma3 = PAGCMDeployment.loadApplicationDescriptor(descriptor3,
                                                              (VariableContractImpl) super.getVariableContract().clone());
        } catch (ProActiveException ex) {
            // on linux a proactive exception here is normal as the windows path points to nowhere but not an IOException saying there is an internal error
            // but on windows
            if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {
                throw ex;
            }
        }

        /**** Testing absolute deployment File ref ****/
        URL descriptor4 = getClass().getResource("application/TestVirtualNodeAbsolute.xml");
        System.out.println("Using descriptor at URL :");
        System.out.println(descriptor4);
        gcma4 = PAGCMDeployment.loadApplicationDescriptor(descriptor4,
                                                          (VariableContractImpl) super.getVariableContract().clone());
        /**** Testing absolute deployment File ref 2 ****/
        URL descriptor5 = getClass().getResource("application/TestVirtualNodeAbsolute2.xml");
        System.out.println("Using descriptor at URL :");
        System.out.println(descriptor5);
        gcma4 = PAGCMDeployment.loadApplicationDescriptor(descriptor5,
                                                          (VariableContractImpl) super.getVariableContract().clone());

    }
}
