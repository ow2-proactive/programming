//@tutorial-start
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
//@snippet-start adl_starter_Main_skeleton
//@snippet-start adl_starter_Main
package org.objectweb.proactive.examples.userguide.components.adl.starter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.adl.FactoryFactory;


/**
 * @author The ProActive Team
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // TODO: Get the Factory
        //@tutorial-break
        //@snippet-break adl_starter_Main_skeleton
        Factory factory = FactoryFactory.getFactory();
        //@snippet-resume adl_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Create Component
        //@tutorial-break
        //@snippet-break adl_starter_Main_skeleton
        Map<String, Object> context = new HashMap<String, Object>();
        Component slave = (Component) factory.newComponent(
                "org.objectweb.proactive.examples.userguide.components.adl.starter.adl.Slave", context);
        //@snippet-resume adl_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Start Component
        //@tutorial-break
        //@snippet-break adl_starter_Main_skeleton
        Fractal.getLifeCycleController(slave).startFc();
        //@snippet-resume adl_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Get the interface i1
        //@tutorial-break
        //@snippet-break adl_starter_Main_skeleton
        Itf1 itf1 = (Itf1) slave.getFcInterface("i1");
        //@snippet-resume adl_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Call the compute method
        //@tutorial-break
        //@snippet-break adl_starter_Main_skeleton
        List<String> arg = new ArrayList<String>();
        arg.add("hello");
        arg.add("world");
        itf1.compute(arg);
        //@snippet-resume adl_starter_Main_skeleton
        //@tutorial-resume
        // TODO: Stop Component
        //@tutorial-break
        //@snippet-break adl_starter_Main_skeleton
        Fractal.getLifeCycleController(slave).stopFc();
        //@snippet-resume adl_starter_Main_skeleton
        //@tutorial-resume

        System.exit(0);
    }
}
//@tutorial-end
//@snippet-end adl_starter_Main_skeleton
//@snippet-end adl_starter_Main
