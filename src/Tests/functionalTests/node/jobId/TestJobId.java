/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.node.jobId;

import java.rmi.AlreadyBoundException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.Job;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.FunctionalTest;


public class TestJobId extends FunctionalTest {
    @Test
    public void testHalfBodyJobID() {

        /* Half Body must have Job.DEFAULT_JOBID as jobId */
        String halfBodyJobId = PAActiveObject.getBodyOnThis().getJobID();
        Assert.assertEquals(Job.DEFAULT_JOBID, halfBodyJobId);
    }

    @Test
    public void testDefaultJobID() throws NodeException, AlreadyBoundException {
        Node node = NodeFactory.createLocalNode("testDefaultJobID", false, null, null, null);
        Assert.assertEquals(Job.DEFAULT_JOBID, node.getNodeInformation().getJobID());
    }

    @Test
    public void testSpecifiedJobID() throws NodeException, AlreadyBoundException {
        Node node = NodeFactory.createLocalNode("testSpecifiedJobID", false, null, null, "myJobID");
        Assert.assertEquals("myJobID", node.getNodeInformation().getJobID());
    }

    @Test
    public void testSetJobID() throws NodeException, AlreadyBoundException {
        Node node = NodeFactory.createLocalNode("testSetJobID", false, null, null, null);
        Assert.assertEquals(Job.DEFAULT_JOBID, node.getNodeInformation().getJobID());
        node.getNodeInformation().setJobID("setJobID");
        Assert.assertEquals("setJobID", node.getNodeInformation().getJobID());
    }
}
