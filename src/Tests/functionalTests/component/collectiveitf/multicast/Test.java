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
package functionalTests.component.collectiveitf.multicast;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Ignore;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Utils;

import functionalTests.ComponentTest;


public class Test extends ComponentTest {

    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;

    public Test() {
        super("Multicast invocations for components", "Multicast invocations for components");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();

        // simple test first
        Component simpleTestCase = (Component) f.newComponent(
                "functionalTests.component.collectiveitf.multicast.simple.testcase", context);
        GCM.getGCMLifeCycleController(simpleTestCase).startFc();
        ((Tester) simpleTestCase.getFcInterface("runTestItf")).testOwnClientMulticastItf();

        // more complex testcase now
        Component testcase = (Component) f.newComponent(
                "functionalTests.component.collectiveitf.multicast.testcase", context);

        GCM.getGCMLifeCycleController(testcase).startFc();
        ((Tester) testcase.getFcInterface("runTestItf")).testConnectedServerMulticastItf();
        ((Tester) testcase.getFcInterface("runTestItf")).testOwnClientMulticastItf();
    }

    @org.junit.Test
    public void testMulticastServerItfNotBound() throws Exception {
        Component boot = Utils.getBootstrapComponent();
        GCMTypeFactory tf = GCM.getGCMTypeFactory(boot);
        GenericFactory gf = GCM.getGenericFactory(boot);
        ComponentType ct = tf.createFcType(new InterfaceType[] { tf.createGCMItfType("serverMult",
                MulticastTestItf.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                GCMTypeFactory.MULTICAST_CARDINALITY) });
        Component composite = gf.newFcInstance(ct, "composite", null);
        try {
            GCM.getGCMLifeCycleController(composite).startFc();
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
    }

    @org.junit.Test
    @Ignore
    public void testStartCompositeWithInternalClientItfBoundOnMulticast() throws Exception {
        Component boot = Utils.getBootstrapComponent();
        GCMTypeFactory ptf = GCM.getGCMTypeFactory(boot);
        GenericFactory gf = GCM.getGenericFactory(boot);
        ComponentType rType = ptf.createFcType(new InterfaceType[] {
                ptf.createFcItfType("server", ServerTestItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                ptf.createFcItfType("client", ServerTestItf.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE) });
        ComponentType cType = ptf.createFcType(new InterfaceType[] {
                ptf.createFcItfType("server", ServerTestItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                ptf.createGCMItfType("client", MulticastTestItf.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.OPTIONAL, GCMTypeFactory.MULTICAST_CARDINALITY) });
        Component r = gf.newFcInstance(rType, "composite", null);
        Component c = gf.newFcInstance(cType, "primitive", ClientServerImpl.class.getName());
        ContentController cc = GCM.getContentController(r);
        cc.addFcSubComponent(c);
        GCM.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        GCM.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        try {
            GCM.getGCMLifeCycleController(r).startFc();
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
        GCM.getBindingController(c).bindFc("client", r.getFcInterface("client"));
        try {
            GCM.getGCMLifeCycleController(r).startFc();
        } catch (IllegalLifeCycleException ilce) {
            fail();
        }
    }
}
