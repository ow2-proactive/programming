/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.migration;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import functionalTests.FunctionalTest;
import functionalTests.GCMFunctionalTest;
import functionalTests.GCMFunctionalTestDefaultNodes;
import functionalTests.component.descriptor.fractaladl.Test;


// we need this active object to perform the test, because futures updates are involved (managed by future pool)
// therefore, future pool must be serialized, which poses a problem if there is a reference on a HalfBody (from main)
// solution : we run the test from an active object (no HalfBody involved)
public class DummyAO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    private GCMApplication newDeploymentDescriptor = null;

    public boolean goOldDeployment() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<String, ProActiveDescriptor> context = new HashMap<String, ProActiveDescriptor>();

        VariableContractImpl vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_JVM_PARAMETERS, FunctionalTest
                .getJvmParameters(), VariableContractType.ProgramVariable);

        ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(Test.class
                .getResource("/functionalTests/component/descriptor/deploymentDescriptor.xml").getPath(),
                vContract);
        context.put("deployment-descriptor", deploymentDescriptor);

        Component x = (Component) f.newComponent("functionalTests.component.migration.x", context);
        deploymentDescriptor.activateMappings();
        Fractal.getLifeCycleController(x).startFc();
        Fractive.getMigrationController(x).migrateTo(deploymentDescriptor.getVirtualNode("VN3").getNode());
        Assert.assertEquals("hello", ((E) x.getFcInterface("e")).gee(new StringWrapper("hello"))
                .stringValue());

        Component y = (Component) f.newComponent("functionalTests.component.migration.y", context);
        Fractive.getMigrationController(y).migrateTo(deploymentDescriptor.getVirtualNode("VN1").getNode());
        Fractal.getLifeCycleController(y).startFc();

        Component toto = (Component) f.newComponent("functionalTests.component.migration.toto", context);
        Fractive.getMigrationController(toto).migrateTo(deploymentDescriptor.getVirtualNode("VN2").getNode());
        Fractal.getLifeCycleController(toto).startFc();
        Assert.assertEquals("toto", ((E) toto.getFcInterface("e01")).gee(new StringWrapper("toto"))
                .stringValue());
        //        
        Component test = (Component) f.newComponent("functionalTests.component.migration.test", context);

        Fractal.getLifeCycleController(test).startFc();
        StringWrapper result = new StringWrapper("");
        //        for (int i = 0; i < 2; i++) {
        //            result = ((A) test.getFcInterface("a")).foo(new StringWrapper("hello world !"));
        //        }

        Component[] subComponents = Fractal.getContentController(test).getFcSubComponents();
        for (int i = 0; i < subComponents.length; i++) {
            NameController nc = Fractal.getNameController(subComponents[i]);
            if (nc.getFcName().equals("y")) {
                Fractive.getMigrationController(subComponents[i]).migrateTo(
                        deploymentDescriptor.getVirtualNode("VN3").getNode());
                break;
            }
        }

        // check singleton - gathercast - multicast interfaces
        //        for (int i = 0; i < 100; i++) {
        //            result = ((A) test.getFcInterface("a")).foo(new StringWrapper("hello world !"));
        //        }
        //        Assert.assertEquals("hello world !", result.stringValue());

        // check collection interfaces
        result = ((E) test.getFcInterface("e01")).gee(new StringWrapper("hello world !"));
        Assert.assertEquals("hello world !", result.stringValue());

        deploymentDescriptor.killall(false);

        return true;
    }

    public boolean goGCMDeployment() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<String, Object> context = new HashMap<String, Object>();

        URL descriptorPath = Test.class
                .getResource("/functionalTests/component/descriptor/applicationDescriptor.xml");

        VariableContractImpl vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(GCMFunctionalTest.VAR_OS, OperatingSystem.getOperatingSystem()
                .name(), VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, Integer.valueOf(4)
                .toString(), VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, Integer.valueOf(1)
                .toString(), VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_JVM_PARAMETERS, FunctionalTest
                .getJvmParameters(), VariableContractType.ProgramVariable);

        newDeploymentDescriptor = PAGCMDeployment.loadApplicationDescriptor(descriptorPath, vContract);

        newDeploymentDescriptor.startDeployment();

        context.put("deployment-descriptor", newDeploymentDescriptor);

        Component x = (Component) f.newComponent("functionalTests.component.migration.x", context);
        Fractal.getLifeCycleController(x).startFc();

        Fractive.getMigrationController(x)
                .migrateTo(newDeploymentDescriptor.getVirtualNode("VN3").getANode());
        Assert.assertEquals("hello", ((E) x.getFcInterface("e")).gee(new StringWrapper("hello"))
                .stringValue());

        Component y = (Component) f.newComponent("functionalTests.component.migration.y", context);
        Fractive.getMigrationController(y)
                .migrateTo(newDeploymentDescriptor.getVirtualNode("VN1").getANode());
        Fractal.getLifeCycleController(y).startFc();

        Component toto = (Component) f.newComponent("functionalTests.component.migration.toto", context);
        Fractive.getMigrationController(toto).migrateTo(
                newDeploymentDescriptor.getVirtualNode("VN2").getANode());
        Fractal.getLifeCycleController(toto).startFc();
        Assert.assertEquals("toto", ((E) toto.getFcInterface("e01")).gee(new StringWrapper("toto"))
                .stringValue());
        //        
        Component test = (Component) f.newComponent("functionalTests.component.migration.test", context);

        Fractal.getLifeCycleController(test).startFc();
        StringWrapper result = new StringWrapper("");
        //        for (int i = 0; i < 2; i++) {
        //            result = ((A) test.getFcInterface("a")).foo(new StringWrapper("hello world !"));
        //        }

        Component[] subComponents = Fractal.getContentController(test).getFcSubComponents();
        for (int i = 0; i < subComponents.length; i++) {
            NameController nc = Fractal.getNameController(subComponents[i]);
            if (nc.getFcName().equals("y")) {
                Fractive.getMigrationController(subComponents[i]).migrateTo(
                        newDeploymentDescriptor.getVirtualNode("VN3").getANode());
                break;
            }
        }

        // check singleton - gathercast - multicast interfaces
        //        for (int i = 0; i < 100; i++) {
        //            result = ((A) test.getFcInterface("a")).foo(new StringWrapper("hello world !"));
        //        }
        //        Assert.assertEquals("hello world !", result.stringValue());

        // check collection interfaces
        result = ((E) test.getFcInterface("e01")).gee(new StringWrapper("hello world !"));
        Assert.assertEquals("hello world !", result.stringValue());

        newDeploymentDescriptor.kill();

        return true;

    }

}
