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
package functionalTests.activeobject.creation.remote.newactive;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;

import functionalTests.GCMFunctionalTest;
import functionalTests.activeobject.creation.A;


/**
 * Test newActive method on a remote node
 */

public class Test extends GCMFunctionalTest {
    A a;

    String name;

    String nodeUrl;

    String remoteHost;

    public Test() throws ProActiveException {
        super(1, 1);
        super.startDeployment();
    }

    @org.junit.Test
    public void action() throws Exception {
        Node remoteNode = super.getANode();

        a = PAActiveObject.newActive(A.class, new Object[] { "toto" }, remoteNode);
        name = a.getName();
        nodeUrl = a.getNodeUrl();

        assertTrue((name.equals("toto")));
    }
}
