/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package functionalTests.component.nonfunctional.membranecontroller.content;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.creation.DummyControllerComponentImpl;
import functionalTests.component.nonfunctional.creation.DummyControllerItf;
import functionalTests.component.nonfunctional.membranecontroller.DummyFunctionalComponentImpl;
import functionalTests.component.nonfunctional.membranecontroller.DummyFunctionalItf;


/**
 * @author The ProActive Team
 *
 * Testing adding and getting a reference on a non-functional component
 */
public class Test extends ComponentTest {
    Component dummyNFComponent;
    Component dummyFComponent;
    String name;
    String nodeUrl;

    public Test() {
        super("Adding non-functional component inside the membrane,then getting a reference",
                "Test Adding non-functional component inside the membrane, then getting a reference");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        // Thread.sleep(2000);
        Component boot = Utils.getBootstrapComponent(); /*Getting the Fractal-Proactive bootstrap component*/
        GCMTypeFactory type_factory = GCM.getGCMTypeFactory(boot); /*Getting the GCM-ProActive type factory*/
        PAGenericFactory cf = Utils.getPAGenericFactory(boot); /*Getting the GCM-ProActive generic factory*/

        dummyNFComponent = cf.newNfFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("dummy-controller-membrane", DummyControllerItf.class.getName(),
                        TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), }),
                new ControllerDescription("dummyController", Constants.PRIMITIVE), new ContentDescription(
                    DummyControllerComponentImpl.class.getName()));

        logger.debug("NF component created");
        dummyFComponent = cf.newFcInstance(type_factory.createFcType(new InterfaceType[] { type_factory
                .createFcItfType("dummy-functional", DummyFunctionalItf.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE), }), new ControllerDescription(
            "dummyFunctional", Constants.PRIMITIVE, getClass().getResource(
                    "/functionalTests/component/nonfunctional/membranecontroller/content/config.xml")
                    .getPath()), new ContentDescription(DummyFunctionalComponentImpl.class.getName()));

        Utils.getPAMembraneController(dummyFComponent).nfAddFcSubComponent(dummyNFComponent);
        Component[] components = Utils.getPAMembraneController(dummyFComponent).nfGetFcSubComponents();
        System.out.println("Name : " + GCM.getNameController(components[0]).getFcName());
        Utils.getPAMembraneController(dummyFComponent).nfStartFc("dummyController");
        Utils.getPAMembraneController(dummyFComponent).nfStopFc("dummyController");
        System.out.println("Lifecycle state :" +
            Utils.getPAMembraneController(dummyFComponent).nfGetFcState("dummyController"));
        Utils.getPAMembraneController(dummyFComponent).nfRemoveFcSubComponent(dummyNFComponent);
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
        //GCM.getGCMLifeCycleController(dummyNFComponent).stopFc();
    }

    public boolean postConditions() throws Exception {
        return /*(dummyNFComponent  instanceof ProActiveNFComponentRepresentative)*/true;
    }

}
