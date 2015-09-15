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
package functionalTests.activeobject.context;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import functionalTests.GCMFunctionalTest;

import static org.junit.Assert.assertTrue;


public class TestContext extends GCMFunctionalTest {

    public TestContext() throws ProActiveException {
        super(2, 1);
        super.startDeployment();
    }

    @org.junit.Test
    public void action() throws Exception {
        Node node1 = getANode();
        Node node2 = getANode();

        // test halfBody creation
        UniqueID myId = null;
        Context c = PAActiveObject.getContext();
        Body myHalfBody = c.getBody();

        // a half body should have been created
        assertTrue(myHalfBody != null);
        myId = myHalfBody.getID();

        boolean exceptionOccured = false;
        try {
            myHalfBody.getRequestQueue();
        } catch (ProActiveRuntimeException e) {
            // Half bodies does not have request queue...
            exceptionOccured = true;
        }
        // myHalfBody should be a half body
        assertTrue(exceptionOccured);

        // test getContext
        AOContext a1 = PAActiveObject.newActive(AOContext.class, null, node1);
        AOContext a2 = PAActiveObject.newActive(AOContext.class, null, node2);

        a1.init("A1");
        a2.init("A2");

        // test between two active objects
        BooleanWrapper res1 = a1.test(a2);
        boolean b = res1.getBooleanValue();
        assertTrue(b);

        // test from a halfBody
        BooleanWrapper res21 = a1.standardService(myId);
        BooleanWrapper res22 = a1.immediateService(myId);
        assertTrue(res21.getBooleanValue());
        assertTrue(res22.getBooleanValue());

        // test stub on caller
        a1.initTestStubOnCaller(a2);
        assertTrue(a2.getCallerName().equals(a1.getName()));

        // test exception for halfbody caller
        assertTrue(a1.testHalfBodyCaller().getBooleanValue());
    }
}
