/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionalTests.component.collectiveitf.gathercast;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.ComponentTest;


public class Test extends ComponentTest {

    /**
     *
     */
    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;
    public static final String VALUE_1 = "10";
    public static final String VALUE_2 = "20";

    public Test() {
        super("Gather interfaces", "Gather interfaces");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<Object, Object> context = new HashMap<Object, Object>();
        Component testcase = (Component) f.newComponent(
                "functionalTests.component.collectiveitf.gathercast.testcase", context);
        //        Component clientB = (Component) f.newComponent("functionalTests.component.collectiveitf.gather.GatherClient("+VALUE_2+")",context);
        //        Component server = (Component) f.newComponent("functionalTests.component.collectiveitf.gather.GatherServer",context);
        //        GCM.getBindingController(clientA).bindFc("client", server.getFcInterface("serverGather"));
        //        GCM.getBindingController(clientB).bindFc("client", server.getFcInterface("serverGather"));
        GCM.getGCMLifeCycleController(testcase).startFc();

        for (int i = 0; i < 100; i++) {
            // several iterations for thoroughly testing concurrency issues
            BooleanWrapper result1 = ((TotoItf) testcase.getFcInterface("testA")).test();
            BooleanWrapper result2 = ((TotoItf) testcase.getFcInterface("testB")).test();

            Assert.assertTrue(result1.getBooleanValue());
            Assert.assertTrue(result2.getBooleanValue());
        }

        String result1 = ((TotoItf) testcase.getFcInterface("testA")).testWaitForAll().getStringValue();
        String result2 = ((TotoItf) testcase.getFcInterface("testB")).testWaitForAll().getStringValue();
        Assert.assertNotSame(result1, result2);
    }

    @org.junit.Test
    public void testStartCompositeWithInternalGathercastClientItf() throws Exception {
        Component boot = Utils.getBootstrapComponent();
        GCMTypeFactory ptf = GCM.getGCMTypeFactory(boot);
        GenericFactory gf = GCM.getGenericFactory(boot);
        ComponentType rType = ptf.createFcType(new InterfaceType[] {
                ptf.createFcItfType("server", GatherDummyItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                ptf.createGCMItfType("client", GatherDummyItf.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, GCMTypeFactory.GATHERCAST_CARDINALITY) });
        ComponentType cType = ptf.createFcType(new InterfaceType[] {
                ptf.createFcItfType("server", GatherDummyItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                ptf.createFcItfType("client", DummyItf.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.OPTIONAL, TypeFactory.SINGLE) });
        Component r = gf.newFcInstance(rType, "composite", null);
        Component c = gf.newFcInstance(cType, "primitive", GatherClientServer.class.getName());
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

    @org.junit.Test
    public void testUnbindAndBindGathercastItfs() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<Object, Object> context = new HashMap<Object, Object>();
        Component testcase = (Component) f.newComponent(
                "functionalTests.component.collectiveitf.gathercast.testcase", context);
        Component[] subComps = GCM.getContentController(testcase).getFcSubComponents();
        Component clientA = null;
        for (int i = 0; i < subComps.length; i++) {
            if (GCM.getNameController(subComps[i]).getFcName().equals("clientA")) {
                clientA = subComps[i];
            }
        }
        if (clientA != null) {
            BindingController bc = GCM.getBindingController(clientA);
            Object gathercastItf = bc.lookupFc("client2primitive");
            bc.unbindFc("client2primitive");
            bc.bindFc("client2primitive", gathercastItf);
            gathercastItf = bc.lookupFc("client2composite");
            bc.unbindFc("client2composite");
            bc.bindFc("client2composite", gathercastItf);
        }
        GCM.getGCMLifeCycleController(testcase).startFc();

        BooleanWrapper result1 = ((TotoItf) testcase.getFcInterface("testA")).test();
        BooleanWrapper result2 = ((TotoItf) testcase.getFcInterface("testB")).test();
        Assert.assertTrue(result1.getBooleanValue());
        Assert.assertTrue(result2.getBooleanValue());
    }
}
