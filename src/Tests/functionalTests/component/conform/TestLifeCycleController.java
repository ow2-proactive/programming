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
package functionalTests.component.conform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.etsi.uri.gcm.api.control.GCMLifeCycleController;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Utils;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.CLifeCycleController;
import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.StateAccessor;


public class TestLifeCycleController extends Conformtest {
    protected Component boot;
    protected GCMTypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected Component c;
    protected Component d;

    // -------------------------------------------------------------------------
    // Constructor and setup
    // -------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
        boot = Utils.getBootstrapComponent();
        tf = GCM.getGCMTypeFactory(boot);
        gf = GCM.getGenericFactory(boot);
        t = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("server", I.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                tf.createFcItfType("servers", I.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION),
                tf.createFcItfType("client", I.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                tf.createFcItfType("clients", I.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION) });
        setUpComponents();
    }

    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, flatPrimitive, C.class.getName());
        d = gf.newFcInstance(t, flatPrimitive, C.class.getName());
    }

    // -------------------------------------------------------------------------
    // Test started and stopped states
    // -------------------------------------------------------------------------
    @Test
    public void testStarted() throws Exception {
        // assumes that a method call on a stopped interface hangs
        GCM.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        assertEquals(GCMLifeCycleController.STOPPED, GCM.getGCMLifeCycleController(c).getFcState());
        GCM.getGCMLifeCycleController(c).startFc();
        assertEquals(GCMLifeCycleController.STARTED, GCM.getGCMLifeCycleController(c).getFcState());
        final I i = (I) c.getFcInterface("server");
        i.m(true);
    }

    @Test
    public void testCustomLifeCycleController() throws Exception {
        t = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("state-accessor", StateAccessor.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType("server", I.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                tf.createFcItfType("servers", I.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION),
                tf.createFcItfType("client", I.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE),
                tf.createFcItfType("clients", I.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.COLLECTION) });
        c = gf.newFcInstance(t, flatPrimitive, CLifeCycleController.class.getName());
        GCM.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        StateAccessor stateAccessor = (StateAccessor) c.getFcInterface("state-accessor");
        assertEquals(GCMLifeCycleController.STOPPED, GCM.getGCMLifeCycleController(c).getFcState());
        assertEquals(CLifeCycleController.CUSTOM_STOPPED, stateAccessor.getFcCustomState());
        GCM.getGCMLifeCycleController(c).startFc();
        assertEquals(GCMLifeCycleController.STARTED, GCM.getGCMLifeCycleController(c).getFcState());
        assertEquals(CLifeCycleController.CUSTOM_STARTED, stateAccessor.getFcCustomState());
        final I i = (I) c.getFcInterface("server");
        i.m(true);
        GCM.getGCMLifeCycleController(c).stopFc();
        assertEquals(GCMLifeCycleController.STOPPED, GCM.getGCMLifeCycleController(c).getFcState());
        assertEquals(CLifeCycleController.CUSTOM_STOPPED, stateAccessor.getFcCustomState());
    }

    // TODO test issue: this test assumes that a call on a stopped interface hangs
    /*
     * This is only one of the possible semantics for the lifecycle controller. For instance, we
     * choose instead, in AOKell, to throw a RuntimeException. Hence the thread is no longer alive.
     */

    //  public void testStopped () throws Exception {      
    //    final I i = (I)c.getFcInterface("server");
    //    Thread t = new Thread(new Runnable() {
    //      public void run () { i.m(true); }
    //    });
    //    t.start();
    //    t.join(50);
    //    assertTrue(t.isAlive());
    //  }
    // -------------------------------------------------------------------------
    // Test errors in start
    // -------------------------------------------------------------------------
    @Test
    public void testMandatoryInterfaceNotBound() throws Exception {
        try {
            GCM.getGCMLifeCycleController(c).startFc();
            fail();
        } catch (IllegalLifeCycleException ilce) {
            assertEquals(GCMLifeCycleController.STOPPED, GCM.getGCMLifeCycleController(c).getFcState());
        }
    }

    // -------------------------------------------------------------------------
    // Test invalid operations in started state
    // -------------------------------------------------------------------------
    @Test
    public void testUnbindNotStopped() throws Exception {
        GCM.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        GCM.getBindingController(c).bindFc("clients0", d.getFcInterface("servers0"));
        GCM.getGCMLifeCycleController(c).startFc();
        try {
            GCM.getBindingController(c).unbindFc("client");
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
        try {
            GCM.getBindingController(c).unbindFc("clients0");
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
    }
}
