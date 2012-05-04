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
package functionalTests.component.nonfunctional.creation.nftype.internalclient;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;

import functionalTests.ComponentTest;
import functionalTests.component.creation.ComponentInfo;


/**
 * @author The ProActive Team
 *
 *Creates a functional component, by defining among its parameters its non-functional type
 */
public class Test extends ComponentTest {
    Component componentA;
    String name;
    String nodeUrl;

    public Test() {
        super("Creation of a composite functional component with internal non-functional client interface",
                "Creation of a composite functional component with internal non-functional client interface");
    }

    @org.junit.Test
    public void action() throws Exception {
        Component boot = Utils.getBootstrapComponent(); /*Getting the Fractal-Proactive bootstrap component*/
        PAGCMTypeFactory type_factory = Utils.getPAGCMTypeFactory(boot); /*Getting the GCM-ProActive type factory*/
        PAGenericFactory cf = Utils.getPAGenericFactory(boot); /*Getting the GCM-ProActive generic factory*/

        InterfaceType[] fItfTypes = new InterfaceType[] { type_factory.createFcItfType("componentInfo",
                ComponentInfo.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), };

        InterfaceType[] nfItfTypes = new InterfaceType[] {
                type_factory
                        .createFcItfType(
                                Constants.BINDING_CONTROLLER,
                                /* BINDING CONTROLLER */org.objectweb.proactive.core.component.control.PABindingController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory
                        .createFcItfType(
                                Constants.CONTENT_CONTROLLER,
                                /* CONTENT CONTROLLER */org.objectweb.proactive.core.component.control.PAContentController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory
                        .createFcItfType(
                                Constants.LIFECYCLE_CONTROLLER,
                                /* LIFECYCLE CONTROLLER */org.objectweb.proactive.core.component.control.PAGCMLifeCycleController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory.createFcItfType(Constants.SUPER_CONTROLLER,
                /* SUPER CONTROLLER */org.objectweb.proactive.core.component.control.PASuperController.class
                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType(Constants.NAME_CONTROLLER,
                /* NAME CONTROLLER */org.objectweb.fractal.api.control.NameController.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory
                        .createFcItfType(
                                Constants.MULTICAST_CONTROLLER,
                                /* MULTICAST CONTROLLER */org.objectweb.proactive.core.component.control.PAMulticastController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory.createFcItfType(Constants.GATHERCAST_CONTROLLER,
                        /* GATHERCAST CONTROLLER */org.etsi.uri.gcm.api.control.GathercastController.class
                                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory
                        .createFcItfType(
                                Constants.MIGRATION_CONTROLLER,
                                /* MIGRATION CONTROLLER */org.objectweb.proactive.core.component.control.PAMigrationController.class
                                        .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                                TypeFactory.SINGLE),
                type_factory
                        .createGCMItfType(
                                "dummy-internal-client-controller",
                                /* DUMMY CONTROLLER */functionalTests.component.nonfunctional.creation.DummyControllerItf.class
                                        .getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                                GCMTypeFactory.SINGLETON_CARDINALITY, PAGCMTypeFactory.INTERNAL) };

        Type type = type_factory.createFcType(fItfTypes, nfItfTypes);

        componentA = cf.newFcInstance(type, new ControllerDescription("componentA", Constants.COMPOSITE,
            !Constants.SYNCHRONOUS, Constants.WITHOUT_CONFIG_FILE), null);

    }

    /**
     * @see testsuite.test.AbstractTest#initTest()
     */
    public void initTest() throws Exception {
    }

    /**
     * @see testsuite.test.AbstractTest#endTest()
     */
    public void endTest() throws Exception {

    }

    public boolean postConditions() throws Exception {
        return (componentA instanceof PAComponentRepresentative);
    }
}
