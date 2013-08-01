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

import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.junit.Before;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.control.PAInterceptorController;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import functionalTests.ComponentTest;
import functionalTests.component.controller.DummyController;


/**
 *
 * Common code to test interception of functional invocations.
 *
 * @author The ProActive Team
 */
public abstract class CommonSetup extends ComponentTest {
    public static final String DUMMY_VALUE = "dummy-value";

    protected Component boot;
    protected PAGCMTypeFactory typeFactory;
    protected GenericFactory genericFactory;
    protected Factory factory;
    protected Component componentA;
    protected Component componentB;
    protected PAInterceptorController interceptorController;
    protected DummyController dummyController;
    protected FooItf fooItf;
    protected Foo2Itf foo2Itf;
    protected Method fooMethod;
    protected Method foo2Method;

    public CommonSetup(String name, String description) {
        super(name, description);
    }

    @Before
    public void setUp() throws Exception {
        this.boot = Utils.getBootstrapComponent();
        this.typeFactory = Utils.getPAGCMTypeFactory(boot);
        this.genericFactory = GCM.getGenericFactory(boot);
        this.factory = FactoryFactory.getFactory();

        this.componentA = this.newComponentA();
        this.interceptorController = Utils.getPAInterceptorController(this.componentA);
        this.dummyController = (DummyController) this.componentA
                .getFcInterface(DummyController.DUMMY_CONTROLLER_NAME);
        this.fooItf = (FooItf) this.componentA.getFcInterface(FooItf.SERVER_ITF_NAME);
        this.foo2Itf = (Foo2Itf) this.componentA.getFcInterface(Foo2Itf.SERVER_ITF_NAME);
        this.fooMethod = FooItf.class.getDeclaredMethod("foo", new Class[0]);
        this.foo2Method = Foo2Itf.class.getDeclaredMethod("foo2", new Class[0]);

        this.componentB = genericFactory.newFcInstance(typeFactory.createFcType(new InterfaceType[] {
                typeFactory.createFcItfType(FooItf.SERVER_ITF_NAME, FooItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                typeFactory.createFcItfType(Foo2Itf.SERVER_ITF_NAME, Foo2Itf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) }),
                new ControllerDescription("B", Constants.PRIMITIVE), new ContentDescription(
                    B.class.getName(), new Object[] {}));
        GCM.getGCMLifeCycleController(this.componentB).startFc();

        GCM.getBindingController(this.componentA).bindFc(FooItf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(FooItf.SERVER_ITF_NAME));
        GCM.getBindingController(this.componentA).bindFc(Foo2Itf.CLIENT_ITF_NAME,
                this.componentB.getFcInterface(Foo2Itf.SERVER_ITF_NAME));
    }

    protected abstract Component newComponentA() throws Exception;

    protected String getInterceptionMessage(String interceptionID, String interfaceName, String methodName) {
        return interceptionID + interfaceName + "-" + methodName + " - ";
    }

    protected void callAndCheckResult(Method method, Object instance, String expectedResult)
            throws IllegalAccessException, InvocationTargetException, IllegalArgumentException {
        this.dummyController.setDummyValue(CommonSetup.DUMMY_VALUE);
        method.invoke(instance, new Object[0]);
        Assert.assertEquals(expectedResult, this.dummyController.getDummyValue());
    }
}
