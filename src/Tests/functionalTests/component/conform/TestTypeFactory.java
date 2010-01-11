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

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;

import functionalTests.component.conform.components.I;


public class TestTypeFactory extends Conformtest {
    protected Component boot;
    protected TypeFactory tf;
    protected GenericFactory gf;

    @Before
    public void setUp() throws Exception {
        boot = Fractal.getBootstrapComponent();
        tf = Fractal.getTypeFactory(boot);
        gf = Fractal.getGenericFactory(boot);
    }

    // -------------------------------------------------------------------------
    // Test interface types
    // -------------------------------------------------------------------------
    @Test
    public void testInterfaceType() throws Exception {
        tf.createFcItfType("i", I.class.getName(), false, false, false);
        tf.createFcItfType("i", I.class.getName(), true, false, false);
    }

    @Test
    public void testNoSuchClass() {
        try {
            // no such class
            Type t = tf.createFcItfType("i", "xyz", false, false, false);
            gf.newFcInstance(t, "composite", null);
            fail();
        } catch (InstantiationException e) {
        }
    }

    @Test
    public void testNotAnInterface() {
        try {
            // not an interface
            Type t = tf.createFcItfType("i", TestTypeFactory.class.getName(), false, false, false);
            gf.newFcInstance(t, "composite", null);
            fail();
        } catch (InstantiationException e) {
        }
    }

    // -------------------------------------------------------------------------
    // Test component types
    // -------------------------------------------------------------------------
    @Test
    public void testComponentType() throws Exception {
        InterfaceType sType = tf.createFcItfType("s", I.class.getName(), false, false, false);
        InterfaceType i1Type = tf.createFcItfType("i1", I.class.getName(), true, false, false);
        InterfaceType i2Type = tf.createFcItfType("i2", I.class.getName(), true, false, false);
        tf.createFcType(null);
        tf.createFcType(new InterfaceType[] { sType });
        tf.createFcType(new InterfaceType[] { i1Type, i2Type });
        tf.createFcType(new InterfaceType[] { sType, i1Type, i2Type });
    }

    @Test
    public void testBadPrefixes() {
        try {
            // bad prefixes
            tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("i", I.class.getName(), false, false, false),
                    tf.createFcItfType("i", I.class.getName(), true, false, false) });
            fail();
        } catch (InstantiationException e) {
        }
        try {
            // bad prefixes
            tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("i", I.class.getName(), true, true, true),
                    tf.createFcItfType("ij", I.class.getName(), true, true, true) });
            fail();
        } catch (InstantiationException e) {
        }
    }
}
