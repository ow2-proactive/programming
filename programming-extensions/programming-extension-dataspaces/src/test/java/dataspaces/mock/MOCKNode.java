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
package dataspaces.mock;

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

    @Override
    public String getThreadDump() throws ProActiveException {
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
