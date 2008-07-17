/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.collectiveitf.multicast;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

import functionalTests.ComponentTest;


public class TestContentControllerWithMulticastItf extends ComponentTest {
    protected Component boot;
    protected ProActiveTypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType tRoot;
    protected ComponentType tTester;
    protected ComponentType tServer;
    protected Component root;
    protected Component tester;
    protected Component server1;
    protected Component server2;
    protected Component server3;
    protected Component server4;

    // -----------------------------------------------------------------------------------------
    // Setup
    // -----------------------------------------------------------------------------------------

    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = (ProActiveTypeFactory) Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
        tRoot = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("tester", Tester.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.MANDATORY),
                tf.createFcItfType("serverMult", MulticastTestItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, ProActiveTypeFactory.MULTICAST_CARDINALITY) });
        tTester = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("tester", Tester.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.MANDATORY),
                tf.createFcItfType("clientItf", MulticastTestItf.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, ProActiveTypeFactory.MULTICAST_CARDINALITY) });
        tServer = tf.createFcType(new InterfaceType[] { tf.createFcItfType("server", ServerTestItf.class
                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.MANDATORY) });
        setUpComponents();
        setUpBindings();
    }

    protected void setUpComponents() throws Exception {
        root = gf.newFcInstance(tRoot, "composite", null);
        tester = gf.newFcInstance(tTester, "primitive", TesterImpl.class.getName());
        server1 = gf.newFcInstance(tServer, "primitive", ServerImpl.class.getName());
        server2 = gf.newFcInstance(tServer, "primitive", ServerImpl.class.getName());
        server3 = gf.newFcInstance(tServer, "primitive", ServerImpl.class.getName());
        server4 = gf.newFcInstance(tServer, "primitive", ServerImpl.class.getName());
        ContentController cc = Fractal.getContentController(root);
        cc.addFcSubComponent(tester);
        cc.addFcSubComponent(server1);
        cc.addFcSubComponent(server2);
        cc.addFcSubComponent(server3);
        cc.addFcSubComponent(server4);
    }

    protected void setUpBindings() throws Exception {
        Fractal.getBindingController(root).bindFc("serverMult", server1.getFcInterface("server"));
        Fractal.getBindingController(root).bindFc("serverMult", server2.getFcInterface("server"));
        Fractal.getBindingController(root).bindFc("tester", tester.getFcInterface("tester"));
        Fractal.getBindingController(tester).bindFc("clientItf", server3.getFcInterface("server"));
        Fractal.getBindingController(tester).bindFc("clientItf", server4.getFcInterface("server"));
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component binded on the Multicast Interface of the root component
    // -----------------------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testRemoveServer1AndFail() throws Exception {
        ContentController cc = Fractal.getContentController(root);
        cc.removeFcSubComponent(server1);
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component which has components binded on its Multicast Interface
    // -----------------------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testRemoveTesterAndFail() throws Exception {
        ContentController cc = Fractal.getContentController(root);
        cc.removeFcSubComponent(tester);
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component binded on the Multicast Interface of an inner component
    // -----------------------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testRemoveServer3AndFail() throws Exception {
        ContentController cc = Fractal.getContentController(root);
        cc.removeFcSubComponent(server3);
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component previously binded on the Multicast Interface of an inner component
    // -----------------------------------------------------------------------------------------
    @Test
    @Ignore
    public void testRemoveServer3() throws Exception {
        ContentController cc = Fractal.getContentController(root);
        Fractive.getMulticastController(tester).unbindFcMulticast("clientItf",
                (ProActiveInterface) server3.getFcInterface("server"));
        cc.removeFcSubComponent(server3);
    }
}
