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
package functionalTests.component.conform;

import static org.junit.Assert.assertEquals;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.ContentController;

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
        GCM.getContentController(r).addFcSubComponent(c);
        GCM.getContentController(r).addFcSubComponent(d);
        GCM.getContentController(r).addFcSubComponent(e);
    }

    @Test
    @Ignore
    public void testInstanceBinding() throws Exception {
        ContentController cc = GCM.getContentController(r);
        GCM.getContentController(e).addFcSubComponent(c);

        GCM.getBindingController(r).bindFc("server", c.getFcInterface("server"));
        GCM.getBindingController(c).bindFc("client", d.getFcInterface("server"));
        GCM.getBindingController(d).bindFc("client", cc.getFcInternalInterface("client"));

        GCM.getBindingController(r).bindFc("servers0", c.getFcInterface("servers0"));
        GCM.getBindingController(c).bindFc("clients0", d.getFcInterface("servers0"));
        GCM.getBindingController(d).bindFc("clients0", cc.getFcInternalInterface("clients0"));

        Component rComp = GCM.getFactory(r).newFcInstance();

        cc = GCM.getContentController(rComp);
        Component[] comps = cc.getFcSubComponents();

        Component cComp = comps[0];
        Component dComp = comps[1];

        assertEquals(GCM.getBindingController(rComp).lookupFc("server"), cComp.getFcInterface("server"));
        assertEquals(GCM.getBindingController(cComp).lookupFc("client"), dComp.getFcInterface("server"));
        assertEquals(GCM.getBindingController(dComp).lookupFc("client"), cc.getFcInternalInterface("client"));

        assertEquals(GCM.getBindingController(rComp).lookupFc("servers0"), cComp.getFcInterface("servers0"));
        assertEquals(GCM.getBindingController(cComp).lookupFc("clients0"), dComp.getFcInterface("servers0"));
        assertEquals(GCM.getBindingController(dComp).lookupFc("clients0"), cc
                .getFcInternalInterface("clients0"));
    }
}
