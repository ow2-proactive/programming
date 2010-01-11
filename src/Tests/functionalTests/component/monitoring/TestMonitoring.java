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
package functionalTests.component.monitoring;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.controller.MethodStatistics;
import org.objectweb.proactive.core.component.controller.MonitorController;

import functionalTests.ComponentTest;


/**
 * Test the monitor controller.
 *
 * @author The ProActive Team
 */
public class TestMonitoring extends ComponentTest {
    // private static final long OVERHEAD = 1000;
    private Factory factory;
    private Component root;
    private MonitorController monitor;

    @org.junit.Test
    public void testMonitoringPrimitiveComponent() throws Exception {
        factory = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<Object, Object> context = new HashMap<Object, Object>();
        root = (Component) factory.newComponent("functionalTests.component.monitoring.adl.TestPrimitive",
                context);

        start();
    }

    @org.junit.Test
    public void testMonitoringCompositeComponent() throws Exception {
        factory = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<Object, Object> context = new HashMap<Object, Object>();
        root = (Component) factory.newComponent("functionalTests.component.monitoring.adl.TestComposite",
                context);

        start();
    }

    @org.junit.Test
    public void testMonitoringCompositeComponentWithMulticast() throws Exception {
        factory = org.objectweb.proactive.core.component.adl.FactoryFactory.getFactory();
        Map<Object, Object> context = new HashMap<Object, Object>();
        root = (Component) factory.newComponent(
                "functionalTests.component.monitoring.adl.TestCompositeMulticast", context);

        start();
    }

    private void printStats() {
        Iterator<MethodStatistics> stats = monitor.getAllStatistics().values().iterator();
        while (stats.hasNext()) {
            System.out.println(stats.next().toString());
        }
    }

    private void checkTime(double supposedTime, double realTime) {
        assertTrue("Measured time is lesser than expected (" + realTime + " instead of " +
            (supposedTime * 0.7) + ")", (supposedTime * 0.7) <= realTime);
        // Upper bound removed due to randomly latency for executing requests
        // assertTrue("Measured time is greater than expected (" + realTime + " instead of " +
        //   ((supposedTime * 1.3) + OVERHEAD) + ")", realTime <= ((supposedTime * 1.3) + OVERHEAD));
    }

    private void checkMethodStatistics(String itfName, String methodName, int nbCalls, int nbMethods,
            long sleepTimeCallMethod) throws Exception {
        MethodStatistics methodStats = monitor.getStatistics(itfName, methodName);
        checkTime(ServerImpl.EXECUTION_TIME, methodStats.getAverageServiceTime());
        checkTime(nbMethods * sleepTimeCallMethod, methodStats.getAverageInterArrivalTime());
    }

    public void start() throws Exception {
        Component[] subComponents = Fractal.getContentController(root).getFcSubComponents();
        for (int i = 0; i < subComponents.length; i++) {
            if (((NameController) subComponents[i].getFcInterface(Constants.NAME_CONTROLLER)).getFcName()
                    .equals("server")) {
                monitor = (MonitorController) subComponents[i].getFcInterface(Constants.MONITOR_CONTROLLER);
            }
        }

        Fractal.getLifeCycleController(root).startFc();

        Runner runner1 = ((Runner) root.getFcInterface("runner1"));
        Runner runner2 = ((Runner) root.getFcInterface("runner2"));
        monitor.startMonitoring();

        System.out.println();
        System.out.println("-----------------------------------------------------------");
        System.out.println("Before execution:");
        System.out.println();
        printStats();

        runner1.run();
        runner2.run();

        int totalNbMethodCalls = runner1.getTotalNbMethodCalls() + runner2.getTotalNbMethodCalls();

        Thread.sleep(ServerImpl.EXECUTION_TIME * totalNbMethodCalls / 2);

        System.out.println();
        System.out.println("-----------------------------------------------------------");
        System.out.println("During execution:");
        System.out.println();
        printStats();

        Thread.sleep(ServerImpl.EXECUTION_TIME * totalNbMethodCalls / 2);

        System.out.println();
        System.out.println("-----------------------------------------------------------");
        System.out.println("After execution:");
        System.out.println();
        printStats();

        String[] itfNamesForEachMethod = runner1.getItfNamesForEachMethod();
        String[] methodNames = runner1.getMethodNames();
        int[] nbCallsPerMethod = runner1.getNbCallsPerMethod();
        for (int i = 0; i < methodNames.length; i++) {
            checkMethodStatistics(itfNamesForEachMethod[i], methodNames[i], nbCallsPerMethod[i],
                    methodNames.length, runner1.getSleepTime());
        }
        itfNamesForEachMethod = runner2.getItfNamesForEachMethod();
        methodNames = runner2.getMethodNames();
        nbCallsPerMethod = runner2.getNbCallsPerMethod();
        for (int i = 0; i < methodNames.length; i++) {
            checkMethodStatistics(itfNamesForEachMethod[i], methodNames[i], nbCallsPerMethod[i],
                    methodNames.length, runner2.getSleepTime());
        }
    }
}
