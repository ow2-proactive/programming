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
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.C;


public class TestBindingControllerCompositeTemplate extends TestBindingControllerComposite {
    public TestBindingControllerCompositeTemplate() {
        isTemplate = true;
    }

    @Override
    protected void setUpComponents() throws Exception {
        r = gf.newFcInstance(t, compositeTemplate, null);
        c = gf.newFcInstance(t, primitiveTemplate, C.class.getName());
        d = gf.newFcInstance(t, compositeTemplate, null);
        e = gf.newFcInstance(u, compositeTemplate, null);
        Fractal.getContentController(r).addFcSubComponent(c);
        Fractal.getContentController(r).addFcSubComponent(d);
        Fractal.getContentController(r).addFcSubComponent(e);
    }

    @Test
    @Ignore
    public void testInstanceBinding() throws Exception {
        ContentController cc = Fractal.getContentController(r);
        Fractal.getContentController(e).addFcSubComponent(c);

        Fractal.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        Fractal.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        Fractal.getBindingController(d).bindFc("client", cc.getFcInternalInterface("client"));

        Fractal.getBindingController(r).bindFc("servers0", c.getFcInterface("servers0"));
        Fractal.getBindingController(c).bindFc("clients0", d.getFcInterface("servers0"));
        Fractal.getBindingController(d).bindFc("clients0", cc.getFcInternalInterface("clients0"));

        Component rComp = Fractal.getFactory(r).newFcInstance();

        cc = Fractal.getContentController(rComp);
        Component[] comps = cc.getFcSubComponents();

        Component cComp = comps[0];
        Component dComp = comps[1];

        assertEquals(Fractal.getBindingController(rComp).lookupFc("server"), cComp.getFcInterface("server"));
        assertEquals(Fractal.getBindingController(cComp).lookupFc("client"), dComp.getFcInterface("server"));
        assertEquals(Fractal.getBindingController(dComp).lookupFc("client"), cc
                .getFcInternalInterface("client"));

        assertEquals(Fractal.getBindingController(rComp).lookupFc("servers0"), cComp
                .getFcInterface("servers0"));
        assertEquals(Fractal.getBindingController(cComp).lookupFc("clients0"), dComp
                .getFcInterface("servers0"));
        assertEquals(Fractal.getBindingController(dComp).lookupFc("clients0"), cc
                .getFcInternalInterface("clients0"));
    }
}
