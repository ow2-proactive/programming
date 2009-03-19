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
package functionalTests.activeobject.loopmixedlocation;

import static junit.framework.Assert.assertTrue;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.ext.util.SimpleLocationServer;
import org.objectweb.proactive.extensions.mixedlocation.MixedLocationMetaObjectFactory;

import functionalTests.GCMFunctionalTestDefaultNodes;

import functionalTests.activeobject.locationserver.A;
import functionalTests.activeobject.locationserver.MigratableA;


/**
 * Test migration with location server and forwarder
 */

public class TestLoopMixedLocation extends GCMFunctionalTestDefaultNodes {
    A a;
    MigratableA migratableA;
    SimpleLocationServer server;
    UniqueID idA;

    public TestLoopMixedLocation() throws ActiveObjectCreationException, NodeException, IOException {
        super(1, 1);

        this.server = (SimpleLocationServer) PAActiveObject.newActive(SimpleLocationServer.class.getName(),
                new Object[] {});
        String serverUrl = PAActiveObject.registerByName(this.server, "LocationServer");

        PAProperties.PA_LOCATION_SERVER_RMI.setValue(serverUrl);

        String additionalJVMargs = PAProperties.PA_LOCATION_SERVER_RMI.getCmdLine() + serverUrl;
        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_JVMARG, additionalJVMargs,
                VariableContractType.DescriptorDefaultVariable);
    }

    @org.junit.Test
    public void action() throws Exception {

        a = (A) PAActiveObject.newActive(A.class.getName(), null, new Object[] { "toto" }, null, null,
                MixedLocationMetaObjectFactory.newInstance());
        migratableA = (MigratableA) PAActiveObject.newActive(MigratableA.class.getName(), null,
                new Object[] { "toto" }, null, null, MixedLocationMetaObjectFactory.newInstance());
        idA = ((BodyProxy) ((StubObject) a).getProxy()).getBodyID();
        Node node = super.getANode();
        migratableA.moveTo(node);
        Thread.sleep(3000);

        assertTrue(server.searchObject(idA) != null);
        assertTrue(a.getName(migratableA).equals("toto"));
    }
}
