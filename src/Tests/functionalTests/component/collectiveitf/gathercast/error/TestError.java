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
package functionalTests.component.collectiveitf.gathercast.error;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;

import functionalTests.ComponentTest;


public class TestError extends ComponentTest {

    /**
     *
     */
    public static final String MESSAGE = "-Main-";
    public static final int NB_CONNECTED_ITFS = 2;
    public static final String VALUE_1 = "10";
    public static final String VALUE_2 = "20";

    public TestError() {
        super("Gather interfaces error", "Gather interfaces error");
    }

    /*
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        try {
            Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
            Map<Object, Object> context = new HashMap<Object, Object>();
            f.newComponent("functionalTests.component.collectiveitf.gathercast.error.testcaseerror", context);
        } catch (Exception e) {
        }
    }
}
