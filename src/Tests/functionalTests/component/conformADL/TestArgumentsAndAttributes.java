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
package functionalTests.component.conformADL;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;

import functionalTests.ComponentTest;
import functionalTests.component.conformADL.components.Action;


/**
 * This test instantiates a component from the "dummy.fractal" definition, which is parameterized
 * with the "message" argument.
 * The "message" argument is then used to set the "info" attribute in the dummy component.
 *
 * @author The ProActive Team
 */
public class TestArgumentsAndAttributes extends ComponentTest {

    /**
     *
     */
    Component dummy;

    public TestArgumentsAndAttributes() {
        super("Configuration with ADL arguments and AttributeController",
                "Configuration with ADL arguments and AttributeController");
    }

    /*
     * (non-Javadoc)
     * 
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    @SuppressWarnings("unchecked")
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map context = new HashMap();
        context.put("message", "hello world");
        dummy = (Component) f.newComponent("functionalTests.component.conformADL.components.dummy", context);
        GCM.getGCMLifeCycleController(dummy).startFc();

        Assert.assertEquals("This component is storing the info : hello world", ((Action) dummy
                .getFcInterface("action")).doSomething());
    }
}
