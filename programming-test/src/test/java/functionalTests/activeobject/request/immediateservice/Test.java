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
package functionalTests.activeobject.request.immediateservice;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;

import functionalTests.FunctionalTest;


/**
 * Test immediateService method on an AO
 */

public class Test extends FunctionalTest {
    A a;

    boolean raisedExceptionArgs, raisedExceptionName, synchroCall;

    BooleanWrapper asynchCall;

    @Before
    public void action() throws Exception {
        a = PAActiveObject.newActive(A.class, new Object[] { "toto" });
        a.init(); // sync call
        // getObject is set as an IS in the runActivity of A
        synchroCall = a.getBooleanSynchronous();
        asynchCall = a.getBooleanAsynchronous();
        raisedExceptionArgs = a.getExceptionMethodArgs();
        raisedExceptionName = a.getExceptionMethodName();
        PAActiveObject.terminateActiveObject(a, true);
    }

    @org.junit.Test
    public void postConditions() throws Exception {
        assertTrue(!PAFuture.isAwaited(asynchCall));
        assertTrue(asynchCall.getBooleanValue());
        assertTrue(synchroCall);
        assertTrue(raisedExceptionArgs);
        assertTrue(raisedExceptionName);
    }
}
