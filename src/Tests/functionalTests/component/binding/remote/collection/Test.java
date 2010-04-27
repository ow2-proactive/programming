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
package functionalTests.component.binding.remote.collection;

import java.util.List;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;

import functionalTests.ComponentTestDefaultNodes;
import functionalTests.component.I1Multicast;
import functionalTests.component.I2;
import functionalTests.component.Message;
import functionalTests.component.PrimitiveComponentB;
import functionalTests.component.PrimitiveComponentD;


/**
 * @author The ProActive Team
 * a test for bindings on client collective interfaces between remote components
 */
public class Test extends ComponentTestDefaultNodes {

    /**
     *
     */
    public static String MESSAGE = "-->Main";
    Component pD1;
    Component pB1;
    Component pB2;
    Message message;

    public Test() {
        super(2, 1);
        //        super("Communication between remote primitive components through client collective interface",
        //                "Communication between remote primitive components through client collective interface ");
    }

    /**
     * @see testsuite.test.FunctionalTest#action()
     */
    @org.junit.Test
    public void action() throws Exception {
        Component boot = Utils.getBootstrapComponent();
        GCMTypeFactory type_factory = GCM.getGCMTypeFactory(boot);
        PAGenericFactory cf = Utils.getPAGenericFactory(boot);

        ComponentType D_Type = type_factory.createFcType(new InterfaceType[] {
                type_factory.createFcItfType("i1", I1Multicast.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                type_factory.createFcItfType("i2", I2.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.COLLECTION) });
        ComponentType B_Type = type_factory.createFcType(new InterfaceType[] { type_factory.createFcItfType(
                "i2", I2.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) });

        // instantiate the components
        pD1 = cf.newFcInstance(D_Type, new ControllerDescription("pD1", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentD.class.getName(), new Object[] {}), super
                        .getANode());
        pB1 = cf.newFcInstance(B_Type, new ControllerDescription("pB1", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}));
        pB2 = cf.newFcInstance(B_Type, new ControllerDescription("pB2", Constants.PRIMITIVE),
                new ContentDescription(PrimitiveComponentB.class.getName(), new Object[] {}), super
                        .getANode());

        // bind the components
        GCM.getBindingController(pD1).bindFc("i2-1", pB2.getFcInterface("i2"));
        GCM.getBindingController(pD1).bindFc("i2-2", pB1.getFcInterface("i2"));

        // start them
        GCM.getGCMLifeCycleController(pD1).startFc();
        GCM.getGCMLifeCycleController(pB1).startFc();
        GCM.getGCMLifeCycleController(pB2).startFc();

        message = null;
        I1Multicast i1 = (I1Multicast) pD1.getFcInterface("i1");
        List<Message> msg1 = i1.processInputMessage(new Message(MESSAGE));
        StringBuffer resulting_msg = new StringBuffer();
        for (Message message : msg1) {
            System.err.println("msg: " + message);
            message.append(MESSAGE);
            resulting_msg.append(message);
        }

        // this --> primitiveA --> primitiveB --> primitiveA --> this  (message goes through composite components)
        String single_message = Test.MESSAGE + PrimitiveComponentD.MESSAGE + PrimitiveComponentB.MESSAGE +
            PrimitiveComponentD.MESSAGE + Test.MESSAGE;

        Assert.assertEquals(resulting_msg.toString(), single_message + single_message);
    }
}