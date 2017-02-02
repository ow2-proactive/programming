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
package org.objectweb.proactive.core.node;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;


public class NodeImplTest {

    @Before
    public void disableRMISecurityManager() throws Exception {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
    }

    // The bug actually lies in ProActiveRuntimeImpl but is visible when trying to kill AOs on a node
    @Test
    public void allActiveObjectsAreKilled() throws Exception {
        Node localNode = NodeFactory.createLocalNode("allActiveObjectsAreKilled", false, "allActiveObjectsAreKilled");

        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        localNode.killAllActiveObjects();

        assertEquals(0, localNode.getNumberOfActiveObjects());

        // second kill reveals the issue
        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);
        localNode.killAllActiveObjects();

        assertEquals(0, localNode.getNumberOfActiveObjects());
    }

    @Test
    public void testThreadDump() throws Exception {
        Node localNode = NodeFactory.createLocalNode("testThreadDump", false, "testThreadDump");

        PAActiveObject.newActive(SimpleActiveObject.class.getName(), null, localNode);

        String threadDump = localNode.getThreadDump();

        System.out.println(threadDump);

        assertTrue(threadDump.contains(SimpleActiveObject.class.getSimpleName()));
        localNode.killAllActiveObjects();
    }

    public static class SimpleActiveObject {
    }
}
