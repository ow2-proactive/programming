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
package functionalTests.component.nonfunctional.membranecontroller.bindnfc.objectcontrollers;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import functionalTests.ComponentTest;
import functionalTests.component.creation.ComponentA;
import functionalTests.component.creation.ComponentInfo;


/**
 * @author The ProActive Team
 *
 * Experimenting with non-functional type and controller objects
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;

    public Test() {
        super("Setting object controllers as an implementation of non-functional interfaces",
                "Test setControllerObject method of the MembraneController");
    }

    @org.junit.Test
    public void action() throws Exception {
        //Thread.sleep(2000);
        Component boot = Utils.getBootstrapComponent(); /*Getting the Fractal-Proactive bootstrap component*/
        PAGCMTypeFactory type_factory = Utils.getPAGCMTypeFactory(boot); /*Getting the GCM-ProActive type factory*/
        PAGenericFactory cf = Utils.getPAGenericFactory(boot); /*Getting the GCM-ProActive generic factory*/

        InterfaceType[] fItfTypes = new InterfaceType[] { type_factory.createFcItfType("componentInfo",
                ComponentInfo.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), };

        InterfaceType[] nfItfTypes = new InterfaceType[] {
                type_factory
                        .createFcItfType(
                                Constants.BINDING_CONTROLLER,
                                /* BINDING CONTROLLER */org.objectweb.proactive.core.component.control.PABindingController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory
                        .createFcItfType(
                                Constants.CONTENT_CONTROLLER,
                                /* CONTENT CONTROLLER */org.objectweb.proactive.core.component.control.PAContentController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory
                        .createFcItfType(
                                Constants.LIFECYCLE_CONTROLLER,
                                /* LIFECYCLE CONTROLLER */org.objectweb.proactive.core.component.control.PAGCMLifeCycleController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory.createFcItfType(Constants.SUPER_CONTROLLER,
                /* SUPER CONTROLLER */org.objectweb.proactive.core.component.control.PASuperController.class
                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(Constants.NAME_CONTROLLER,
                /* NAME CONTROLLER */org.objectweb.fractal.api.control.NameController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(Constants.MEMBRANE_CONTROLLER,
                /* MEMBRANE CONTROLLER */PAMembraneController.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),

        };

        Type type = type_factory.createFcType(fItfTypes, nfItfTypes);

        componentA = cf.newFcInstance(type, new ControllerDescription("myComposite", Constants.COMPOSITE,
            !Constants.SYNCHRONOUS, Constants.WITHOUT_CONFIG_FILE), null);

        PAMembraneController memController = Utils.getPAMembraneController(componentA);
        //Setting the controllers by hand

        memController.setControllerObject(Constants.BINDING_CONTROLLER,
                org.objectweb.proactive.core.component.control.PABindingControllerImpl.class.getName());
        memController.setControllerObject(Constants.CONTENT_CONTROLLER,
                org.objectweb.proactive.core.component.control.PAContentControllerImpl.class.getName());
        memController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.control.PASuperControllerImpl.class.getName());
        memController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.control.PANameControllerImpl.class.getName());

        memController.startMembrane();//Starting the membrane, non-functional calls can be emitted on controllers
        //Emmiting calls on non-functional interfaces

        System.out.println("Name of the composite is :" + GCM.getNameController(componentA).getFcName());

        Component componentB = cf.newFcInstance(type, new ControllerDescription("componentB",
            Constants.PRIMITIVE, !Constants.SYNCHRONOUS, Constants.WITHOUT_CONFIG_FILE),
                new ContentDescription(ComponentA.class.getName(), new Object[] { "tata" }));

        PAMembraneController componentBMembraneController = Utils.getPAMembraneController(componentB);

        componentBMembraneController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.control.PASuperControllerImpl.class.getName());
        componentBMembraneController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.control.PANameControllerImpl.class.getName());
        componentBMembraneController.startMembrane();//Need to do this, otherwise, when adding this component to the composite one, there will be a suspension, because the addFcSubComponent method is calling the SuperController of the primitive component

        GCM.getContentController(componentA).addFcSubComponent(componentB);
        GCM.getBindingController(componentA).bindFc("componentInfo",
                componentB.getFcInterface("componentInfo"));

        System.out.println("Parameters are : " +
            ((PAComponent) componentA).getComponentParameters().getHierarchicalType());
        System.out.println("Lifecycle state is : " + GCM.getGCMLifeCycleController(componentA).getFcState());

        System.out.println("Name is :" + GCM.getNameController(componentA).getFcName());
        Component[] tabComp = GCM.getSuperController(componentB).getFcSuperComponents();
        System.out.println("Super components of primitive: " + tabComp);
        //tabComp=GCM.getContentController(componentA).getFcSubComponents();
        //Component[] tabComp2=GCM.getSuperController(tabComp[0]).getFcSuperComponents();
        //System.err.println("Super components of primitive: "+tabComp2);

        memController.stopMembrane();
        memController.setControllerObject(Constants.BINDING_CONTROLLER,
                org.objectweb.proactive.core.component.control.PABindingControllerImpl.class.getName());
        memController.setControllerObject(Constants.CONTENT_CONTROLLER,
                org.objectweb.proactive.core.component.control.PAContentControllerImpl.class.getName());
        memController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.control.PASuperControllerImpl.class.getName());
        memController.setControllerObject(Constants.NAME_CONTROLLER,
                org.objectweb.proactive.core.component.control.PANameControllerImpl.class.getName());

        memController.startMembrane();
        GCM.getBindingController(componentA).unbindFc("componentInfo");
        GCM.getContentController(componentA).removeFcSubComponent(componentB);
        GCM.getContentController(componentA).addFcSubComponent(componentB);
        GCM.getBindingController(componentA).bindFc("componentInfo",
                componentB.getFcInterface("componentInfo"));

        System.out.println("Parameters are : " +
            ((PAComponent) componentA).getComponentParameters().getHierarchicalType());
        System.out.println("Lifecycle state is : " + GCM.getGCMLifeCycleController(componentA).getFcState());
        System.out.println("Name of the composte is :" + GCM.getNameController(componentA).getFcName());

        componentBMembraneController.stopMembrane();
        componentBMembraneController.setControllerObject(Constants.SUPER_CONTROLLER,
                org.objectweb.proactive.core.component.control.PASuperControllerImpl.class.getName());
        componentBMembraneController.startMembrane();
        tabComp = GCM.getSuperController(componentB).getFcSuperComponents();

        System.out.println("Super components of composite: " + tabComp);

    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {
        GCM.getGCMLifeCycleController(componentA).stopFc();
    }

    public boolean postConditions() throws Exception {
        return (componentA instanceof PAComponentRepresentative);
    }
}
