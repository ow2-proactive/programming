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
package unitTests.dataspaces.mock;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeInformation;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;


public class MOCKNode implements Node {

    final private MOCKNodeInformation info;
    final private MOCKProActiveRuntime runtime;

    public MOCKNode(String runtimeId, String nodeId) {
        info = new MOCKNodeInformation(nodeId);
        runtime = new MOCKProActiveRuntime(runtimeId);
    }

    public Object[] getActiveObjects() throws NodeException, ActiveObjectCreationException {
        return null;
    }

    public Object[] getActiveObjects(String className) throws NodeException, ActiveObjectCreationException {
        return null;
    }

    public NodeInformation getNodeInformation() {
        return info;
    }

    public int getNumberOfActiveObjects() throws NodeException {

        return 0;
    }

    public ProActiveRuntime getProActiveRuntime() {
        return runtime;
    }

    public String getProperty(String key) throws ProActiveException {

        return null;
    }

    public VMInformation getVMInformation() {
        return runtime.getVMInformation();
    }

    public void killAllActiveObjects() throws NodeException, IOException {

    }

    public Object setProperty(String key, String value) throws ProActiveException {

        return null;
    }

}
