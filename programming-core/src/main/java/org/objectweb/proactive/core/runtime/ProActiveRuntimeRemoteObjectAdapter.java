/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.runtime;

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
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;


/**
 * @author The ProActive Team
 * this class provides some additional behaviours expected when talking with a
 * runtime.
 *  - cache the vmInformation field
 */

public class ProActiveRuntimeRemoteObjectAdapter extends Adapter<ProActiveRuntime> implements
        ProActiveRuntime {

    private static final long serialVersionUID = 62L;

    /**
     * generated serial uid
     */

    /**
     * Cache the vmInformation field
     */
    protected VMInformation vmInformation;

    /**
     * Cache the URL value
     */
    protected String url;

    public ProActiveRuntimeRemoteObjectAdapter() {
        // empty non arg constructor
    }

    public ProActiveRuntimeRemoteObjectAdapter(ProActiveRuntime u) {
        super(u);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.adapter.Adapter#construct()
     */
    @Override
    protected void construct() {
        this.vmInformation = target.getVMInformation();
        this.url = target.getURL();
    }

    // =========   Implements ProActiveRuntime ==================

    public UniversalBody createBody(String nodeName, ConstructorCall bodyConstructorCall, boolean isNodeLocal)
            throws ProActiveException, ConstructorCallExecutionFailedException, InvocationTargetException {
        return target.createBody(nodeName, bodyConstructorCall, isNodeLocal);
    }

    public Node createLocalNode(String nodeName, boolean replacePreviousBinding, String vnName)
            throws NodeException, AlreadyBoundException {
        return target.createLocalNode(nodeName, replacePreviousBinding, vnName);
    }

    public void createVM(UniversalProcess remoteProcess) throws IOException, ProActiveException {
        target.createVM(remoteProcess);
    }

    public List<UniversalBody> getActiveObjects(String nodeName) throws ProActiveException {
        return target.getActiveObjects(nodeName);
    }

    public List<UniversalBody> getActiveObjects(String nodeName, String className) throws ProActiveException {
        return target.getActiveObjects(nodeName, className);
    }

    public ProActiveDescriptorInternal getDescriptor(String url, boolean isHierarchicalSearch)
            throws IOException, ProActiveException {
        return target.getDescriptor(url, isHierarchicalSearch);
    }

    public FileTransferEngine getFileTransferEngine() {
        return target.getFileTransferEngine();
    }

    public ServerConnector getJMXServerConnector() {
        return target.getJMXServerConnector();
    }

    public String[] getLocalNodeNames() throws ProActiveException {
        return target.getLocalNodeNames();
    }

    public String getLocalNodeProperty(String nodeName, String key) throws ProActiveException {
        return target.getLocalNodeProperty(nodeName, key);
    }

    public ProActiveRuntimeWrapperMBean getMBean() {
        return target.getMBean();
    }

    public String getMBeanServerName() {
        return target.getMBeanServerName();
    }

    public ProActiveRuntime getProActiveRuntime(String proActiveRuntimeName) throws ProActiveException {
        return target.getProActiveRuntime(proActiveRuntimeName);
    }

    public ProActiveRuntime[] getProActiveRuntimes() throws ProActiveException {
        return target.getProActiveRuntimes();
    }

    public String getURL() {
        return this.url;
    }

    public VMInformation getVMInformation() {
        return this.vmInformation;
    }

    public String getVNName(String Nodename) throws ProActiveException {
        return target.getVNName(Nodename);
    }

    public VirtualNodeInternal getVirtualNode(String virtualNodeName) throws ProActiveException {
        return target.getVirtualNode(virtualNodeName);
    }

    public void killAllNodes() throws ProActiveException {
        target.killAllNodes();
    }

    public void killNode(String nodeName) throws ProActiveException {
        target.killNode(nodeName);
    }

    public void killRT(boolean softly) {
        target.killRT(softly);
    }

    public void launchMain(String className, String[] parameters) throws ClassNotFoundException,
            NoSuchMethodException, ProActiveException {
        target.launchMain(className, parameters);
    }

    public void newRemote(String className) throws ClassNotFoundException, ProActiveException {
        target.newRemote(className);
    }

    public void register(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl, String creatorID,
            String creationProtocol, String vmName) throws ProActiveException {
        target.register(proActiveRuntimeDist, proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
    }

    public void register(GCMRuntimeRegistrationNotificationData event) {
        target.register(event);
    }

    public void registerVirtualNode(String virtualNodeName, boolean replacePreviousBinding)
            throws ProActiveException, AlreadyBoundException {
        target.registerVirtualNode(virtualNodeName, replacePreviousBinding);
    }

    public Object setLocalNodeProperty(String nodeName, String key, String value) throws ProActiveException {
        return target.setLocalNodeProperty(nodeName, key, value);
    }

    public void startJMXServerConnector() {
        target.startJMXServerConnector();
    }

    public void unregister(ProActiveRuntime proActiveRuntimeDist, String proActiveRuntimeUrl,
            String creatorID, String creationProtocol, String vmName) throws ProActiveException {
        target.unregister(proActiveRuntimeDist, proActiveRuntimeUrl, creatorID, creationProtocol, vmName);
    }

    public void unregisterAllVirtualNodes() throws ProActiveException {
        target.unregisterAllVirtualNodes();
    }

    public void unregisterVirtualNode(String virtualNodeName) throws ProActiveException {
        target.unregisterVirtualNode(virtualNodeName);
    }

    public Node createGCMNode(String vnName, List<TechnicalService> tsList) throws NodeException,
            AlreadyBoundException {
        return target.createGCMNode(vnName, tsList);
    }

    public byte[] getClassData(String className) {
        return target.getClassData(className);
    }
}
