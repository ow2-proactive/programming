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
package functionalTests.activeobject.locationserver;

import java.io.IOException;

import junit.framework.Assert;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.ext.locationserver.LocationServerMetaObjectFactory;
import org.objectweb.proactive.ext.util.SimpleLocationServer;

import functionalTests.GCMFunctionalTestDefaultNodes;


/**
 * Test migration with location server
 */

public class TestLocationServer extends GCMFunctionalTestDefaultNodes {
    A a;
    MigratableA migratableA;
    UniqueID idA;

    SimpleLocationServer server;

    public TestLocationServer() throws IOException, ProActiveException {
        super(1, 1);

        this.server = PAActiveObject.newActive(SimpleLocationServer.class, new Object[] {});
        String serverUrl = PAActiveObject.registerByName(this.server, "LocationServer");

        PAProperties.PA_LOCATION_SERVER_RMI.setValue(serverUrl);

        String additionalJVMargs = PAProperties.PA_LOCATION_SERVER_RMI.getCmdLine() + serverUrl;
        super.vContract.setVariableFromProgram(GCMFunctionalTestDefaultNodes.VAR_JVMARG, additionalJVMargs,
                VariableContractType.DescriptorDefaultVariable);
    }

    @org.junit.Test
    public void action() throws Exception {
        this.a = PAActiveObject.newActive(A.class, null, new Object[] { "toto" }, null, null,
                LocationServerMetaObjectFactory.newInstance());

        this.migratableA = PAActiveObject.newActive(MigratableA.class, null, new Object[] { "toto" }, null,
                null, LocationServerMetaObjectFactory.newInstance());

        this.idA = ((BodyProxy) ((StubObject) this.a).getProxy()).getBodyID();

        Node node = super.getANode();
        this.migratableA.moveTo(node);

        Thread.sleep(3000);

        Assert.assertNotNull(this.server.searchObject(this.idA));
        Assert.assertEquals("toto", this.a.getName(this.migratableA));
    }
}
