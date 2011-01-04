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

import functionalTests.component.conform.components.C;


public class TestContentControllerTemplate extends TestContentController {
    @Override
    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, compositeTemplate, null);
        d = gf.newFcInstance(t, compositeTemplate, null);
        e = gf.newFcInstance(t, primitiveTemplate, C.class.getName());
    }

    @Test
    @Ignore
    public void testInstanceContent() throws Exception {
        Component r = gf.newFcInstance(t, compositeTemplate, null);
        GCM.getContentController(r).addFcSubComponent(c);
        GCM.getContentController(r).addFcSubComponent(d);
        GCM.getContentController(c).addFcSubComponent(e);
        GCM.getContentController(d).addFcSubComponent(e);

        Component root = GCM.getFactory(r).newFcInstance();
        Component[] comps = GCM.getContentController(root).getFcSubComponents();
        assertEquals(2, comps.length);
        Component[] cComps = GCM.getContentController(comps[0]).getFcSubComponents();
        Component[] dComps = GCM.getContentController(comps[1]).getFcSubComponents();
        assertEquals(1, cComps.length);
        assertEquals(1, dComps.length);
        assertEquals(cComps[0], dComps[0]);
    }
}
