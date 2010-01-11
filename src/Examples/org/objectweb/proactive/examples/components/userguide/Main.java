/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
//@snippet-start full-main
package org.objectweb.proactive.examples.components.userguide;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.components.userguide.primitive.ComputeItf;
import org.objectweb.proactive.examples.components.userguide.primitive.PrimitiveComputer;
import org.objectweb.proactive.examples.components.userguide.primitive.PrimitiveMaster;


public class Main {
    public static void main(String[] args) {
        //        System.out.println("Launch primitive component example");
        //        Main.launchFirstPrimitive();
        System.out.println("Launch component assembly example");
        Main.launchWithoutADL();

        //        System.out.println("Launch and deploy component assembly example");
        //        Main.launchAndDeployWithoutADL();

        //        System.out.println("Launch component assembly example with ADL");
        //        Main.launchOneWithADL();
        //
        //        System.out.println("Launch and deploy component assembly example with ADL");
        //        Main.launchAndDeployWithADL();

        //System.err.println("The END...");
        //System.exit(0);
    }

    //@snippet-end full-main

    //@snippet-start launch_first_primitive
    private static void launchFirstPrimitive() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory typeFact = Fractal.getTypeFactory(boot);
            GenericFactory genericFact = Fractal.getGenericFactory(boot);
            Component primitiveComputer = null;

            // type of PrimitiveComputer component
            ComponentType computerType = typeFact.createFcType(new InterfaceType[] { typeFact
                    .createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });

            // component creation
            primitiveComputer = genericFact.newFcInstance(computerType, new ControllerDescription("root",
                Constants.PRIMITIVE), new ContentDescription(PrimitiveComputer.class.getName()));

            // start PrimitiveComputer component
            Fractal.getLifeCycleController(primitiveComputer).startFc();
            ((LifeCycleController) primitiveComputer.getFcInterface("lifecycle-controller")).startFc();

            // get the compute-itf interface
            ComputeItf itf = ((ComputeItf) primitiveComputer.getFcInterface("compute-itf"));
            ;
            // call component
            itf.doNothing();
            int result = itf.compute(5);

            System.out.println("Result of computation whith 5 is: " + result); //display 10
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@snippet-end launch_first_primitive
    //@snippet-start launch_without_ADL
    private static void launchWithoutADL() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory typeFact = Fractal.getTypeFactory(boot);
            GenericFactory genericFact = Fractal.getGenericFactory(boot);

            // component types: PrimitiveComputer, PrimitiveMaster, CompositeWrapper
            ComponentType computerType = typeFact.createFcType(new InterfaceType[] { typeFact
                    .createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType masterType = typeFact.createFcType(new InterfaceType[] {
                    typeFact.createFcItfType("run", Runnable.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    typeFact.createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.CLIENT,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType wrapperType = typeFact.createFcType(new InterfaceType[] { typeFact.createFcItfType(
                    "run", Runnable.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE) });

            // components creation
            Component primitiveComputer = genericFact.newFcInstance(computerType, new ControllerDescription(
                "PrimitiveComputer", Constants.PRIMITIVE), new ContentDescription(PrimitiveComputer.class
                    .getName()));
            Component primitiveMaster = genericFact.newFcInstance(masterType, new ControllerDescription(
                "PrimitiveMaster", Constants.PRIMITIVE), new ContentDescription(PrimitiveMaster.class
                    .getName()));
            Component compositeWrapper = genericFact.newFcInstance(wrapperType, new ControllerDescription(
                "CompositeWrapper", Constants.COMPOSITE), null);

            // component assembling
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveComputer);
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveMaster);
            Fractal.getBindingController(compositeWrapper).bindFc("run",
                    primitiveMaster.getFcInterface("run"));
            Fractal.getBindingController(primitiveMaster).bindFc("compute-itf",
                    primitiveComputer.getFcInterface("compute-itf"));

            // start CompositeWrapper component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            itf.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@snippet-end launch_without_ADL
    private static void launchAndDeployWithoutADL() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory typeFact = Fractal.getTypeFactory(boot);
            ProActiveGenericFactory genericFact = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);

            ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(Main.class
                    .getResource("deploymentDescriptor.xml").getPath());
            deploymentDescriptor.activateMappings();
            VirtualNode vnode = deploymentDescriptor.getVirtualNode("primitive-node");
            vnode.activate();
            Node node1 = vnode.getNode();

            // component types: PrimitiveComputer, PrimitiveMaster, CompositeWrapper
            ComponentType computerType = typeFact.createFcType(new InterfaceType[] { typeFact
                    .createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType masterType = typeFact.createFcType(new InterfaceType[] {
                    typeFact.createFcItfType("run", Runnable.class.getName(), TypeFactory.SERVER,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE),
                    typeFact.createFcItfType("compute-itf", ComputeItf.class.getName(), TypeFactory.CLIENT,
                            TypeFactory.MANDATORY, TypeFactory.SINGLE) });
            ComponentType wrapperType = typeFact.createFcType(new InterfaceType[] { typeFact.createFcItfType(
                    "run", Runnable.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE) });

            // components creation
            Component primitiveComputer = genericFact.newFcInstance(computerType, new ControllerDescription(
                "PrimitiveComputer", Constants.PRIMITIVE), new ContentDescription(PrimitiveComputer.class
                    .getName()), node1);
            Component primitiveMaster = genericFact.newFcInstance(masterType, new ControllerDescription(
                "PrimitiveMaster", Constants.PRIMITIVE), new ContentDescription(PrimitiveMaster.class
                    .getName()));
            Component compositeWrapper = genericFact.newFcInstance(wrapperType, new ControllerDescription(
                "CompositeWrapper", Constants.COMPOSITE), null);

            // component assembling
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveComputer);
            Fractal.getContentController(compositeWrapper).addFcSubComponent(primitiveMaster);
            Fractal.getBindingController(compositeWrapper).bindFc("run",
                    primitiveMaster.getFcInterface("run"));
            Fractal.getBindingController(primitiveMaster).bindFc("compute-itf",
                    primitiveComputer.getFcInterface("compute-itf"));

            // start CompositeWrapper component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            while (true) {
                Thread.sleep(1000);
                itf.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@snippet-start launch_with_ADL
    private static void launchWithADL() {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            // component creation
            Component compositeWrapper = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.adl.CompositeWrapper", context);

            // start PrimitiveComputer component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            itf.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@snippet-end launch_with_ADL
    private static void launchOneWithADL() {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            // component creation
            Component compositeWrapper = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.adl.PrimitiveComputer", context);

            // start PrimitiveComputer component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the run interface
            ComputeItf itf = ((ComputeItf) compositeWrapper.getFcInterface("compute-itf"));

            // call component
            System.out.println("Result compute: " + itf.compute(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@snippet-start launch_and_deploy_with_ADL
    private static void launchAndDeployWithADL() {
        try {
            // get the component Factory allowing component creation from ADL
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<String, Object> context = new HashMap<String, Object>();

            // retrieve the deployment descriptor
            ProActiveDescriptor deploymentDescriptor = PADeployment.getProactiveDescriptor(Main.class
                    .getResource("deploymentDescriptor.xml").getPath());
            context.put("deployment-descriptor", deploymentDescriptor);
            deploymentDescriptor.activateMappings();

            // component creation
            Component compositeWrapper = (Component) f.newComponent(
                    "org.objectweb.proactive.examples.components.userguide.adl.CompositeWrapper", context);

            // start PrimitiveComputer component
            Fractal.getLifeCycleController(compositeWrapper).startFc();

            // get the compute-itf interface
            Runnable itf = ((Runnable) compositeWrapper.getFcInterface("run"));

            // call component
            itf.run();

            Thread.sleep(1000);
            // wait for the end of execution 
            // and kill JVM created with the deployment descriptor
            deploymentDescriptor.killall(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //@snippet-end launch_and_deploy_with_ADL
}
