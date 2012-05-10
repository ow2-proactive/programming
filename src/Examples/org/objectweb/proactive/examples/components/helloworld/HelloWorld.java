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
package org.objectweb.proactive.examples.components.helloworld;

/***
 *
 * Author: Eric Bruneton
 * Modified by: The ProActive Team
 */
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.Launcher;


/**
 * This example is a mix from the helloworld examples in the Fractal distribution : the example from Julia, and the one from the FractalADL.<br>
 * The differences are the following : <br>
 *     - from FractalADL : <br>
 *             * this one uses a custom parser, based on the standard FractalADL, but it is able to add cardinality to virtual nodes and
 * allows the composition of virtual nodes.<br>
 *             * there are 4 .fractal files corresponding to definitions of the system in the current vm, in distributed vms (this uses
 * the ProActive deployment capabilities), a version with wrapping composite components and a version without wrapping components.
 *
 * Use the "parser" parameter to make it work.<br>
 *     - from Julia :
 *
 *
 * Sections involving templates have been removed, because this implementation does not provide templates. <br>
 * A functionality offered by ProActive is the automatic deployment of components onto remote locations.<br>
 * TODO change comment
 * When using the "distributed" option with the "parser" option, the ADL loader will load the "helloworld-distributed.xml" ADL,
 * which affects virtual nodes to components, and the "deployment.xml" file, which maps the virtual nodes to real nodes.<br>
 * If other cases, all components are instantiated locally, in the current virtual machine. <br>
 *
 *
 */
public class HelloWorld {
    public static void main(final String[] args) throws Exception {
        boolean useParser = false;
        boolean useTemplates = false;
        boolean useWrapper = false;
        boolean distributed = false;

        for (int i = 0; i < args.length; ++i) {
            useParser |= args[i].equals("parser");
            useTemplates |= args[i].equals("templates");
            useWrapper |= args[i].equals("wrapper");
            distributed |= args[i].equals("distributed");
        }

        if (useParser) {
            //      // -------------------------------------------------------------------
            //      // OPTION 1 : USE THE (custom) FRACTAL ADL
            //      // -------------------------------------------------------------------
            String arg0 = "-fractal"; // using the fractal component model
            String arg1; // which component definition to load
            String arg2 = "r";
            String arg3 = HelloWorld.class.getResource(
                    "/org/objectweb/proactive/examples/components/helloworld/deployment.xml").toString(); // the deployment descriptor for proactive

            if (distributed) {
                if (useWrapper) {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-distributed-wrappers";
                } else {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-distributed-no-wrappers";
                }
            } else {
                if (useWrapper) {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-local-wrappers";
                } else {
                    arg1 = "org.objectweb.proactive.examples.components.helloworld.helloworld-local-no-wrappers";
                }
            }
            Launcher.main(new String[] { arg0, arg1, arg2, arg3 });
        } else {
            // -------------------------------------------------------------------
            // OPTION 2 : DO NOT USE THE FRACTAL ADL
            // -------------------------------------------------------------------
            Component boot = Utils.getBootstrapComponent();
            GCMTypeFactory tf = GCM.getGCMTypeFactory(boot);
            Component rComp = null;

            // type of root component
            ComponentType rType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("r",
                    Runnable.class.getName(), false, false, false) });

            // type of client component
            ComponentType cType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("r", Runnable.class.getName(), false, false, false),
                    tf.createFcItfType("s", Service.class.getName(), true, false, false) });

            // type of server component
            ComponentType sType = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("s", Service.class.getName(), false, false, false),
                    tf.createFcItfType(Constants.ATTRIBUTE_CONTROLLER, ServiceAttributes.class.getName(),
                            false, false, false) });

            GenericFactory cf = GCM.getGenericFactory(boot);

            if (!useTemplates) {
                // -------------------------------------------------------------------
                // OPTION 2.1 : CREATE COMPONENTS DIRECTLY
                // -------------------------------------------------------------------
                // create root component
                rComp = cf.newFcInstance(rType, new ControllerDescription("root", Constants.COMPOSITE), null);
                // create client component
                Component cComp = cf.newFcInstance(cType, new ControllerDescription("client",
                    Constants.PRIMITIVE), new ContentDescription(ClientImpl.class.getName())); // other properties could be added (activity for example)

                // create server component
                Component sComp = cf.newFcInstance(sType, new ControllerDescription("server",
                    Constants.PRIMITIVE), new ContentDescription(ServerImpl.class.getName()));

                ((ServiceAttributes) GCM.getAttributeController(sComp)).setHeader("--------> ");
                ((ServiceAttributes) GCM.getAttributeController(sComp)).setCount(1);

                if (useWrapper) {
                    sType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("s", Service.class
                            .getName(), false, false, false) });
                    // create client component "wrapper" component
                    Component CComp = cf.newFcInstance(cType, new ControllerDescription("client-wrapper",
                        Constants.COMPOSITE), null);

                    // create server component "wrapper" component
                    Component SComp = cf.newFcInstance(sType, new ControllerDescription("server-wrapper",
                        Constants.COMPOSITE), null);

                    // component assembly
                    GCM.getContentController(CComp).addFcSubComponent(cComp);
                    GCM.getContentController(SComp).addFcSubComponent(sComp);
                    GCM.getBindingController(CComp).bindFc("r", cComp.getFcInterface("r"));
                    GCM.getBindingController(cComp).bindFc("s",
                            GCM.getContentController(CComp).getFcInternalInterface("s"));
                    //GCM.getBindingController(cComp).bindFc("s", CComp.getFcInterface("s"));
                    GCM.getBindingController(SComp).bindFc("s", sComp.getFcInterface("s"));
                    // replaces client and server components by "wrapper" components
                    // THIS CHANGES REFERENCES (STUBS)
                    cComp = CComp;
                    sComp = SComp;
                }

                // component assembly
                GCM.getContentController(rComp).addFcSubComponent(cComp);
                GCM.getContentController(rComp).addFcSubComponent(sComp);
                GCM.getBindingController(rComp).bindFc("r", cComp.getFcInterface("r"));
                GCM.getBindingController(cComp).bindFc("s", sComp.getFcInterface("s"));
            }

            // start root component
            GCM.getGCMLifeCycleController(rComp).startFc();

            // call main method
            ((Runnable) rComp.getFcInterface("r")).run();
        }
        PALifeCycle.exitSuccess();
    }
}
