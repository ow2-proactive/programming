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
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.activeobject.proactive359;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.body.request.BlockingRequestQueue;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;

import functionalTests.FunctionalTest;


public class TestProActive359 extends FunctionalTest {

    @Test
    public void test() throws ActiveObjectCreationException, NodeException, InterruptedException {
        AO ao = PAActiveObject.newActive(AO.class, null);

        boolean exception;

        try {
            exception = false;

            StringWrapper foo = ao.foo();
            PAFuture.waitFor(foo, 1000);
        } catch (ProActiveTimeoutException e) {
            exception = true;
        }
        Assert.assertFalse(exception);

        StringWrapper bar = ao.bar();
        try {
            exception = false;

            PAFuture.waitFor(bar, 1000);
        } catch (ProActiveTimeoutException e) {
            exception = true;
        }
        Assert.assertTrue(exception);

        ao.resume();
        try {
            exception = false;

            PAFuture.waitFor(bar, 1000);
        } catch (ProActiveTimeoutException e) {
            exception = true;
        }
        Assert.assertFalse(exception);
    }

    static public class AO implements InitActive, RunActive, Serializable {
        private BlockingRequestQueue rqueue;

        public AO() {

        }

        public void initActivity(Body body) {
            PAActiveObject.setImmediateService("resume");
            rqueue = body.getRequestQueue();
        }

        public void runActivity(Body body) {
            Service service = new Service(body);
            service.blockingServeOldest("foo");

            rqueue.suspend();
            service.blockingServeOldest("bar");

        }

        public void resume() {
            rqueue.resume();
        }

        public StringWrapper foo() {
            System.err.println("foo");
            return new StringWrapper("foo");
        }

        public StringWrapper bar() {
            System.err.println("bar");
            return new StringWrapper("bar");
        }
    }

}
