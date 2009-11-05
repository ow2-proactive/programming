/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.component.collectiveitf.multicast;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;

import functionalTests.ComponentTest;


public class Test extends ComponentTest {

    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;

    public Test() {
        super("Multicast invocations for components", "Multicast invocations for components");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();

        // simple test first
        Component simpleTestCase = (Component) f.newComponent(
                "functionalTests.component.collectiveitf.multicast.simple.testcase", context);
        Fractal.getLifeCycleController(simpleTestCase).startFc();
        ((Tester) simpleTestCase.getFcInterface("runTestItf")).testOwnClientMulticastItf();

        // more complex testcase now
        Component testcase = (Component) f.newComponent(
                "functionalTests.component.collectiveitf.multicast.testcase", context);

        Fractal.getLifeCycleController(testcase).startFc();
        ((Tester) testcase.getFcInterface("runTestItf")).testConnectedServerMulticastItf();
        ((Tester) testcase.getFcInterface("runTestItf")).testOwnClientMulticastItf();
    }

    @org.junit.Test
    public void testMulticastServerItfNotBound() throws Exception {
        Component boot = Fractal.getBootstrapComponent();
        ProActiveTypeFactory tf = (ProActiveTypeFactory) Fractal.getTypeFactory(boot);
        GenericFactory gf = Fractal.getGenericFactory(boot);
        ComponentType ct = tf.createFcType(new InterfaceType[] { tf.createFcItfType("serverMult",
                MulticastTestItf.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                ProActiveTypeFactory.MULTICAST_CARDINALITY) });
        Component composite = gf.newFcInstance(ct, "composite", null);
        try {
            Fractal.getLifeCycleController(composite).startFc();
            fail();
        } catch (IllegalLifeCycleException ilce) {
        }
    }
}
