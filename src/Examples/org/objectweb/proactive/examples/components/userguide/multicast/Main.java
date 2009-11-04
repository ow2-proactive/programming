/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.components.userguide.multicast;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.component.adl.Launcher;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;


public class Main {
    private static String descriptor = "";

    public static void main(String[] args) {
        if (args.length == 1) {
            descriptor = args[0];
        } else {
            descriptor = Main.class.getResource("../deploymentDescriptorOld.xml").toString();
        }

        System.err.println("Launch multicast example");
        Main.manualLauncher();
        //Main.proactiveLauncher();
    }

    private static void proactiveLauncher() {
        System.err.println("Begin Launcher");
        String arg0 = "-fractal"; // using the fractal component model
        String arg1 = "org.objectweb.proactive.examples.components.userguide.multicast.adl.Launcher"; // which component definition to load
        String arg2 = "runnable";
        String arg3 = descriptor; // the deployment descriptor for proactive

        try {
            Launcher.main(new String[] { arg0, arg1, arg2, arg3 });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void manualLauncher() {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(descriptor);
            context.put("deployment-descriptor", deploymentDescriptor);
            deploymentDescriptor.activateMappings();

            Component launcher = null;
            launcher = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.multicast.adl.Launcher", context);
            if (launcher == null) {
                System.err.println("Component Launcher creation failed!");
                return;
            }

            Fractal.getLifeCycleController(launcher).startFc(); //root

            //     System.out.println("Components started!");
            ((java.lang.Runnable) launcher.getFcInterface("runnable")).run();
            Thread.sleep(10000);
            deploymentDescriptor.killall(false);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
