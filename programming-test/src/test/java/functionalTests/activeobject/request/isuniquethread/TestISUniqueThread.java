/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionalTests.activeobject.request.isuniquethread;

import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.request.RequestReceiverImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.GCMFunctionalTest;


public class TestISUniqueThread extends GCMFunctionalTest {

    private final static int NB_CALL = 100;

    private final static int NB_CALLER = 10;

    public TestISUniqueThread() throws ProActiveException {
        super(2, 1);
        super.startDeployment();
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
        FOO_VOID,
        FOO_INT,
        FOO_LONG_INT,
        NOTHING();
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
        for (Vector<BooleanWrapper> v : foo_int) {
            PAFuture.waitForAll(v);
        }

        //checks services
        for (int i = 0; i < NB_CALLER; i++) {
            for (int j = 0; j < NB_CALL; j++) {
                Assert.assertTrue(foo_void[i].get(j).getBooleanValue());
                Assert.assertTrue(foo_int[i].get(j).getBooleanValue());
                Assert.assertTrue(foo_long_int[i].get(j).getBooleanValue());
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
