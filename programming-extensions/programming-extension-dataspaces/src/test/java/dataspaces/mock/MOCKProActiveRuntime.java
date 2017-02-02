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
import java.lang.reflect.InvocationTargetException;
import java.rmi.AlreadyBoundException;
import java.util.List;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.filetransfer.FileTransferEngine;
import org.objectweb.proactive.core.jmx.mbean.ProActiveRuntimeWrapperMBean;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.jmx.server.ServerConnector;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.ConstructorCallExecutionFailedException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.process.UniversalProcess;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.VMInformation;


public class MOCKProActiveRuntime implements ProActiveRuntime {

    final private MOCKVMInformation info;

    public MOCKProActiveRuntime(String runtimeId) {
        info = new MOCKVMInformation(runtimeId);
    }

    public void addAcquaintance(String proActiveRuntimeName) throws ProActiveException {

    }

    public UniversalBody createBody(String nodeName, ConstructorCall bodyConstructorCall, boolean isNodeLocal)
            throws ProActiveException, ConstructorCallExecutionFailedException, InvocationTargetException {
        return null;
    }

    public Node createGCMNode(String vnName, List<TechnicalService> tsList)
            throws NodeException, AlreadyBoundException {
        return null;
    }

    public Node createLocalNode(String nodeName, boolean replacePreviousBinding, String vnName)
            throws NodeException, AlreadyBoundException {

        return null;
    }

    public void createVM(UniversalProcess remoteProcess) throws IOException, ProActiveException {

    }

    public String[] getAcquaintances() throws ProActiveException {

        return null;
    }

    public List<UniversalBody> getActiveObjects(String nodeName) throws ProActiveException {

        return null;
    }

    public List<UniversalBody> getActiveObjects(String nodeName, String className) throws ProActiveException {

        return null;
    }

    public byte[] getClassDataFromParentRuntime(String className) throws ProActiveException {

        return null;
    }

    public byte[] getClassDataFromThisRuntime(String className) throws ProActiveException {

        return null;
    }

    public ProActiveDescriptorInternal getDescriptor(String url, boolean isHierarchicalSearch)
            throws IOException, ProActiveException {

        return null;
    }

    public FileTransferEngine getFileTransferEngine() {

        return null;
    }

    public ServerConnector getJMXServerConnector() {

        return null;
    }

    public String getJobID(String nodeUrl) throws ProActiveException {

        return null;
    }

    public String[] getLocalNodeNames() throws ProActiveException {

        return null;
    }

    public String getLocalNodeProperty(String nodeName, String key) throws ProActiveException {

        return null;
    }

    public ProActiveRuntimeWrapperMBean getMBean() {

        return null;
    }

    public String getMBeanServerName() {

        return null;
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) throws ProActiveException {

        return null;
    }

    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {

        return null;
    }

    public String getURL() {

        return null;
    }

    public VMInformation getVMInformation() {
        return info;
    }

    public String getVNName(String Nodename) throws ProActiveException {

        return null;
    }

    public VirtualNodeInternal getVirtualNode(String virtualNodeName) throws ProActiveException {

        return null;
    }

    public void killAllNodes() throws ProActiveException {

    }

    public void killNode(String nodeName) throws ProActiveException {

    }

    public void killRT(boolean softly) {

    }

    public void launchMain(String className, String[] parameters)
            throws ClassNotFoundException, NoSuchMethodException, ProActiveException {

    }

    public void newRemote(String className) throws ClassNotFoundException, ProActiveException {

    }

    public UniversalBody receiveBody(String nodeName, Body body) throws ProActiveException {

        return null;
    }

    public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl, String creatorID,
            String creationProtocol, String vmName) throws ProActiveException {

    }

    public void register(GCMRuntimeRegistrationNotificationData event) {

    }

    public void registerVirtualNode(String virtualNodeName, boolean replacePreviousBinding)
            throws ProActiveException, AlreadyBoundException {

    }

    public void rmAcquaintance(String proActiveRuntimeName) throws ProActiveException {

    }

    public Object setLocalNodeProperty(String nodeName, String key, String value) throws ProActiveException {

        return null;
    }

    public void startJMXServerConnector() {

    }

    public void unregister(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl, String creatorID,
            String creationProtocol, String vmName) throws ProActiveException {

    }

    public void unregisterAllVirtualNodes() throws ProActiveException {

    }

    public void unregisterVirtualNode(String virtualNodeName) throws ProActiveException {

    }

    public byte[] getClassData(String className) {
        return null;
    }

}
