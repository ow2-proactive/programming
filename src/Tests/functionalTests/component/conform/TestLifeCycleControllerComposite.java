/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package functionalTests.component.conform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.util.Fractal;

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
        Fractal.getContentController(o).addFcSubComponent(r);
        Fractal.getContentController(r).addFcSubComponent(c);
        Fractal.getContentController(r).addFcSubComponent(d);
    }

    @Test
    public void testRecursiveStartStop() throws Exception {
        ContentController cc = Fractal.getContentController(r);
        Fractal.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        Fractal.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        Fractal.getBindingController(d).bindFc("client", cc.getFcInternalInterface("client"));
        Fractal.getBindingController(r).bindFc("client", r.getFcInterface("server"));

        Fractal.getLifeCycleController(r).startFc();
        assertEquals("STARTED", Fractal.getLifeCycleController(r).getFcState());
        assertEquals("STARTED", Fractal.getLifeCycleController(c).getFcState());
        assertEquals("STARTED", Fractal.getLifeCycleController(d).getFcState());
        final I i = (I) r.getFcInterface("server");
        Thread t = new Thread(new Runnable() {
            public void run() {
                i.m(true);
            }
        });
        t.start();
        t.join(50);
        assertTrue(!t.isAlive());

        Fractal.getLifeCycleController(r).stopFc();
        assertEquals("STOPPED", Fractal.getLifeCycleController(r).getFcState());
        assertEquals("STOPPED", Fractal.getLifeCycleController(c).getFcState());
        assertEquals("STOPPED", Fractal.getLifeCycleController(d).getFcState());
    }

    @Override
    @Test
    public void testMandatoryInterfaceNotBound() throws Exception {
        super.testMandatoryInterfaceNotBound();
        ContentController cc = Fractal.getContentController(r);
        cc.removeFcSubComponent(c);
        cc.removeFcSubComponent(d);
        Fractal.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        try {
            Fractal.getLifeCycleController(c).startFc();
            fail();
        } catch (IllegalLifeCycleException e) {
        }
    }

    @Test
    public void testCompositeMandatoryServerInterfaceNotBound() throws Exception {
        Fractal.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        Fractal.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        Fractal.getBindingController(d).bindFc("client", c.getFcInterface("server"));
        try {
            Fractal.getLifeCycleController(r).startFc();
            fail();
        } catch (IllegalLifeCycleException e) {
        }
    }

    @Test
    @Ignore
    public void testRemoveNotStopped() throws Exception {
        ContentController cc = Fractal.getContentController(r);
        Fractal.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        Fractal.getBindingController(c).bindFc("client", cc.getFcInternalInterface("client"));
        Fractal.getBindingController(r).bindFc("client", r.getFcInterface("server"));
        cc.removeFcSubComponent(d);
        Fractal.getLifeCycleController(r).startFc();

        // TODO test issue: adding a sub-component in a started composite automatically starts the added one?
        cc.addFcSubComponent(d);
        //crash here
        //due to org.objectweb.proactive.core.component.controller.AbstractProActiveController.checkLifeCycleIsStopped() 
        //first line which impose the composite to be in stopped state
        try {
            cc.removeFcSubComponent(d);
            // fail();
        } catch (IllegalLifeCycleException e) {
        }
    }

}
