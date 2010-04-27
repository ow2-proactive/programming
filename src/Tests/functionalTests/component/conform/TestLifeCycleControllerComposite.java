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
package functionalTests.component.conform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;


public class TestLifeCycleControllerComposite extends TestLifeCycleController {

    protected Component r;

    @Override
    protected void setUpComponents() throws Exception {
        Component o = gf.newFcInstance(t, "composite", null);
        r = gf.newFcInstance(t, "composite", null);
        c = gf.newFcInstance(t, "primitive", C.class.getName());
        d = gf.newFcInstance(t, "primitive", C.class.getName());
        GCM.getContentController(o).addFcSubComponent(r);
        GCM.getContentController(r).addFcSubComponent(c);
        GCM.getContentController(r).addFcSubComponent(d);
    }

    @Test
    public void testRecursiveStartStop() throws Exception {
        ContentController cc = GCM.getContentController(r);
        GCM.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        GCM.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        GCM.getBindingController(d).bindFc("client", cc.getFcInternalInterface("client"));
        GCM.getBindingController(r).bindFc("client", r.getFcInterface("server"));

        GCM.getGCMLifeCycleController(r).startFc();
        assertEquals("STARTED", GCM.getGCMLifeCycleController(r).getFcState());
        assertEquals("STARTED", GCM.getGCMLifeCycleController(c).getFcState());
        assertEquals("STARTED", GCM.getGCMLifeCycleController(d).getFcState());
        final I i = (I) r.getFcInterface("server");
        Thread t = new Thread(new Runnable() {
            public void run() {
                i.m(true);
            }
        });
        t.start();
        t.join(50);
        assertTrue(!t.isAlive());

        GCM.getGCMLifeCycleController(r).stopFc();
        assertEquals("STOPPED", GCM.getGCMLifeCycleController(r).getFcState());
        assertEquals("STOPPED", GCM.getGCMLifeCycleController(c).getFcState());
        assertEquals("STOPPED", GCM.getGCMLifeCycleController(d).getFcState());
    }

    @Override
    @Test
    public void testMandatoryInterfaceNotBound() throws Exception {
        super.testMandatoryInterfaceNotBound();
        ContentController cc = GCM.getContentController(r);
        cc.removeFcSubComponent(c);
        cc.removeFcSubComponent(d);
        GCM.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        try {
            GCM.getGCMLifeCycleController(c).startFc();
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
    }

    @Test
    public void testCompositeMandatoryServerInterfaceNotBound() throws Exception {
        GCM.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        GCM.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        GCM.getBindingController(d).bindFc("client", c.getFcInterface("server"));
        try {
            GCM.getGCMLifeCycleController(r).startFc();
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
    }

    @Test
    public void testCompositeMandatoryInternalClientInterfaceNotBound() throws Exception {
        ComponentType eType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("server", I.class
                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) });
        Component e = gf.newFcInstance(eType, "primitive", C.class.getName());
        ContentController cc = GCM.getContentController(r);
        cc.removeFcSubComponent(d);
        cc.addFcSubComponent(e);
        GCM.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        GCM.getBindingController(c).bindFc("client", e.getFcInterface("server"));
        GCM.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        try {
            GCM.getGCMLifeCycleController(r).startFc();
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
    }

    @Test
    @Ignore
    public void testRemoveNotStopped() throws Exception {
        ContentController cc = GCM.getContentController(r);
        GCM.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        GCM.getBindingController(c).bindFc("client", cc.getFcInternalInterface("client"));
        GCM.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        cc.removeFcSubComponent(d);
        GCM.getGCMLifeCycleController(r).startFc();

        // TODO test issue: adding a sub-component in a started composite automatically starts the added one?
        cc.addFcSubComponent(d);
        //crash here
        //due to org.objectweb.proactive.core.component.control.AbstractPAController.checkLifeCycleIsStopped()
        //first line which impose the composite to be in stopped state
        try {
            cc.removeFcSubComponent(d);
            // fail();
        } catch (IllegalLifeCycleException ilce) {
        }
    }

}
