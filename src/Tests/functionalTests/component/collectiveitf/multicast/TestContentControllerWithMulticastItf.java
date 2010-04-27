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

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
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
import org.objectweb.proactive.core.component.Utils;

import functionalTests.ComponentTest;


public class TestContentControllerWithMulticastItf extends ComponentTest {
    protected Component boot;
    protected GCMTypeFactory tf;
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
        boot = Utils.getBootstrapComponent();
        tf = GCM.getGCMTypeFactory(boot);
        gf = GCM.getGenericFactory(boot);
        tRoot = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("tester", Tester.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.MANDATORY),
                tf.createGCMItfType("serverMult", MulticastTestItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, GCMTypeFactory.MULTICAST_CARDINALITY) });
        tTester = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("tester", Tester.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.MANDATORY),
                tf.createGCMItfType("clientItf", MulticastTestItf.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, GCMTypeFactory.MULTICAST_CARDINALITY) });
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
        ContentController cc = GCM.getContentController(root);
        cc.addFcSubComponent(tester);
        cc.addFcSubComponent(server1);
        cc.addFcSubComponent(server2);
        cc.addFcSubComponent(server3);
        cc.addFcSubComponent(server4);
    }

    protected void setUpBindings() throws Exception {
        GCM.getBindingController(root).bindFc("serverMult", server1.getFcInterface("server"));
        GCM.getBindingController(root).bindFc("serverMult", server2.getFcInterface("server"));
        GCM.getBindingController(root).bindFc("tester", tester.getFcInterface("tester"));
        GCM.getBindingController(tester).bindFc("clientItf", server3.getFcInterface("server"));
        GCM.getBindingController(tester).bindFc("clientItf", server4.getFcInterface("server"));
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component binded on the Multicast Interface of the root component
    // -----------------------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testRemoveServer1AndFail() throws Exception {
        ContentController cc = GCM.getContentController(root);
        cc.removeFcSubComponent(server1);
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component which has components binded on its Multicast Interface
    // -----------------------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testRemoveTesterAndFail() throws Exception {
        ContentController cc = GCM.getContentController(root);
        cc.removeFcSubComponent(tester);
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component binded on the Multicast Interface of an inner component
    // -----------------------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testRemoveServer3AndFail() throws Exception {
        ContentController cc = GCM.getContentController(root);
        cc.removeFcSubComponent(server3);
    }

    // -----------------------------------------------------------------------------------------
    // Test remove component previously binded on the Multicast Interface of an inner component
    // -----------------------------------------------------------------------------------------
    @Test
    @Ignore
    public void testRemoveServer3() throws Exception {
        ContentController cc = GCM.getContentController(root);
        GCM.getMulticastController(tester).unbindGCMMulticast("clientItf", server3.getFcInterface("server"));
        cc.removeFcSubComponent(server3);
    }
}
