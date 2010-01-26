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
//@snippet-start api_starter_Main_skeleton
//@snippet-start api_starter_Main
//@tutorial-start
package org.objectweb.proactive.examples.userguide.components.api.starter;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;


/**
 * @author The ProActive Team
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // TODO: Get the Bootstrap Component
        //@tutorial-break
        //@snippet-break api_starter_Main_skeleton
        Component boot = Fractal.getBootstrapComponent();
        //@snippet-resume api_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Get the TypeFactory
        //@tutorial-break
        //@snippet-break api_starter_Main_skeleton
        TypeFactory tf = Fractal.getTypeFactory(boot);
        //@snippet-resume api_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Get the GenericFactory
        //@tutorial-break
        //@snippet-break api_starter_Main_skeleton
        GenericFactory gf = Fractal.getGenericFactory(boot);
        //@snippet-resume api_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Create the i1 Interface Type (org.objectweb.proactive.examples.userguide.components.api.starter.Itf1)
        //@tutorial-break
        //@snippet-break api_starter_Main_skeleton
        InterfaceType itf1Slave = tf.createFcItfType("i1", Itf1.class.getName(), TypeFactory.SERVER,
                TypeFactory.MANDATORY, TypeFactory.SINGLE);
        //@snippet-resume api_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Create the Slave Component type
        //@tutorial-break
        //@snippet-break api_starter_Main_skeleton
        ComponentType tSlave = tf.createFcType(new InterfaceType[] { itf1Slave });
        //@snippet-resume api_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Create the Slave Component
        //@tutorial-break
        //@snippet-break api_starter_Main_skeleton
        Component slave = gf.newFcInstance(tSlave, new ControllerDescription("slave", Constants.PRIMITIVE),
                SlaveImpl.class.getName());
        //@snippet-resume api_starter_Main_skeleton
        //@tutorial-resume

        Fractal.getLifeCycleController(slave).startFc();

        Itf1 itf1 = (Itf1) slave.getFcInterface("i1");
        List<String> arg = new ArrayList<String>();
        arg.add("hello");
        arg.add("world");
        itf1.compute(arg);

        Fractal.getLifeCycleController(slave).stopFc();

        System.exit(0);
    }
}
//@tutorial-end
//@snippet-end api_starter_Main_skeleton
//@snippet-end api_starter_Main
