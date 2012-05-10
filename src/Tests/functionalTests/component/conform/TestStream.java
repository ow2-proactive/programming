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

import static org.junit.Assert.fail;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Utils;

import functionalTests.component.conform.components.I;
import functionalTests.component.conform.components.ItfWithStream;
import functionalTests.component.conform.components.ItfWithStreamError;
import functionalTests.component.conform.components.ItfWithStreamInherited;
import functionalTests.component.conform.components.ItfWithStreamInheritedError;
import functionalTests.component.conform.components.StreamImpl;


public class TestStream extends Conformtest {
    protected Component boot;
    protected GCMTypeFactory tf;
    protected GenericFactory gf;
    protected InterfaceType it;

    @Before
    public void setUp() throws Exception {
        boot = Utils.getBootstrapComponent();
        tf = GCM.getGCMTypeFactory(boot);
        gf = GCM.getGenericFactory(boot);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which not extend StreamInterface
    // -------------------------------------------------------------------------
    @Test
    public void testNoStreamItf() throws Exception {
        it = tf.createFcItfType("server", I.class.getName(), false, false, false);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface
    // -------------------------------------------------------------------------
    @Test
    public void testStreamItf() throws Exception {
        it = tf.createFcItfType("server", ItfWithStream.class.getName(), false, false, false);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface by inheritance
    // -------------------------------------------------------------------------
    @Test
    public void testStreamItfInherited() throws Exception {
        it = tf.createFcItfType("server", ItfWithStreamInherited.class.getName(), false, false, false);
    }

    // -------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface with error
    // -------------------------------------------------------------------------
    @Test
    public void testStreamItfError() throws Exception {
        try {
            it = tf.createFcItfType("server", ItfWithStreamError.class.getName(), false, false, false);
            fail();
        } catch (InstantiationException e) {
        }
    }

    // -----------------------------------------------------------------------------------
    // Type interface creation which extend StreamInterface by inheritance with error
    // -----------------------------------------------------------------------------------
    @Test
    public void testStreamItfInheritedError() throws Exception {
        try {
            it = tf.createFcItfType("server", ItfWithStreamInheritedError.class.getName(), false, false,
                    false);
            fail();
        } catch (InstantiationException e) {
        }
    }

    // -----------------------------------------------------------------------------------
    // Full test
    // -----------------------------------------------------------------------------------
    @Test
    public void testExecStreamItf() throws Exception {
        try {
            ComponentType t = tf.createFcType(new InterfaceType[] { tf.createFcItfType("server",
                    ItfWithStream.class.getName(), false, false, false) });
            Component c = gf.newFcInstance(t, parametricPrimitive, StreamImpl.class.getName());
            GCM.getGCMLifeCycleController(c).startFc();
            ItfWithStream iws = (ItfWithStream) c.getFcInterface("server");
            iws.hello();
            iws.hello("world");
        } catch (InstantiationException e) {
        }
    }
}
