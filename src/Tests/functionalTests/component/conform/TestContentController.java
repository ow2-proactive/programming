/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

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

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;


public class TestContentController extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected Component c;
    protected Component d;
    protected Component e;

    // -------------------------------------------------------------------------
    // Constructor ans setup
    // -------------------------------------------------------------------------

    //  public TestContentController (final String name) {
    //    super(name);
    //  }
    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
        t = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("server", I.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.MANDATORY),
                tf.createFcItfType("client", I.class.getName(), TypeFactory.CLIENT, TypeFactory.OPTIONAL,
                        TypeFactory.MANDATORY) });
        setUpComponents();
    }

    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, "composite", null);
        d = gf.newFcInstance(t, "composite", null);
        e = gf.newFcInstance(t, "primitive", C.class.getName());
    }

    // -------------------------------------------------------------------------
    // Test add and remove
    // -------------------------------------------------------------------------
    @Test
    public void testAddAndRemove() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        assertTrue(Arrays.asList(cc.getFcSubComponents()).contains(e));
        cc.removeFcSubComponent(e);
        assertTrue(!Arrays.asList(cc.getFcSubComponents()).contains(e));
    }

    // -------------------------------------------------------------------------
    // Test add errors
    // -------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testAlreadySubComponent() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        cc.addFcSubComponent(e);
    }

    @Test(expected = IllegalContentException.class)
    @Ignore
    public void testWouldCreateCycle1() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(c);
    }

    @Test(expected = IllegalContentException.class)
    @Ignore
    public void testWouldCreateCycle2() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        ContentController cd = Fractal.getContentController(d);
        cc.addFcSubComponent(d);
        cd.addFcSubComponent(c);
    }

    // -------------------------------------------------------------------------
    // Test remove errors
    // -------------------------------------------------------------------------
    @Test(expected = IllegalContentException.class)
    public void testNotASubComponent() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        // must throw an IllegalContentException
        cc.removeFcSubComponent(d);
    }

    @Test(expected = IllegalContentException.class)
    public void testWouldCreateNonLocalExportBinding() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        Fractal.getBindingController(c).bindFc("server", e.getFcInterface("server"));
        // must throw an IllegalContentException
        cc.removeFcSubComponent(e);
    }

    @Test(expected = IllegalContentException.class)
    public void testWouldCreateNonLocalImportBinding() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(e);
        Fractal.getBindingController(e).bindFc("client", cc.getFcInternalInterface("client"));
        cc.removeFcSubComponent(e);
    }

    @Test(expected = IllegalContentException.class)
    public void testWouldCreateNonLocalNormalBinding() throws Exception {
        ContentController cc = Fractal.getContentController(c);
        cc.addFcSubComponent(d);
        cc.addFcSubComponent(e);
        Fractal.getBindingController(d).bindFc("client", e.getFcInterface("server"));
        cc.removeFcSubComponent(e);
    }
}
