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
 * $PROACTIVE_INITIAL_DEV$
 */
package functionalTests.component.creation.parallel;

import java.util.HashMap;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.PAFactory;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;

import functionalTests.ComponentTest;


/**
 * Test for creating components in parallel.
 * 
 * @author The ProActive Team
 */
public class TestParallelCreation extends ComponentTest {
    private static final int NB_COMPONENTS = 100;

    @Test
    public void testCreateComponentsInParallelWithAPI() throws Exception {
        Component bootstrap = Utils.getBootstrapComponent();
        GCMTypeFactory typeFactory = GCM.getGCMTypeFactory(bootstrap);
        PAGenericFactory genericFactory = (PAGenericFactory) GCM.getGenericFactory(bootstrap);

        Component[] components = genericFactory.newFcInstanceInParallel(typeFactory
                .createFcType(new InterfaceType[] { typeFactory.createFcItfType(Service.SERVER_ITF_NAME,
                        Service.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE) }), new ControllerDescription("slave", Constants.PRIMITIVE),
                new ContentDescription(SlaveImpl.class.getName()), NB_COMPONENTS);

        testComponents(components);
    }

    @Test
    public void testCreateComponentsInParallelWithADL() throws Exception {
        PAFactory factory = (PAFactory) FactoryFactory.getFactory();

        Object[] components = factory.newComponentsInParallel(
                "functionalTests.component.creation.parallel.adl.Slave", new HashMap<String, Object>(),
                NB_COMPONENTS);

        testComponents(components);
    }

    @Test
    public void testCreateCompositesInParallelWithADL() throws Exception {
        PAFactory factory = (PAFactory) FactoryFactory.getFactory();

        Object[] components = factory.newComponentsInParallel(
                "functionalTests.component.creation.parallel.adl.Composite", new HashMap<String, Object>(),
                NB_COMPONENTS);

        testComponents(components);
    }

    private void testComponents(Object[] components) throws Exception {
        Assert.assertNotNull(components);
        Assert.assertEquals(NB_COMPONENTS, components.length);

        for (int i = 0; i < components.length; i++) {
            Component composite = (Component) components[i];
            Assert.assertNotNull(composite);
            GCM.getGCMLifeCycleController(composite).startFc();

            Service service = (Service) composite.getFcInterface(Service.SERVER_ITF_NAME);

            Assert.assertEquals("hello", service.sayHello());
        }
    }
}
