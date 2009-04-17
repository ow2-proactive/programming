/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2005 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.component.collectiveitf.gathercast_remote;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import functionalTests.ComponentTest;
import functionalTests.GCMFunctionalTest;
import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestRemoteGathercast extends ComponentTest {

    private GCMApplication newDeploymentDescriptor = null;
    private ProActiveDescriptor oldDeploymentDescriptor = null;

    @org.junit.Test
    public void testRemoteGathercastPADeployement() throws Exception {
        oldDeploymentDescriptor = PADeployment.getProactiveDescriptor(TestRemoteGathercast.class.getResource(
                "/functionalTests/component/descriptor/deploymentDescriptor.xml").getPath(),
                (VariableContractImpl) super.vContract.clone());

        useRemoteGathercastItf(oldDeploymentDescriptor);
    }

    @org.junit.Test
    public void testRemoteGathercastGCMDeployement() throws Exception {

        URL descriptorPath = TestRemoteGathercast.class
                .getResource("/functionalTests/component/descriptor/applicationDescriptor.xml");

        vContract.setVariableFromProgram(GCMFunctionalTest.VAR_OS, OperatingSystem.getOperatingSystem()
                .name(), VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_HOSTCAPACITY, Integer.valueOf(4)
                .toString(), VariableContractType.DescriptorDefaultVariable);
        vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_VMCAPACITY, Integer.valueOf(1)
                .toString(), VariableContractType.DescriptorDefaultVariable);

        newDeploymentDescriptor = PAGCMDeployment.loadApplicationDescriptor(descriptorPath,
                (VariableContractImpl) super.vContract.clone());

        newDeploymentDescriptor.startDeployment();

        useRemoteGathercastItf(newDeploymentDescriptor);
    }

    private void useRemoteGathercastItf(Object deploymentDesc) throws Exception {
             Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            context.put("deployment-descriptor", deploymentDesc);
            Component gatherCmpServer, gatherCmpClient;

            // instantiate components
            System.out.println("\nInstantiate components...");
            gatherCmpClient = (Component) f.newComponent(
                    "functionalTests.component.collectiveitf.gathercast_remote.GatherCmp", context);
            gatherCmpServer = (Component) f.newComponent(
                    "functionalTests.component.collectiveitf.gathercast_remote.GatherCmp", context);

            // bind components
            System.out.println("\nBind components...");

            // CgeneratedCCgeneratedfunctionalTestsCCPcomponentCCPcollectiveitfCCPgathercast_remoteCCPGatherItfCCOreceiverCCgathercastItfProxyCOreceiverCrepresentative
            //     TestRemoteGathercast.class.getClassLoader().loadClass("CgeneratedCCgeneratedorgCCPobjectwebCCPproactiveCCPexamplesCCPcomponentsCCPjacobiCCPGathercastDataReceiverCCOreceiverCCgathercastItfProxyCOreceiverCrepresentative");
            BindingController bc = Fractal.getBindingController(gatherCmpClient);

            // binding
            bc.bindFc("sender", gatherCmpServer.getFcInterface("receiver"));

            // start and launch components
            System.out.println("\nLaunch components...");
            ((Runnable) gatherCmpClient.getFcInterface("main")).run();

            System.out.println("\nStart components...");
            Fractal.getLifeCycleController(gatherCmpClient).startFc();
            Fractal.getLifeCycleController(gatherCmpServer).startFc();
    }

    @After
    public void endTest() throws Exception {
        Registry.instance().clear();
        if (newDeploymentDescriptor != null) {
            newDeploymentDescriptor.kill();
        }

        if (oldDeploymentDescriptor != null) {
            oldDeploymentDescriptor.killall(false);
        }
    }

}
