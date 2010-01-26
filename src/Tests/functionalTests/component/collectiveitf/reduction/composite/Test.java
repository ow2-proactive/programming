/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package functionalTests.component.collectiveitf.reduction.composite;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;

import functionalTests.ComponentTest;


public class Test extends ComponentTest {
    public Test() {
        super("Multicast reduction mixing composite and primitive components",
                "Multicast reduction mixing composite and primitive components");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();

            Map<String, Object> context = new HashMap<String, Object>();

            Component root = (Component) f.newComponent(
                    "functionalTests.component.collectiveitf.reduction.composite.adl.testcase", context);
            Fractal.getLifeCycleController(root).startFc();
            Reduction reductionItf = ((Reduction) root.getFcInterface("mcast"));

            IntWrapper rval = reductionItf.doIt();
            Assert.assertEquals(new IntWrapper(123), rval);

            rval = reductionItf.doItInt(new IntWrapper(321));
            Assert.assertEquals(new IntWrapper(123), rval);

            reductionItf.voidDoIt();
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
