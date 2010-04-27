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
package functionalTests.component.conform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import junit.framework.Assert;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.component.conform.components.BadSlaveMulticast;
import functionalTests.component.conform.components.Master;
import functionalTests.component.conform.components.MasterImpl;
import functionalTests.component.conform.components.Slave;
import functionalTests.component.conform.components.SlaveImpl;
import functionalTests.component.conform.components.SlaveMulticast;


public class TestMulticast extends Conformtest {
    protected Component boot;
    protected GCMTypeFactory tf;
    protected GenericFactory gf;
    protected ComponentType tMaster;
    protected ComponentType tBadMaster;
    protected ComponentType tSlave;
    protected final static String serverMaster = "server/" + Master.class.getName() + "/false,false,false";
    protected final static String serverSlave = "server-multicast/" + PKG + ".Slave/false,false,false";
    protected final static String clientSlaveMulticast = MasterImpl.ITF_CLIENTE_MULTICAST + "/" + PKG +
        ".SlaveMulticast/true,false,false";

    // -------------------------------------------------------------------------
    // Constructor and setup
    // -------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {
        boot = Utils.getBootstrapComponent();
        tf = GCM.getGCMTypeFactory(boot);
        gf = GCM.getGenericFactory(boot);
        tMaster = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("server", Master.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createGCMItfType(MasterImpl.ITF_CLIENTE_MULTICAST, SlaveMulticast.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY, GCMTypeFactory.MULTICAST_CARDINALITY) });
        tSlave = tf.createFcType(new InterfaceType[] { tf.createFcItfType("server-multicast", Slave.class
                .getName(), TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE), });
    }

    // -------------------------------------------------------------------------
    // Test component instantiation
    // -------------------------------------------------------------------------
    @Test
    public void testPrimitiveWithMulticast() throws Exception {
        Component master = gf.newFcInstance(tMaster, "primitive", MasterImpl.class.getName());
        checkComponent(master, new HashSet<Object>(Arrays.asList(new Object[] { COMP, BC, LC, SC, NC, MCC,
                GC, MC, MoC, PC, serverMaster, clientSlaveMulticast })));
        Component slave = gf.newFcInstance(tSlave, "primitive", SlaveImpl.class.getName());
        checkComponent(slave, new HashSet<Object>(Arrays.asList(new Object[] { COMP, LC, SC, NC, MCC, GC, MC,
                MoC, PC, serverSlave })));
    }

    @Test
    public void testCompositeWithMulticast() throws Exception {
        Component master = gf.newFcInstance(tMaster, "composite", null);
        checkComponent(master, new HashSet<Object>(Arrays.asList(new Object[] { COMP, BC, CC, LC, SC, NC,
                MCC, GC, MC, MoC, PC, serverMaster, clientSlaveMulticast })));
        Component slave = gf.newFcInstance(tSlave, "composite", null);
        checkComponent(slave, new HashSet<Object>(Arrays.asList(new Object[] { COMP, BC, CC, LC, SC, NC, MCC,
                GC, MC, MoC, PC, serverSlave })));
    }

    @Test(expected = InstantiationException.class)
    public void testItfTypeWithBadMulticastItf() throws Exception {
        tBadMaster = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("server", Master.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createGCMItfType("client-multicast", BadSlaveMulticast.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.MANDATORY, GCMTypeFactory.MULTICAST_CARDINALITY) });
    }

    // -------------------------------------------------------------------------
    // Test multicast interface with different dispatch of parameters
    // -------------------------------------------------------------------------
    @Test
    public void testRoundRobinParameterDispatch() throws Exception {
        Component master = gf.newFcInstance(tMaster, "primitive", MasterImpl.class.getName());
        Component slave1 = gf.newFcInstance(tSlave, "primitive", SlaveImpl.class.getName());
        Component slave2 = gf.newFcInstance(tSlave, "primitive", SlaveImpl.class.getName());

        GCM.getBindingController(master).bindFc(MasterImpl.ITF_CLIENTE_MULTICAST,
                slave1.getFcInterface("server-multicast"));
        GCM.getBindingController(master).bindFc(MasterImpl.ITF_CLIENTE_MULTICAST,
                slave2.getFcInterface("server-multicast"));

        GCM.getGCMLifeCycleController(master).startFc();
        GCM.getGCMLifeCycleController(slave1).startFc();
        GCM.getGCMLifeCycleController(slave2).startFc();

        Master masterItf = (Master) master.getFcInterface("server");

        List<List<String>> listOfParameters = generateParameter();
        for (List<String> stringList : listOfParameters) {
            masterItf.computeOneWay(stringList, "OneWay call");
        }
        for (List<String> stringList : listOfParameters) {
            List<StringWrapper> results = masterItf.computeAsync(stringList, "Asynchronous call");
            ArrayList<String> resultsAL = new ArrayList<String>();
            for (StringWrapper sw : results) {
                Assert.assertNotNull("One result is null", sw);
                resultsAL.add(sw.stringValue());
            }
            checkResult(stringList, "Asynchronous call", resultsAL);
            System.err.println("TM: async call" + results);
        }
        //FIXME
        //for (List<String> stringList : listOfParameters) {
        //List<GenericTypeWrapper<String>> results = masterItf.computeAsyncGenerics(stringList,
        //        "Asynchronous call");
        //System.err.println("TM: async gen call" + results);
        //}
        //FIXME
        //for (List<String> stringList : listOfParameters) {
        //List<String> results = masterItf.computeSync(stringList,
        //        "With non reifiable return type call");
        //System.err.println("TM: sync call" +  results);
        //}
    }

    @Test
    public void testMixedRoundRobinBroadcastParameterDispatch() throws Exception {
        Component master = gf.newFcInstance(tMaster, "primitive", MasterImpl.class.getName());
        Component slave1 = gf.newFcInstance(tSlave, "primitive", SlaveImpl.class.getName());
        Component slave2 = gf.newFcInstance(tSlave, "primitive", SlaveImpl.class.getName());

        GCM.getBindingController(master).bindFc(MasterImpl.ITF_CLIENTE_MULTICAST,
                slave1.getFcInterface("server-multicast"));
        GCM.getBindingController(master).bindFc(MasterImpl.ITF_CLIENTE_MULTICAST,
                slave2.getFcInterface("server-multicast"));

        GCM.getGCMLifeCycleController(master).startFc();
        GCM.getGCMLifeCycleController(slave1).startFc();
        GCM.getGCMLifeCycleController(slave2).startFc();

        Master masterItf = (Master) master.getFcInterface("server");

        List<List<String>> listOfParameters = generateParameter();
        for (List<String> stringList : listOfParameters) {
            masterItf.computeOneWay(stringList, "OneWay call");
        }
        List<String> broadcastArg = new ArrayList<String>();
        broadcastArg.add("mix RoundRobinBroadcast arg1");
        broadcastArg.add("mix RoundRobinBroadcast arg2");
        broadcastArg.add("mix RoundRobinBroadcast arg3");
        String broadcastArgs = "mix RoundRobinBroadcast arg1" + "mix RoundRobinBroadcast arg2"
            + "mix RoundRobinBroadcast arg3";
        for (List<String> stringList : listOfParameters) {
            List<StringWrapper> results = masterItf.computeRoundRobinBroadcastAsync(stringList, broadcastArg);
            ArrayList<String> resultsAL = new ArrayList<String>();
            for (StringWrapper sw : results) {
                Assert.assertNotNull("One result is null", sw);
                resultsAL.add(sw.stringValue());
            }
            checkResult(stringList, broadcastArgs, resultsAL);
            System.err.println("TM: async call" + results);
        }
        //FIXME
        //for (List<String> stringList : listOfParameters) {
        //List<GenericTypeWrapper<String>> results = masterItf.computeAsyncGenerics(stringList,
        //        "Asynchronous call");
        //System.err.println("TM: async gen call" + results);
        //}
        //FIXME
        //for (List<String> stringList : listOfParameters) {
        //List<String> results = masterItf.computeSync(stringList,
        //        "With non reifiable return type call");
        //System.err.println("TM: sync call" +  results);
        //}
    }

    private static List<List<String>> generateParameter() {
        List<List<String>> multicastArgsList = new ArrayList<List<String>>();
        for (int i = 0; i < 6; i++) {
            multicastArgsList.add(i, new ArrayList<String>());

            for (int j = 0; j < i; j++) {
                multicastArgsList.get(i).add("arg " + j);
            }
        }
        return multicastArgsList;
    }

    private static void checkResult(List<String> args, String other, ArrayList<String> results) {
        Slave mySlave = new SlaveImpl();
        ArrayList<String> expectedResults = new ArrayList<String>(args.size());

        for (String string : args) {
            expectedResults.add(mySlave.computeSync(string, other));
        }
        Assert.assertEquals("Result aren't equals", expectedResults, results);
    }
}
