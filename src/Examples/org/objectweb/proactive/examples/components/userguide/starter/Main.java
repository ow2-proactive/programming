/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.components.userguide.starter;

import java.util.HashMap;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.Registry;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


public class Main {
    public static void main(String[] args) throws Exception {
        PAProperties.FRACTAL_PROVIDER.setValue("org.objectweb.proactive.core.component.Fractive");
        GCMApplication gcma = PAGCMDeployment
                .loadApplicationDescriptor(Main.class
                        .getResource("/org/objectweb/proactive/examples/components/userguide/starter/applicationDescriptor.xml"));
        gcma.startDeployment();

        Factory factory = FactoryFactory.getFactory();
        HashMap<String, GCMApplication> context = new HashMap<String, GCMApplication>(1);
        context.put("deployment-descriptor", gcma);

        // creates server component
        Component server = (Component) factory.newComponent(
                "org.objectweb.proactive.examples.components.userguide.starter.Server", context);

        // creates client component
        Component client = (Component) factory.newComponent(
                "org.objectweb.proactive.examples.components.userguide.starter.Client", context);

        // bind components
        BindingController bc = ((BindingController) client.getFcInterface("binding-controller"));
        bc.bindFc("s", server.getFcInterface("s"));

        // start components
        Fractal.getLifeCycleController(server).startFc();
        Fractal.getLifeCycleController(client).startFc();

        // launch the application
        ((Runnable) client.getFcInterface("m")).run();

        // stop components
        Fractal.getLifeCycleController(client).stopFc();
        Fractal.getLifeCycleController(server).stopFc();

        Registry.instance().clear();
        gcma.kill();
    }
}
