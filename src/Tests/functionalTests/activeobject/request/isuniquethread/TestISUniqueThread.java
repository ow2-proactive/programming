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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.activeobject.request.isuniquethread;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.request.RequestReceiverImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestISUniqueThread extends GCMFunctionalTestDefaultNodes {

    private final static int NB_CALL = 100;
    private final static int NB_CALLER = 10;

    public TestISUniqueThread(int hostCapacity, int vmCapacity) {
        super(hostCapacity, vmCapacity);
    }

    public TestISUniqueThread() {
        super(2, 1);
    }

    public static class Caller {

        public Caller() {
        }

        public Vector<BooleanWrapper> call(AgentForIS a, int nbCall, MethodSelector ms) {
            Vector<BooleanWrapper> results = new Vector<BooleanWrapper>(nbCall);
            // create RMI threads on callee side
            if (ms.equals(MethodSelector.FOO_VOID)) {
                for (int i = 0; i < nbCall; i++)
                    results.add(a.foo());
            } else if (ms.equals(MethodSelector.FOO_INT)) {
                for (int i = 0; i < nbCall; i++)
                    results.add(a.foo(new Integer(1)));
            } else if (ms.equals(MethodSelector.FOO_LONG_INT)) {
                for (int i = 0; i < nbCall; i++)
                    results.add(a.foo(new Long(1), new Integer(1)));
            } else if (ms.equals(MethodSelector.NOTHING)) {
                for (int i = 0; i < nbCall; i++)
                    a.nothing();
            }
            return results;
        }

        public int synchronousBarrier() {
            return 0;
        }
    }

    protected enum MethodSelector {
        FOO_VOID, FOO_INT, FOO_LONG_INT, NOTHING();
    }

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {

        Node n1 = super.getANode();
        Node n2 = super.getANode();

        AgentForIS a1 = PAActiveObject.newActive(AgentForIS.class, null, n1);
        a1.init();
        AgentForIS a2 = PAActiveObject.newActive(AgentForIS.class, null, n1);
        a2.init();

        Caller[] callers = new Caller[NB_CALLER];
        for (int i = 0; i < NB_CALLER; i++) {
            callers[i] = PAActiveObject.newActive(Caller.class, new Object[] {}, n2);
        }

        // create RMI threads
        for (int i = 0; i < NB_CALLER; i++) {
            callers[i].call(a1, NB_CALL, MethodSelector.NOTHING);
        }

        Vector<BooleanWrapper>[] foo_void = new Vector[NB_CALLER];
        Vector<BooleanWrapper>[] foo_int = new Vector[NB_CALLER];
        Vector<BooleanWrapper>[] foo_long_int = new Vector[NB_CALLER];

        for (int i = 0; i < NB_CALLER; i++) {
            foo_void[i] = callers[i].call(a1, NB_CALL, MethodSelector.FOO_VOID);
            callers[i].call(a2, NB_CALL, MethodSelector.FOO_VOID);
        }

        for (int i = 0; i < NB_CALLER; i++) {
            foo_int[i] = callers[i].call(a1, NB_CALL, MethodSelector.FOO_INT);
            callers[i].call(a2, NB_CALL, MethodSelector.FOO_INT);
        }

        for (int i = 0; i < NB_CALLER; i++) {
            foo_long_int[i] = callers[i].call(a1, NB_CALL, MethodSelector.FOO_LONG_INT);
            callers[i].call(a2, NB_CALL, MethodSelector.FOO_LONG_INT);
        }

        for (int i = 0; i < NB_CALLER; i++) {
            callers[i].synchronousBarrier();
        }
        //checks services
        for (int i = 0; i < NB_CALLER; i++) {
            for (int j = 0; j < NB_CALL; j++) {
                Assert.assertTrue(foo_void[i].get(j).booleanValue());
                Assert.assertTrue(foo_int[i].get(j).booleanValue());
                Assert.assertTrue(foo_long_int[i].get(j).booleanValue());
            }
        }

        // 1) check implicit termination 

        // terminate all the callers
        for (int i = 0; i < NB_CALLER; i++) {
            PAActiveObject.terminateActiveObject(callers[i], true);
        }

        // wait for ping period 
        try {
            Thread.sleep(RequestReceiverImpl.THREAD_FOR_IS_PING_PERIOD * 1000 + 3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(a1.checkAllThreadISAreDown(null));

        // 2) check explicit termination
        UniqueID a2id = a2.getID();
        PAFuture.waitFor(a2id);
        PAActiveObject.terminateActiveObject(a2, true);
        // test a2 threads on a1 because a2 is dead...
        Assert.assertTrue(a1.checkAllThreadISAreDown(a2id));

    }

}
