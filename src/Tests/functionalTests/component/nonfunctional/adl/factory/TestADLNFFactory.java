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
package functionalTests.component.nonfunctional.adl.factory;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.ComponentTest;
import functionalTests.component.nonfunctional.adl.factory.nf.NFService;


public class TestADLNFFactory extends ComponentTest {

    public TestADLNFFactory() {
        super("TestADLNFFactory", "Component instantiation using the extended ADL Factory");
    }

    @org.junit.Test
    public void action() throws Exception {

        // ADL Instantiation
        System.out.println("------------------------------------");
        System.out.println("Starting GCM ADL instantiation ...");
        Factory f = FactoryFactory.getFactory();
        Map<Object, Object> context = new HashMap<Object, Object>();
        String gcmADL = "functionalTests.component.nonfunctional.adl.factory.adl.helloworld-wrappers";
        System.out.println("GCM ADL: " + gcmADL);
        Component comp = (Component) f.newComponent(gcmADL, context);

        // Start and run
        System.out.println("------------------------------------");
        System.out.println("Components created... ");
        System.out.println("Checking membranes ...");
        checkMembranes(comp);
        System.out.println("------------------------------------");
        System.out.println("Starting LifeCycle...");
        GCM.getGCMLifeCycleController(comp).startFc();
        System.out.println("Running...");
        ((Runnable) comp.getFcInterface("r")).run();

        // change one attribute of server and re-run
        Component clientWrapper = find(comp, "client-wrapper");
        if (clientWrapper != null) {
            ((WrapperAttributes) GCM.getAttributeController(clientWrapper)).setHeader("<----");
            ((WrapperAttributes) GCM.getAttributeController(clientWrapper)).setCount(3);
            System.out.println("Running again ...");
            ((Runnable) comp.getFcInterface("r")).run();
        }

        System.out.println("------------------------------------");
        System.out.println("Now making an NF call!");
        NFService nf = (NFService) comp.getFcInterface("dummy-controller");
        nf.print("NFmsg");
        System.out.println("------------------------------------");
        nf.print("NFmsg2");
        System.out.println("------------------------------------");
        StringWrapper res = nf.walk();
        System.out.println(res);
        System.out.println("------------------------------------");

        // Finish
        System.out.println("... and done!");
        System.out.println("------------------------------------");
        System.out.println("Stopping components ...");
        GCM.getGCMLifeCycleController(comp).stopFc();
        System.out.println("Finished!...");
    }

    /**
     * DFS search for a component from the component 'start'
     * 
     * @param start
     * @param name
     * @return
     */
    public static Component find(Component start, String name) {
        try {
            Component[] subComponents = GCM.getContentController(start).getFcSubComponents();
            for (Component comp : subComponents) {
                String compName = GCM.getNameController(comp).getFcName();
                if (name.equals(compName)) {
                    return comp;
                } else {
                    return find(comp, name);
                }
            }
        } catch (NoSuchInterfaceException e) {
            // silently continue
        }
        return null;
    }

    /** 
     * DFS search looking for membranes in each component beginning from 'start'
     * @param start
     */
    public static void checkMembranes(Component start) {

        PAMembraneController pamc = null;
        String compName = null;

        try {
            compName = GCM.getNameController(start).getFcName();
            pamc = Utils.getPAMembraneController(start);
        } catch (NoSuchInterfaceException e1) {
            pamc = null;
        }
        System.out.println("Component " + compName + " has MembraneController? " + (pamc != null));

        try {
            Component[] subComponents = GCM.getContentController(start).getFcSubComponents();
            for (Component comp : subComponents) {
                checkMembranes(comp);
            }
        } catch (NoSuchInterfaceException e) {
            // primitive component ... silently continue
        }
    }
}
