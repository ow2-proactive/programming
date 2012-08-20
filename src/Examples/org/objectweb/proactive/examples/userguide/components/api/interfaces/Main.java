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
//@snippet-start api_interfaces_Main_skeleton
//@snippet-start api_interfaces_Main
//@tutorial-start
package org.objectweb.proactive.examples.userguide.components.api.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;


/**
 * @author The ProActive Team
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Component boot = Utils.getBootstrapComponent();
        GCMTypeFactory tf = GCM.getGCMTypeFactory(boot);
        GenericFactory gf = GCM.getGenericFactory(boot);
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

        ContentController cc = GCM.getContentController(composite);
        cc.addFcSubComponent(slave);
        cc.addFcSubComponent(master);

        BindingController bcComposite = GCM.getBindingController(composite);
        bcComposite.bindFc("runner", master.getFcInterface("runner"));
        BindingController bcMaster = GCM.getBindingController(master);
        bcMaster.bindFc("i1", slave.getFcInterface("i1"));

        // TODO: Do the binding for the new interface
        //@tutorial-break
        //@snippet-break api_interfaces_Main_skeleton
        bcMaster.bindFc("i2", slave.getFcInterface("i2"));
        //@snippet-resume api_interfaces_Main_skeleton
        //@tutorial-resume

        GCM.getGCMLifeCycleController(composite).startFc();

        Runner runner = (Runner) composite.getFcInterface("runner");
        List<String> arg = new ArrayList<String>();
        arg.add("hello");
        arg.add("world");
        runner.run(arg);

        GCM.getGCMLifeCycleController(composite).stopFc();

        System.exit(0);
    }
}
//@tutorial-end
//@snippet-end api_interfaces_Main_skeleton
//@snippet-end api_interfaces_Main
