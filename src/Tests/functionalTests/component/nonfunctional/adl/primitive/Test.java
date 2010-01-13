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
package functionalTests.component.nonfunctional.adl.primitive;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.representative.ProActiveNFComponentRepresentative;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;


/**
 *
 * Test for creation of a primitive non-functional component with ADL 
 * @author The ProActive Team
 */
public class Test extends ComponentTest {
    Component root;
    ComponentType type;

    public Test() {
        super("Basic creation of non-functional components", "Basic creation of non-functional components");
    }

    @org.junit.Test
    public void action() throws Exception {
        Factory f = org.objectweb.proactive.core.component.adl.FactoryFactory.getNFFactory();
        @SuppressWarnings("unchecked")
        Map context = new HashMap();

        root = (Component) f.newComponent("functionalTests.component.nonfunctional.adl.dummyPrimitive",
                context);
        Fractal.getLifeCycleController(root).startFc();

        DummyControllerItf ref = (DummyControllerItf) root.getFcInterface("dummy-membrane");
        ref.dummyMethodWithResult();
        ref.dummyVoidMethod("Message");
        Assert.assertTrue(root instanceof ProActiveNFComponentRepresentative);
    }
}
