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
package functionalTests.component.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.control.PAInterceptorController;

import functionalTests.ComponentTest;
import functionalTests.component.controller.DummyController;


/**
 *
 * Checks that interception of functional invocations works.
 * <br>
 * Interceptors are only placed around the "A" component.
 *
 * @author The ProActive Team
 */
public class TestInterceptor extends ComponentTest {
    private Component componentA;
    private Component componentB;
    private PAInterceptorController interceptorController;
    private DummyController dummyController;
    private FooItf fooItf;
    private Foo2Itf foo2Itf;
    private Method fooMethod;
    private Method foo2Method;

    public static final String DUMMY_VALUE = "dummy-value";

    public TestInterceptor() {
        super("Components : interception of functional invocations",
                "Components : interception of functional invocations");
    }

    @Before
    public void setUp() throws Exception {
        Component boot = Utils.getBootstrapComponent();
        GCMTypeFactory type_factory = GCM.getGCMTypeFactory(boot);
        GenericFactory cf = GCM.getGenericFactory(boot);

        this.componentA = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType(FooItf.SERVER_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(Foo2Itf.SERVER_ITF_NAME, Foo2Itf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(FooItf.CLIENT_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(Foo2Itf.CLIENT_ITF_NAME, Foo2Itf.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY, TypeFactory.SINGLE) }),
                new ControllerDescription("A", Constants.PRIMITIVE, getClass().getResource(
                        "/functionalTests/component/interceptor/config.xml").getPath()),
                new ContentDescription(A.class.getName(), new Object[] {}));
        this.interceptorController = Utils.getPAInterceptorController(this.componentA);
        this.dummyController = (DummyController) this.componentA
                .getFcInterface(DummyController.DUMMY_CONTROLLER_NAME);
        this.fooItf = (FooItf) this.componentA.getFcInterface(FooItf.SERVER_ITF_NAME);
        this.foo2Itf = (Foo2Itf) this.componentA.getFcInterface(Foo2Itf.SERVER_ITF_NAME);
        this.fooMethod = FooItf.class.getDeclaredMethod("foo", new Class[0]);
        this.foo2Method = Foo2Itf.class.getDeclaredMethod("foo2", new Class[0]);

        this.componentB = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType(FooItf.SERVER_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(Foo2Itf.SERVER_ITF_NAME, Foo2Itf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) }),
                new ControllerDescription("B", Constants.PRIMITIVE), new ContentDescription(
                    B.class.getName(), new Object[] {}));
        GCM.getGCMLifeCycleController(this.componentB).startFc();

        GCM.getBindingController(this.componentA).bindFc(FooItf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(FooItf.SERVER_ITF_NAME));
        GCM.getBindingController(this.componentA).bindFc(Foo2Itf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(Foo2Itf.SERVER_ITF_NAME));
    }

    @Test
    public void testInterceptorController() throws Exception {
        List<String> expectedInterceptorIDs = new ArrayList<String>();

        expectedInterceptorIDs.add(Interceptor1.INTERCEPTOR1_NAME);
        expectedInterceptorIDs.add(Interceptor2.INTERCEPTOR2_NAME);

        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME);

        Assert.assertEquals(expectedInterceptorIDs, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.SERVER_ITF_NAME));

        expectedInterceptorIDs.remove(Interceptor2.INTERCEPTOR2_NAME);
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME);

        Assert.assertEquals(expectedInterceptorIDs, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.SERVER_ITF_NAME));
    }

    @Test
    public void testAddInterceptorAtSpecifiedPosition() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME, 0);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor2.BEFORE_INTERCEPTION +
            Interceptor1.BEFORE_INTERCEPTION + Interceptor1.AFTER_INTERCEPTION +
            Interceptor2.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testAddInterceptorOnServerInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testAddInterceptorOnClientInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.CLIENT_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testAdd2InterceptorsOnServerInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor2.BEFORE_INTERCEPTION + Interceptor2.AFTER_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testAdd2InterceptorsOnClientInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.CLIENT_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.addInterceptorOnInterface(FooItf.CLIENT_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor2.BEFORE_INTERCEPTION + Interceptor2.AFTER_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testAddListInterceptorsOnInterface() throws Exception {
        List<String> interceptorsIDs = new ArrayList<String>();
        interceptorsIDs.add(Interceptor1.INTERCEPTOR1_NAME);
        interceptorsIDs.add(Interceptor2.INTERCEPTOR2_NAME);
        this.interceptorController.addInterceptorsOnInterface(FooItf.CLIENT_ITF_NAME, interceptorsIDs);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor2.BEFORE_INTERCEPTION + Interceptor2.AFTER_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testAddInterceptorOnAllServerInterfaces() throws Exception {
        this.interceptorController.addInterceptorOnAllServerInterfaces(Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE +
            Interceptor1.BEFORE_INTERCEPTION + Interceptor1.AFTER_INTERCEPTION);
    }

    @Test
    public void testAddInterceptorOnAllClientInterfaces() throws Exception {
        this.interceptorController.addInterceptorOnAllClientInterfaces(Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE +
            Interceptor1.BEFORE_INTERCEPTION + Interceptor1.AFTER_INTERCEPTION);
    }

    @Test
    public void testAddInterceptorOnAllInterfaces() throws Exception {
        this.interceptorController.addInterceptorOnAllInterfaces(Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor1.BEFORE_INTERCEPTION + Interceptor1.AFTER_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE +
            Interceptor1.BEFORE_INTERCEPTION + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION + Interceptor1.AFTER_INTERCEPTION);
    }

    @Test(expected = IllegalLifeCycleException.class)
    public void testAddInterceptorOnStartedComponent() throws Exception {
        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
    }

    @Test(expected = IllegalBindingException.class)
    public void testAddInterceptorAtInvalidPosition() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME, 1);
    }

    @Test(expected = NoSuchInterfaceException.class)
    public void testAddInterceptorOnInvalidInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface("error-interface",
                Interceptor1.INTERCEPTOR1_NAME);
    }

    @Test(expected = IllegalBindingException.class)
    public void testAddInvalidInterceptor() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Constants.NAME_CONTROLLER);
    }

    @Test
    public void testRemoveInterceptorLocatedAtSpeficiedPosition() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME);
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME, 0);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor2.BEFORE_INTERCEPTION +
            Interceptor2.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testRemoveInterceptorFromInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME);
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor2.BEFORE_INTERCEPTION +
            Interceptor2.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testRemoveAllInterceptorsFromInterface() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor2.INTERCEPTOR2_NAME);
        this.interceptorController.removeAllInterceptorsFromInterface(FooItf.SERVER_ITF_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testRemoveInterceptorFromAllServerInterfaces() throws Exception {
        this.interceptorController.addInterceptorOnAllServerInterfaces(Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.removeInterceptorFromAllServerInterfaces(Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testRemoveInterceptorFromAllClientInterfaces() throws Exception {
        this.interceptorController.addInterceptorOnAllClientInterfaces(Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.removeInterceptorFromAllClientInterfaces(Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test
    public void testRemoveInterceptorFromAllInterfaces() throws Exception {
        this.interceptorController.addInterceptorOnAllInterfaces(Interceptor1.INTERCEPTOR1_NAME);
        this.interceptorController.removeInterceptorFromAllInterfaces(Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test(expected = IllegalLifeCycleException.class)
    public void testRemoveInterceptorOnStartedComponent() throws Exception {
        this.interceptorController.addInterceptorOnInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);

        GCM.getGCMLifeCycleController(this.componentA).startFc();

        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME,
                Interceptor1.INTERCEPTOR1_NAME);
    }

    @Test(expected = IllegalBindingException.class)
    public void testRemoveInterceptorAtInvalidPosition() throws Exception {
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME, 0);
    }

    @Test(expected = NoSuchInterfaceException.class)
    public void testRemoveInterceptorOnInvalidInterface() throws Exception {
        this.interceptorController.removeInterceptorFromInterface("error-interface",
                Interceptor1.INTERCEPTOR1_NAME);
    }

    @Test(expected = IllegalBindingException.class)
    public void testRemoveInvalidInterceptor() throws Exception {
        this.interceptorController.removeInterceptorFromInterface(FooItf.SERVER_ITF_NAME,
                Constants.NAME_CONTROLLER);
    }

    @Test
    public void testAddInterceptorsWithADL() throws Exception {
        Factory factory = FactoryFactory.getFactory();
        this.componentA = (Component) factory.newComponent("functionalTests.component.interceptor.A",
                new HashMap<String, Object>());
        this.interceptorController = Utils.getPAInterceptorController(this.componentA);
        this.dummyController = (DummyController) this.componentA
                .getFcInterface(DummyController.DUMMY_CONTROLLER_NAME);
        this.fooItf = (FooItf) this.componentA.getFcInterface(FooItf.SERVER_ITF_NAME);
        this.foo2Itf = (Foo2Itf) this.componentA.getFcInterface(Foo2Itf.SERVER_ITF_NAME);

        GCM.getGCMLifeCycleController(this.componentB).stopFc();

        GCM.getBindingController(this.componentA).bindFc(FooItf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(FooItf.SERVER_ITF_NAME));
        GCM.getBindingController(this.componentA).bindFc(Foo2Itf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(Foo2Itf.SERVER_ITF_NAME));

        GCM.getGCMLifeCycleController(this.componentA).startFc();
        GCM.getGCMLifeCycleController(this.componentB).startFc();

        List<String> expectedInterceptorIDs = new ArrayList<String>();
        expectedInterceptorIDs.add(Interceptor1.INTERCEPTOR1_NAME);
        expectedInterceptorIDs.add(Interceptor2.INTERCEPTOR2_NAME);

        Assert.assertEquals(expectedInterceptorIDs, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.SERVER_ITF_NAME));
        Assert.assertEquals(expectedInterceptorIDs, this.interceptorController
                .getInterceptorIDsFromInterface(FooItf.CLIENT_ITF_NAME));

        expectedInterceptorIDs.clear();

        Assert.assertEquals(expectedInterceptorIDs, this.interceptorController
                .getInterceptorIDsFromInterface(Foo2Itf.SERVER_ITF_NAME));
        Assert.assertEquals(expectedInterceptorIDs, this.interceptorController
                .getInterceptorIDsFromInterface(Foo2Itf.CLIENT_ITF_NAME));

        this.callAndCheckResult(this.fooMethod, this.fooItf, DUMMY_VALUE + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor2.BEFORE_INTERCEPTION + Interceptor1.BEFORE_INTERCEPTION +
            Interceptor2.BEFORE_INTERCEPTION + Interceptor2.AFTER_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION + Interceptor2.AFTER_INTERCEPTION +
            Interceptor1.AFTER_INTERCEPTION);

        this.callAndCheckResult(this.foo2Method, this.foo2Itf, DUMMY_VALUE);
    }

    @Test(expected = ADLException.class)
    public void testAddInterceptorsWithADLWithThisKeywordMissing() throws Exception {
        Factory factory = FactoryFactory.getFactory();
        this.componentA = (Component) factory.newComponent(
                "functionalTests.component.interceptor.A-ThisKeywordMissing", new HashMap<String, Object>());
    }

    @Test(expected = ADLException.class)
    public void testAddInterceptorsWithADLWithNonExistingInterceptor() throws Exception {
        Factory factory = FactoryFactory.getFactory();
        this.componentA = (Component) factory.newComponent(
                "functionalTests.component.interceptor.A-NonExistingInterceptor",
                new HashMap<String, Object>());
    }

    private void callAndCheckResult(Method method, Object instance, String expectedResult)
            throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        this.dummyController.setDummyValue(TestInterceptor.DUMMY_VALUE);
        method.invoke(instance, new Object[0]);
        Assert.assertEquals(expectedResult, this.dummyController.getDummyValue());
    }
}
