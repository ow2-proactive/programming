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
package functionalTests.component.nonfunctional.membranecontroller.bindnfc.components.internalserver;

import junit.framework.Assert;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PABindingControllerImpl;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleControllerImpl;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.control.PANameControllerImpl;
import org.objectweb.proactive.core.component.control.PASuperController;
import org.objectweb.proactive.core.component.control.PASuperControllerImpl;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import functionalTests.ComponentTest;


/**
 * @author The ProActive Team
 *
 *Testing non-functional bindings
 */
public class TestPrimitive extends ComponentTest {
    public TestPrimitive() {
        super(
                "Binds the non-functional internal server interface of primitive component to a component inside the membrane",
                "Binds the non-functional internal server interface of primitive component to a component inside the membrane");
    }

    @org.junit.Test
    public void action() throws Exception {
        //Thread.sleep(2000);
        Component boot = Utils.getBootstrapComponent();
        PAGCMTypeFactory tf = Utils.getPAGCMTypeFactory(boot);
        PAGenericFactory gf = Utils.getPAGenericFactory(boot);

        InterfaceType[] nfItfType = new InterfaceType[] {
                tf.createFcItfType(Constants.BINDING_CONTROLLER, PABindingController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType(Constants.LIFECYCLE_CONTROLLER, PAGCMLifeCycleController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType(Constants.SUPER_CONTROLLER, PASuperController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType(Constants.NAME_CONTROLLER, NameController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType(Constants.MEMBRANE_CONTROLLER, PAMembraneController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createGCMItfType(FunctionalComponent.NON_FUNCTIONAL_ITF_NAME, Service.class.getName(),
                        TypeFactory.SERVER, TypeFactory.OPTIONAL, GCMTypeFactory.SINGLETON_CARDINALITY,
                        PAGCMTypeFactory.INTERNAL) };

        InterfaceType[] fItfType = new InterfaceType[] { tf.createFcItfType(
                FunctionalComponent.FUNCTIONAL_ITF_NAME, Service.class.getName(), TypeFactory.SERVER,
                TypeFactory.MANDATORY, TypeFactory.SINGLE), };

        Type fComponentType = tf.createFcType(fItfType, nfItfType);

        Component functionalComponent = gf.newFcInstance(fComponentType,
                new ControllerDescription("FunctionalComponent", Constants.PRIMITIVE, !Constants.SYNCHRONOUS,
                    Constants.WITHOUT_CONFIG_FILE), new ContentDescription(FunctionalComponent.class
                        .getName()));

        PAMembraneController membraneController = Utils.getPAMembraneController(functionalComponent);

        membraneController.setControllerObject(Constants.BINDING_CONTROLLER, PABindingControllerImpl.class
                .getName());
        membraneController.setControllerObject(Constants.LIFECYCLE_CONTROLLER,
                PAGCMLifeCycleControllerImpl.class.getName());
        membraneController.setControllerObject(Constants.SUPER_CONTROLLER, PASuperControllerImpl.class
                .getName());
        membraneController.setControllerObject(Constants.NAME_CONTROLLER, PANameControllerImpl.class
                .getName());

        Component nonFunctionalComponent = gf.newNfFcInstance(tf.createFcType(new InterfaceType[] { tf
                .createFcItfType(NonFunctionalComponent.NON_FUNCTIONAL_ITF_NAME, Service.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), }),
                new ControllerDescription("NonFunctionalComponent", Constants.PRIMITIVE),
                new ContentDescription(NonFunctionalComponent.class.getName()));

        membraneController.nfAddFcSubComponent(nonFunctionalComponent);
        membraneController.nfBindFc(FunctionalComponent.NON_FUNCTIONAL_ITF_NAME, "NonFunctionalComponent." +
            NonFunctionalComponent.NON_FUNCTIONAL_ITF_NAME);

        membraneController.startMembrane();

        Utils.getPAGCMLifeCycleController(functionalComponent).startFc();

        Service service = (Service) functionalComponent
                .getFcInterface(FunctionalComponent.FUNCTIONAL_ITF_NAME);
        Assert.assertEquals("NonFunctionalComponent>FunctionalComponent>Message", service.notify("Message"));

        Utils.getPAGCMLifeCycleController(functionalComponent).stopFc();

        membraneController.stopMembrane();
        membraneController.nfUnbindFc(FunctionalComponent.NON_FUNCTIONAL_ITF_NAME);
        membraneController.startMembrane();

        Utils.getPAGCMLifeCycleController(functionalComponent).startFc();

        Assert.assertEquals("FunctionalComponent>Message", service.notify("Message"));
    }
}
