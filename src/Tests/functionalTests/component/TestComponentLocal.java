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
package functionalTests.component;

import java.util.Arrays;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.type.Composite;

import functionalTests.ComponentTest;


public class TestComponentLocal extends ComponentTest {
    private static final String P1_NAME = "primitive-component-1";
    private static final String P2_NAME = "primitive-component-2";
    private static final String C1_NAME = "composite-component1";
    private static final String C2_NAME = "composite-component2";
    private static String MESSAGE = "-->Main";
    private static Component p1;
    private static Component p2;
    private static Component c1;
    private static Component c2;

    /**
     * @author The ProActive Team
     *
     * Step 1. Creation of the components
     *
     * Creates the following components :
     *
     *                 __________________
     *                 |                                                                        |                                        ________
     *                 |                                                                        |                                        |                                |
     *         i1        |                                                                        |i2                         i2        |        (p2)                |
     *                 |                                                                        |                                        |_______|
     *                 |                                                                        |
     *                 |_(c1)______________|
     *
     *                 __________________
     *                 |                                                                        |                                        ________
     *                 |                                                                        |                                        |                                |
     *         i1        |                                                                        |i2                         i1        |        (p1)                |i2
     *                 |                                                                        |                                        |_______|
     *                 |                                                                        |
     *                 |_(c2)_____________|
     *
     *         where :
     *                 (c1) and (c2) are composites, (p1) and (p2) are primitive components
     *                 i1 represents an interface of type I1
     *                 i2 represents an interface of type I2
     *
     */
    @org.junit.Test
    public void testCreationNewactiveComposite() throws Exception {
        Component boot = Utils.getBootstrapComponent();
        GCMTypeFactory type_factory = GCM.getGCMTypeFactory(boot);
        GenericFactory cf = GCM.getGenericFactory(boot);

        // check that composites with no functional interface can be instantiated
        ComponentType voidType = type_factory.createFcType(new InterfaceType[] {});
        cf.newFcInstance(voidType, new ControllerDescription("void composite", Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(), new Object[] {}));

        ComponentType i1_i2_type = type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType("i1", I1.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType("i2", I2.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE) });

        p1 = cf.newFcInstance(i1_i2_type, new ControllerDescription(P1_NAME, Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentA.class.getName(), new Object[] {}));
        p2 = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                "i2", I2.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) }),
                new ControllerDescription(P2_NAME, Constants.PRIMITIVE), new ContentDescription(
                    PrimitiveComponentB.class.getName(), new Object[] {}));
        c1 = cf.newFcInstance(i1_i2_type, new ControllerDescription(C1_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(), new Object[] {}));
        c2 = cf.newFcInstance(i1_i2_type, new ControllerDescription(C2_NAME, Constants.COMPOSITE),
                new ContentDescription(Composite.class.getName(), new Object[] {}));

        Assert.assertEquals(GCM.getNameController(p1).getFcName(), P1_NAME);
        Assert.assertEquals(GCM.getNameController(p2).getFcName(), P2_NAME);
        Assert.assertEquals(GCM.getNameController(c1).getFcName(), C1_NAME);
        Assert.assertEquals(GCM.getNameController(c2).getFcName(), C2_NAME);

        //        System.err.println("CREATION c1:" + c1 + "c2:" + c2 + "p1:" + p1 +
        //            "p2:" + p2);
    }

    /**
     * @author The ProActive Team
     *
     * Step 2 : assembles the following component system :
     *
     *                 ___________________________
     *                 |                __________________                |
     *                 |                |                        ______                        |                |                                                ________
     *                 |                |                        |                        |                        |                |                                                |                                |
     *         i1 | i1  |    i1        |(p1)        |i2            |i2   | i2           i2        |        (p2)                |
     *                 |                |                        |_____|                        |                |                                                |                                |
     *                 |                |                                                                        |                |                                                |_______|
     *                 |                |_(c1)_____________|                |
     *                 |                                                                                                        |
     *                 |__(c2)____________________|
     *
     *         where :
     *                 (c2) and (c1) are composite components, (p1) and (p2) are primitive components
     *                 i1 represents an interface of type I1
     *                 i2 represents an interface of type I2
     *
     */
    @org.junit.Test
    public void testAssemblyLocalComposite() throws Exception {
        new TestComponentLocal().testCreationNewactiveComposite();
        //        System.err.println("ASSEMBLY c1:" + c1 + "c2:" + c2 + "p1:" + p1 +
        //            "p2:" + p2);
        // ASSEMBLY
        GCM.getContentController(c1).addFcSubComponent(p1);
        GCM.getContentController(c2).addFcSubComponent(c1);

        Component[] c2SubComponents = GCM.getContentController(c2).getFcSubComponents();
        Component[] c1SubComponents = GCM.getContentController(c1).getFcSubComponents();

        Component[] c2_sub_components = { c1 };
        Component[] c1_sub_components = { p1 };

        // a test for super controllers
        Component[] c1_super_components = GCM.getSuperController(c1).getFcSuperComponents();
        Component[] p1_super_components = GCM.getSuperController(p1).getFcSuperComponents();

        Assert.assertTrue(Arrays.equals(c1_super_components, new Component[] { c2 }));
        Assert.assertTrue(Arrays.equals(p1_super_components, new Component[] { c1 }));

        Assert.assertTrue(Arrays.equals(c2SubComponents, c2_sub_components));
        Assert.assertTrue(Arrays.equals(c1SubComponents, c1_sub_components));
    }

    /**
     * @author The ProActive Team
     *
     * Step 3 : bindings, life cycle start, interface method invocation
     *
     *                 ___________________________
     *                 |                __________________                |
     *                 |                |                        ______                        |                |                                                ________
     *                 |                |                        |                        |                        |                |                                                |                                |
     *         i1-|-i1-|----i1        |(p1)        |i2----        |i2 --|-i2----------i2        |        (p2)                |
     *                 |                |                        |_____|                        |                |                                                |                                |
     *                 |                |                                                                        |                |                                                |_______|
     *                 |                |_(c1)_____________|                |
     *                 |                                                                                                        |
     *                 |__(c2)____________________|
     *
     *
     */
    @org.junit.Test
    public void testBindingLocalComposite() throws Exception {
        new TestComponentLocal().testAssemblyLocalComposite();
        // BINDING
        GCM.getBindingController(c2).bindFc("i1", c1.getFcInterface("i1"));
        GCM.getBindingController(c1).bindFc("i1", p1.getFcInterface("i1"));
        GCM.getBindingController(p1).bindFc("i2", c1.getFcInterface("i2"));
        GCM.getBindingController(c1).bindFc("i2", c2.getFcInterface("i2"));
        GCM.getBindingController(c2).bindFc("i2", p2.getFcInterface("i2"));

        // START LIFE CYCLE
        GCM.getGCMLifeCycleController(c2).startFc();
        GCM.getGCMLifeCycleController(p2).startFc();

        // INVOKE INTERFACE METHOD
        I1 i1 = (I1) c2.getFcInterface("i1");

        //I1 i1= (I1)p1.getFcInterface("i1");
        Message message = i1.processInputMessage(new Message(MESSAGE)).append(MESSAGE);

        Assert.assertEquals(message.toString(), TestComponentLocal.MESSAGE + PrimitiveComponentA.MESSAGE +
            PrimitiveComponentB.MESSAGE + PrimitiveComponentA.MESSAGE + TestComponentLocal.MESSAGE);
    }
}
