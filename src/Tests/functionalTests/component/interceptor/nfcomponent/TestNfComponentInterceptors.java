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
package functionalTests.component.interceptor.nfcomponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.control.PABindingController;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleController;
import org.objectweb.proactive.core.component.control.PAInterceptorController;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.exceptions.IllegalInterceptorException;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;

import functionalTests.component.controller.DummyController;
import functionalTests.component.interceptor.A;
import functionalTests.component.interceptor.CommonSetup;
import functionalTests.component.interceptor.Foo2Itf;
import functionalTests.component.interceptor.FooItf;


/**
 *
 * Checks that interception of functional invocations works.
 * <br>
 * Interceptors are only NF components and are only placed around the "A" component.
 *
 * @author The ProActive Team
 */
public class TestNfComponentInterceptors extends CommonSetup {
    private static final String INTERCEPTOR_ID = InterceptorImpl.COMPONENT_NAME + "." +
        InterceptorImpl.INTERCEPTOR_SERVICES;

    public TestNfComponentInterceptors() {
        super("Components : interception of functional invocations",
                "Components : interception of functional invocations");
    }

    @Override
    public Component newComponentA() throws Exception {
        InterfaceType[] fItfTypes = new InterfaceType[] {
                this.typeFactory.createFcItfType(FooItf.SERVER_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(Foo2Itf.SERVER_ITF_NAME, Foo2Itf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(FooItf.CLIENT_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(Foo2Itf.CLIENT_ITF_NAME, Foo2Itf.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY, TypeFactory.SINGLE) };

        InterfaceType[] nfItfTypes = new InterfaceType[] {
                this.typeFactory.createFcItfType(Constants.BINDING_CONTROLLER, PABindingController.class
                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(Constants.LIFECYCLE_CONTROLLER,
                        PAGCMLifeCycleController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(Constants.NAME_CONTROLLER, NameController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(Constants.MEMBRANE_CONTROLLER, PAMembraneController.class
                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(Constants.INTERCEPTOR_CONTROLLER,
                        PAInterceptorController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                this.typeFactory.createFcItfType(DummyController.DUMMY_CONTROLLER_NAME, DummyController.class
                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE)

        };

        Type type = this.typeFactory.createFcType(fItfTypes, nfItfTypes);

        Component componentA = this.genericFactory.newFcInstance(type, new ControllerDescription(
            "componentA", Constants.PRIMITIVE, !Constants.SYNCHRONOUS, Constants.WITHOUT_CONFIG_FILE),
                new ContentDescription(A.class.getName(), new Object[] {}));

        Factory nfFactory = FactoryFactory.getNFFactory();
        Component interceptor = (Component) nfFactory.newComponent(
                "functionalTests.component.interceptor.nfcomponent.adl.Interceptor",
                new HashMap<String, Object>());
        GCM.getNameController(interceptor).setFcName(InterceptorImpl.COMPONENT_NAME);

        PAMembraneController membraneController = Utils.getPAMembraneController(componentA);
        membraneController.nfAddFcSubComponent(interceptor);
        membraneController.nfBindFc(DummyController.DUMMY_CONTROLLER_NAME, InterceptorImpl.COMPONENT_NAME +
            "." + InterceptorImpl.DUMMY_SERVICES);
        membraneController.startMembrane();

        return componentA;
    }

    @Test
    public void testInterceptorController() throws Exception {
        List<String> expectedInterceptorID = new ArrayList<String>();

        expectedInterceptorID.add(INTERCEPTOR_ID);
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME, INTERCEPTOR_ID);
        Assert.assertEquals(expectedInterceptorID, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.SERVER_ITF_NAME));

        expectedInterceptorID.remove(INTERCEPTOR_ID);
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME, INTERCEPTOR_ID);
        Assert.assertEquals(expectedInterceptorID, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.SERVER_ITF_NAME));
    }

    @Test
    public void testAddInterceptorOnServerInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME, INTERCEPTOR_ID);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE +
            this.getInterceptionMessage(InterceptorImpl.BEFORE_INTERCEPTION, FooItf.SERVER_ITF_NAME,
                    this.fooMethod.getName()) +
            this.getInterceptionMessage(InterceptorImpl.AFTER_INTERCEPTION, FooItf.SERVER_ITF_NAME,
                    this.fooMethod.getName()));

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testAddInterceptorOnClientInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.CLIENT_ITF_NAME, INTERCEPTOR_ID);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE +
            this.getInterceptionMessage(InterceptorImpl.BEFORE_INTERCEPTION, FooItf.CLIENT_ITF_NAME,
                    this.fooMethod.getName()) +
            this.getInterceptionMessage(InterceptorImpl.AFTER_INTERCEPTION, FooItf.CLIENT_ITF_NAME,
                    this.fooMethod.getName()));

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test(expected = NoSuchComponentException.class)
    public void testAddInterceptorWithNonExistingNFComponent() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME, "error-component." +
            InterceptorImpl.INTERCEPTOR_SERVICES);
    }

    @Test(expected = NoSuchInterfaceException.class)
    public void testAddInterceptorWithNonExistingInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                InterceptorImpl.COMPONENT_NAME + ".error-interface");
    }

    @Test(expected = IllegalInterceptorException.class)
    public void testAddInterceptorWithIllegalInterceptor() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                InterceptorImpl.COMPONENT_NAME + "." + InterceptorImpl.DUMMY_SERVICES);
    }

    @Test
    public void testRemoveInterceptorFromInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME, INTERCEPTOR_ID);
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME, INTERCEPTOR_ID);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test(expected = NoSuchComponentException.class)
    public void testRemoveInterceptorWithNonExistingNFComponent() throws Exception {
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME, "error-component." +
            InterceptorImpl.INTERCEPTOR_SERVICES);
    }

    @Test(expected = NoSuchInterfaceException.class)
    public void testRemoveInterceptorWithNonExistingInterface() throws Exception {
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME,
                InterceptorImpl.COMPONENT_NAME + ".error-interface");
    }

    @Test(expected = IllegalInterceptorException.class)
    public void testRemoveInterceptorWithIllegalInterceptor() throws Exception {
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME,
                InterceptorImpl.COMPONENT_NAME + "." + InterceptorImpl.DUMMY_SERVICES);
    }

    @Test
    public void testAddInterceptorsWithADL() throws Exception {
        this.componentA = (Component) this.factory.newComponent(
                "functionalTests.component.interceptor.nfcomponent.adl.A", new HashMap<String, Object>());
        this.interceptorController = Utils.getPAInterceptorController(this.componentA);
        this.dummyController = (DummyController) this.componentA
                .getFcInterface(DummyController.DUMMY_CONTROLLER_NAME);
        this.fooItf = (FooItf) this.componentA.getFcInterface(FooItf.SERVER_ITF_NAME);
        this.foo2Itf = (Foo2Itf) this.componentA.getFcInterface(Foo2Itf.SERVER_ITF_NAME);

        GCM.getBindingController(this.componentA).bindFc(FooItf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(FooItf.SERVER_ITF_NAME));
        GCM.getBindingController(this.componentA).bindFc(Foo2Itf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(Foo2Itf.SERVER_ITF_NAME));

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        List<String> expectedInterceptorID = new ArrayList<String>();
        expectedInterceptorID.add(INTERCEPTOR_ID);
        Assert.assertEquals(expectedInterceptorID, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.SERVER_ITF_NAME));
        Assert.assertEquals(expectedInterceptorID, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.CLIENT_ITF_NAME));
        expectedInterceptorID.clear();
        Assert.assertEquals(expectedInterceptorID, this.interceptorController
                .getInterceptorIDsFromInterface(Foo2Itf.SERVER_ITF_NAME));
        Assert.assertEquals(expectedInterceptorID, this.interceptorController
                .getInterceptorIDsFromInterface(Foo2Itf.CLIENT_ITF_NAME));

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE +
            this.getInterceptionMessage(InterceptorImpl.BEFORE_INTERCEPTION, FooItf.SERVER_ITF_NAME,
                    this.fooMethod.getName()) +
            this.getInterceptionMessage(InterceptorImpl.BEFORE_INTERCEPTION, FooItf.CLIENT_ITF_NAME,
                    this.fooMethod.getName()) +
            this.getInterceptionMessage(InterceptorImpl.AFTER_INTERCEPTION, FooItf.CLIENT_ITF_NAME,
                    this.fooMethod.getName()) +
            this.getInterceptionMessage(InterceptorImpl.AFTER_INTERCEPTION, FooItf.SERVER_ITF_NAME,
                    this.fooMethod.getName()));

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test(expected = ADLException.class)
    public void testAddInterceptorWithADLWithNonExistingNFComponent() throws Exception {
        this.componentA = (Component) this.factory.newComponent(
                "functionalTests.component.interceptor.nfcomponent.adl.A-NonExistingNFComponent",
                new HashMap<String, Object>());
    }

    @Test(expected = ADLException.class)
    public void testAddInterceptorWithADLWithNonExistingInterface() throws Exception {
        this.componentA = (Component) this.factory.newComponent(
                "functionalTests.component.interceptor.nfcomponent.adl.A-NonExistingInterface",
                new HashMap<String, Object>());
    }
}
