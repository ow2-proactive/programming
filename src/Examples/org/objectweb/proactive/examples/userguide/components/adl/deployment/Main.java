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
//@snippet-start adl_deployment_Main_skeleton
//@snippet-start adl_deployment_Main
//@tutorial-start
package org.objectweb.proactive.examples.userguide.components.adl.deployment;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * @author The ProActive Team
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // TODO: Load the Application Descriptor
        //@tutorial-break
        //@snippet-break adl_deployment_Main_skeleton
        String descriptorPath = "file://" +
            System.getProperty("proactive.home") +
            "/src/Examples/org/objectweb/proactive/examples/userguide/components/adl/deployment/descriptors/application_descriptor.xml";
        // This tricky line enables to use this path in both Linux and Windows OS
        File deploymentFile = new File((new URL(descriptorPath)).toURI().getPath());
        GCMApplication gcma = PAGCMDeployment.loadApplicationDescriptor(deploymentFile);
        //@snippet-resume adl_deployment_Main_skeleton
        //@tutorial-resume

        // TODO: Start the deployment
        //@tutorial-break
        //@snippet-break adl_deployment_Main_skeleton
        gcma.startDeployment();
        gcma.waitReady();
        GCMVirtualNode vn = gcma.getVirtualNode("slave-node");
        vn.waitReady();
        //@snippet-resume adl_deployment_Main_skeleton
        //@tutorial-resume

        Factory factory = FactoryFactory.getFactory();

        Map<String, Object> context = new HashMap<String, Object>();

        // TODO: Put the Application Descriptor in the context
        //@tutorial-break
        //@snippet-break adl_deployment_Main_skeleton
        context.put("deployment-descriptor", gcma);
        //@snippet-resume adl_deployment_Main_skeleton
        //@tutorial-resume

        Component composite = (Component) factory
                .newComponent(
                        "org.objectweb.proactive.examples.userguide.components.adl.deployment.adl.Composite",
                        context);

        GCM.getGCMLifeCycleController(composite).startFc();

        Runner runner = (Runner) composite.getFcInterface("runner");
        List<String> arg = new ArrayList<String>();
        arg.add("hello");
        arg.add("world");
        runner.run(arg);

        GCM.getGCMLifeCycleController(composite).stopFc();

        System.exit(0);
    }
}
//@snippet-end adl_deployment_Main_skeleton
//@snippet-end adl_deployment_Main
//@tutorial-end
