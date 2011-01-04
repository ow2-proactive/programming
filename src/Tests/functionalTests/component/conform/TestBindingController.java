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
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Utils;

import functionalTests.component.conform.components.C;
import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.J;


public class TestBindingController extends Conformtest {
    protected Component boot;
    protected GCMTypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType t;
    protected ComponentType u;
    protected Component c;
    protected Component d;
    protected Component e;
    protected boolean isTemplate;

    // -------------------------------------------------------------------------
    // Constructor and setup
    // -------------------------------------------------------------------------
    public TestBindingController() {
        super();
    }

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
        u = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("serverI", I.class.getName(), TypeFactory.SERVER, TypeFactory.OPTIONAL,
                        TypeFactory.SINGLE),
                tf.createFcItfType("serverJ", J.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE), });
        setUpComponents();
    }

    protected void setUpComponents() throws Exception {
        c = gf.newFcInstance(t, flatPrimitive, C.class.getName());
        d = gf.newFcInstance(t, flatPrimitive, C.class.getName());
        e = gf.newFcInstance(u, flatPrimitive, C.class.getName());
    }

    // -------------------------------------------------------------------------
    // Test list, lookup, bind, unbind
    // -------------------------------------------------------------------------
    @Test
    public void testList() throws Exception {
        BindingController bc = GCM.getBindingController(c);
        checkList(bc, new String[] { "client" });
    }

    @Test
    public void testBindLookupUnbind() throws Exception {
        BindingController bc = GCM.getBindingController(c);
        bc.bindFc("client", d.getFcInterface("server"));
        checkList(bc, new String[] { "client" });
        assertEquals(d.getFcInterface("server"), bc.lookupFc("client"));
        bc.unbindFc("client");
        assertEquals(null, bc.lookupFc("client"));
    }

    @Test
    public void testCollectionBindLookupUnbind() throws Exception {
        BindingController bc = GCM.getBindingController(c);
        bc.bindFc("clients0", d.getFcInterface("server"));
        checkList(bc, new String[] { "client", "clients0" });
        assertEquals(d.getFcInterface("server"), bc.lookupFc("clients0"));
        bc.unbindFc("clients0");
        try {
            assertEquals(null, bc.lookupFc("clients0"));
        } catch (NoSuchInterfaceException e) {
            checkList(bc, new String[] { "client" });
        }
    }

    protected void checkList(BindingController bc, String[] expected) {
        String[] names = bc.listFc();
        HashSet<String> nameSet = new HashSet<String>();
        for (int i = 0; i < names.length; ++i) {
            String name = names[i];
            if (!nameSet.add(name)) {
                fail("Duplicated interface name: " + name);
            }
        }
        assertEquals(new HashSet<String>(Arrays.asList(expected)), nameSet);
    }

    // -------------------------------------------------------------------------
    // Test errors in lookup, bind, unbind
    // -------------------------------------------------------------------------
    @Test
    public void testNoSuchInterfaceLookup() throws Exception {
        try {
            GCM.getBindingController(c).lookupFc("c");
            fail();
        } catch (NoSuchInterfaceException e) {
        }
    }

    @Test
    public void testNoSuchInterfaceBind() throws Exception {
        try {
            GCM.getBindingController(c).bindFc("c", d.getFcInterface("server"));
            fail();
        } catch (NoSuchInterfaceException e) {
        }
    }

    @Test
    @Ignore
    public void testNotAServerInterface() throws Exception {
        try {
            GCM.getBindingController(c).bindFc("client", c.getFcInterface("client"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    public void testWrongType() throws Exception {
        try {
            GCM.getBindingController(c).bindFc("client", e.getFcInterface("serverJ"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    @Ignore
    public void testMandatoryToOptional() throws Exception {
        try {
            GCM.getBindingController(c).bindFc("client", e.getFcInterface("serverI"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    public void testAlreadyBound() throws Exception {
        BindingController bc = GCM.getBindingController(c);
        bc.bindFc("client", d.getFcInterface("server"));
        try {
            bc.bindFc("client", d.getFcInterface("server"));
            fail();
        } catch (IllegalBindingException e) {
        }
        bc.bindFc("clients0", d.getFcInterface("server"));
        try {
            bc.bindFc("clients0", d.getFcInterface("server"));
            fail();
        } catch (IllegalBindingException e) {
        }
    }

    @Test
    public void testNoSuchInterfaceUnind() throws Exception {
        try {
            GCM.getBindingController(c).unbindFc("c");
            fail();
        } catch (NoSuchInterfaceException e) {
        }
    }

    @Test
    public void testNotBound() throws Exception {
        try {
            GCM.getBindingController(c).unbindFc("client");
            fail();
        } catch (IllegalBindingException e) {
        }
    }
}
