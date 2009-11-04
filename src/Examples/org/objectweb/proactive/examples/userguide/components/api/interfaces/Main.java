/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
//@snippet-start api_interfaces_Main_skeleton
//@snippet-start api_interfaces_Main
package org.objectweb.proactive.examples.userguide.components.api.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
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
        Component boot = Fractal.getBootstrapComponent();
        TypeFactory tf = Fractal.getTypeFactory(boot);
        GenericFactory gf = Fractal.getGenericFactory(boot);
        ComponentType tComposite = tf.createFcType(new InterfaceType[] { tf.createFcItfType("runner",
                Runner.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE) });
        ComponentType tMaster = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("runner", Runner.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType("i1", Itf1.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
                //TODO: Add the new client interface
                //@tutorial-break
                //@snippet-break api_interfaces_Main_skeleton
                ,
                tf.createFcItfType("i2", Itf2.class.getName(), TypeFactory.CLIENT, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
        //@snippet-resume api_interfaces_Main_skeleton
                //@tutorial-resume
                });
        ComponentType tSlave = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("i1", Itf1.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
                //TODO: Add the new server interface
                //@tutorial-break
                //@snippet-break api_interfaces_Main_skeleton
                ,
                tf.createFcItfType("i2", Itf2.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                        TypeFactory.SINGLE)
        //@snippet-resume api_interfaces_Main_skeleton
                //@tutorial-resume
                });
        Component slave = gf.newFcInstance(tSlave, new ControllerDescription("slave", Constants.PRIMITIVE),
                SlaveImpl.class.getName());
        Component master = gf.newFcInstance(tMaster,
                new ControllerDescription("master", Constants.PRIMITIVE), MasterImpl.class.getName());
        Component composite = gf.newFcInstance(tComposite, new ControllerDescription("composite",
            Constants.COMPOSITE), null);

        BindingController bcComposite = Fractal.getBindingController(composite);
        bcComposite.bindFc("runner", master.getFcInterface("runner"));
        BindingController bcMaster = Fractal.getBindingController(master);
        bcMaster.bindFc("i1", slave.getFcInterface("i1"));

        // TODO: Do the binding for the new interface
        //@tutorial-break
        //@snippet-break api_interfaces_Main_skeleton
        bcMaster.bindFc("i2", slave.getFcInterface("i2"));
        //@snippet-resume api_interfaces_Main_skeleton
        //@tutorial-resume

        Fractal.getLifeCycleController(slave).startFc();
        Fractal.getLifeCycleController(master).startFc();
        Fractal.getLifeCycleController(composite).startFc();

        Runner runner = (Runner) composite.getFcInterface("runner");
        List<String> arg = new ArrayList<String>();
        arg.add("hello");
        arg.add("world");
        runner.run(arg);

        Fractal.getLifeCycleController(composite).stopFc();

        System.exit(0);
    }
}
//@tutorial-end
//@snippet-end api_interfaces_Main_skeleton
//@snippet-end api_interfaces_Main