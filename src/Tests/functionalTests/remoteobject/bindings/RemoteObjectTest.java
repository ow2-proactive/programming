/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package functionalTests.remoteobject.bindings;

import java.net.URI;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import functionalTests.FunctionalTest;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;


/**
 * Test for the remote objects, registration of a remote object with several protocols and lookup
 */
public class RemoteObjectTest extends FunctionalTest {
    @org.junit.Test
    public void register() throws Exception {
        // get an object
        ProActiveRuntime p = ProActiveRuntimeImpl.getProActiveRuntime();

        // create a remote object exposer for this object

        RemoteObjectExposer<ProActiveRuntime> roe = PARemoteObject.newRemoteObject(ProActiveRuntime.class
                .getName(), p);

        // generate an uri where to rebind the runtime
        URI uri = RemoteObjectHelper.generateUrl("myruntime");

        ProActiveRuntime p1 = PARemoteObject.bind(roe, uri);

        assertTrue(p.getURL().equals(p1.getURL()));
    }

    @org.junit.Test
    public void doubleRegister() throws Exception {
        // get an object
        ProActiveRuntime p = ProActiveRuntimeImpl.getProActiveRuntime();

        // create a remote object exposer for this object
        RemoteObjectExposer<ProActiveRuntime> roe = PARemoteObject.newRemoteObject(ProActiveRuntime.class
                .getName(), p);

        RemoteObject<ProActiveRuntime> ro = roe.getRemoteObject();

        // generate an uri where to rebind the runtime
        URI uri = RemoteObjectHelper.generateUrl("myruntime-1");

        ProActiveRuntime p1 = PARemoteObject.bind(roe, uri);

        // registering on a second url
        URI uri2 = RemoteObjectHelper.generateUrl("myruntime-2");

        // this time using the register method from the helper class
        RemoteObjectHelper.register(ro, uri2, false);

        // new lookup
        RemoteObject<ProActiveRuntime> ro2 = RemoteObjectHelper.lookup(uri2);

        ProActiveRuntime p2 = (ProActiveRuntime) RemoteObjectHelper.generatedObjectStub(ro2);

        assertTrue(p1.getURL().equals(p2.getURL()));
    }

    @org.junit.Test
    public void multibind() throws Exception {
        // get an object
        ProActiveRuntime p = ProActiveRuntimeImpl.getProActiveRuntime();

        // create a remote object exposer for this object
        RemoteObjectExposer<ProActiveRuntime> roe = PARemoteObject.newRemoteObject(ProActiveRuntime.class
                .getName(), p);

        // generate an uri where to rebind the runtime
        URI uri = RemoteObjectHelper.generateUrl("myruntimeA");

        ProActiveRuntime p1 = PARemoteObject.bind(roe, uri);

        // second binding
        URI uri2 = RemoteObjectHelper.generateUrl(Constants.XMLHTTP_PROTOCOL_IDENTIFIER, "myruntimeB");

        ProActiveRuntime p2 = PARemoteObject.bind(roe, uri2);

        assertTrue(p2.getURL().equals(p1.getURL()));
    }

    @org.junit.Test
    public void unregister() throws Exception {
        // get an object
        ProActiveRuntime p = ProActiveRuntimeImpl.getProActiveRuntime();

        // create a remote object exposer for this object
        RemoteObjectExposer<ProActiveRuntime> roe = PARemoteObject.newRemoteObject(ProActiveRuntime.class
                .getName(), p);

        // generate an uri where to rebind the runtime
        URI uri = RemoteObjectHelper.generateUrl("myruntime");

        // let's bind the object on the given endpoint
        PARemoteObject.bind(roe, uri);

        // looking for the remote object
        RemoteObject<ProActiveRuntime> ro = RemoteObjectHelper.lookup(uri);

        assertNotNull(ro);

        PARemoteObject.unregister(uri);

        RemoteObject<ProActiveRuntime> ro1 = null;

        // looking for the remote object
        try {
            ro1 = RemoteObjectHelper.lookup(uri);
        } catch (ProActiveException e) {
        }

        assertNull(ro1);
    }
}
