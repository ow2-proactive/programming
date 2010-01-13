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
package functionalTests.component.collectiveitf.dynamicdispatch;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import functionalTests.ComponentTest;


public class Test extends ComponentTest {
    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;

    public Test() {
        super("Multicast reduction for components", "Multicast reduction for components");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();

        Component testCase = (Component) f.newComponent(
                "functionalTests.component.collectiveitf.dynamicdispatch.testcase", context);
        Fractal.getLifeCycleController(testCase).startFc();
        boolean result = ((RunnerItf) testCase.getFcInterface("runTestItf")).runTest();
        Assert.assertTrue(result);

        //        Component testcase = (Component) f.newComponent("functionalTests.component.collectiveitf.multicast.testcase",
        //                context);

        //        Fractal.getLifeCycleController(testcase).startFc();
        //        ((Tester) testcase.getFcInterface("runTestItf")).testConnectedServerMulticastItf();
        //        ((Tester) testcase.getFcInterface("runTestItf")).testOwnClientMulticastItf();
    }
}
