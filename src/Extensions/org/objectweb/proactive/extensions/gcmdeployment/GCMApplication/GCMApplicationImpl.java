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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollectorDeployer;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ApplicationAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;
import org.objectweb.proactive.extensions.dataspaces.service.DataSpacesTechnicalService;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.GCMDeploymentDescriptor;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.GCMDeploymentDescriptorImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.GCMDeploymentResources;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm.GCMVirtualMachineManager;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm.VMBean;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeInternal;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeRemoteObjectAdapter;
import org.objectweb.proactive.extensions.gcmdeployment.core.TopologyImpl;
import org.objectweb.proactive.extensions.gcmdeployment.core.TopologyRootImpl;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;
import org.objectweb.proactive.utils.TimeoutAccounter;


public class GCMApplicationImpl implements GCMApplicationInternal {
    static private Map<Long, GCMApplication> localDeployments = new HashMap<Long, GCMApplication>();

    /** Flag to store information whether DataSpaces were already on configured for some GCMA on JVM */
    private static boolean dataSpacesConfiguredOnJVM;

    /** Lock for an above flag */
    private static Object dataSpacesConfiguredOnJVMLock = new Object();

    /** An unique identifier for this deployment */
    private long deploymentId;

    /** descriptor file */
    private URL descriptor = null;

    /** GCM Application parser (statefull) */
    private GCMApplicationParser parser = null;

    /** All Node Providers referenced by the Application descriptor */
    private Map<String, NodeProvider> nodeProviders = null;

    /** Defined Virtual Nodes */
    private Map<String, GCMVirtualNodeInternal> virtualNodes = null;
    private Map<String, GCMVirtualNode> ROvirtualNodes = null;

    /** The Deployment Tree */
    private TopologyRootImpl deploymentTree;

    /** A mapping to associate deployment IDs to Node Provider */
    private Map<Long, NodeProvider> topologyIdToNodeProviderMapping;

    /** The Command builder to use to start the deployment */
    private CommandBuilder commandBuilder;

    /** The node allocator in charge of Node dispatching */
    private NodeMapper nodeMapper;
    private ArrayList<String> currentDeploymentPath;

    /** All the nodes created by this GCM Application */
    private List<Node> nodes;

    /** All the runtime created by this GCM Application */
    private Queue<ProActiveRuntime> deployedRuntimes;
    private Object deploymentMutex = new Object();
    private boolean isStarted;
    private boolean isKilled;
    private ProActiveSecurityManager proactiveApplicationSecurityManager;

    private VariableContractImpl vContract;

    final private ProActiveLogCollectorDeployer logCollector;

    /** Whether application requests Data Spaces usage or not */
    private boolean dataSpacesEnabled;

    /** Configurations of input and output spaces for application */
    private Set<InputOutputSpaceConfiguration> spacesConfigurations;

    /** URL of Data Spaces Naming Service */
    private String namingServiceURL;

    /** Deployer of Naming Service if it was requested to be started locally */
    private NamingServiceDeployer namingServiceDeployer;

    /** Stub to Data Spaces Naming Service */
    private NamingService namingService;

    static public GCMApplication getLocal(long deploymentId) {
        return localDeployments.get(deploymentId);
    }

    public GCMApplicationImpl(String filename) throws ProActiveException, MalformedURLException {
        this(new URL("file", null, filename), null);
    }

    public GCMApplicationImpl(String filename, VariableContractImpl vContract) throws ProActiveException,
            MalformedURLException {
        this(new URL("file", null, filename), vContract);
    }

    public GCMApplicationImpl(URL file) throws ProActiveException {
        this(file, null);
    }

    public GCMApplicationImpl(URL file, VariableContractImpl vContract) throws ProActiveException {
        if (file == null) {
            throw new ProActiveException("Failed to create GCM Application: URL cannot be null !");
        }

        try {
            file.openStream();
        } catch (IOException e) {
            throw new ProActiveException("Failed to create GCM Application: URL " + file.toString() +
                " cannot be opened");
        }

        try {

            deploymentId = ProActiveRandom.nextPosLong();
            localDeployments.put(deploymentId, this);

            currentDeploymentPath = new ArrayList<String>();
            topologyIdToNodeProviderMapping = new HashMap<Long, NodeProvider>();
            nodes = new LinkedList<Node>();
            deployedRuntimes = new ConcurrentLinkedQueue<ProActiveRuntime>();
            isStarted = false;
            isKilled = false;

            if (vContract == null) {
                vContract = new VariableContractImpl();
            }
            this.vContract = vContract;

            descriptor = file;
            // vContract will be modified by the Parser to include variable defined in the descriptor
            parser = new GCMApplicationParserImpl(descriptor, this.vContract);
            nodeProviders = parser.getNodeProviders();
            virtualNodes = parser.getVirtualNodes();
            commandBuilder = parser.getCommandBuilder();
            nodeMapper = new NodeMapper(this, virtualNodes.values());

            proactiveApplicationSecurityManager = parser.getProactiveApplicationSecurityManager();

            dataSpacesEnabled = parser.isDataSpacesEnabled();
            spacesConfigurations = parser.getInputOutputSpacesConfigurations();
            namingServiceURL = parser.getDataSpacesNamingServiceURL();

            this.vContract.close();

            TechnicalServicesProperties appTSProperties = parser.getAppTechnicalServices();

            // always start Data Spaces BEFORE applying tech services on local node
            // (to provide working Data Spaces, DataSpacesTechnicalService assumes that appId is known and 
            // application is registered in working NamingService) 
            if (dataSpacesEnabled) {
                synchronized (dataSpacesConfiguredOnJVMLock) {
                    if (dataSpacesConfiguredOnJVM) {
                        GCMA_LOGGER.error("DataSpaces were already configured for this JVM"
                            + " for different GCM application, they cannot be configured again");
                        dataSpacesEnabled = false;
                    } else {
                        dataSpacesConfiguredOnJVM = true;
                    }
                }
            }
            if (dataSpacesEnabled) {
                startDataSpaces();

                // we need to add technical service properties here, as we already know the application id
                // (deploymentId) and NamingService URL
                // TODO this kind of hacks should be eventually moved to CommandBuilderProActive#setup()
                // or similar developed mechanism
                final TechnicalServicesProperties dataSpacesTSP = DataSpacesTechnicalService
                        .createTechnicalServiceProperties(deploymentId, namingServiceURL);
                for (GCMVirtualNodeInternal vn : virtualNodes.values()) {
                    vn.addTechnicalServiceProperties(dataSpacesTSP);
                }
                appTSProperties = appTSProperties.getCombinationWith(dataSpacesTSP);
            }

            // apply Application-wide tech services on local node
            //
            Node defaultNode = NodeFactory.getDefaultNode();
            Node halfBodiesNode = NodeFactory.getHalfBodiesNode();

            for (Map.Entry<String, HashMap<String, String>> tsp : appTSProperties) {

                TechnicalService ts = TechnicalServicesFactory.create(tsp.getKey(), tsp.getValue());
                if (ts != null) {
                    ts.apply(defaultNode);
                    ts.apply(halfBodiesNode);
                }
            }

            this.ROvirtualNodes = new HashMap<String, GCMVirtualNode>();
            for (GCMVirtualNode vn : this.virtualNodes.values()) {
                this.ROvirtualNodes.put(vn.getName(), vnAsRemoteObject(vn));
            }

            this.logCollector = new ProActiveLogCollectorDeployer(this.deploymentId + "/logCollector");

        } catch (Exception e) {
            throw new ProActiveException("Failed to create GCMApplication: " + e.getMessage() +
                ", see embded message for more details", e);
        }
    }

    /*
     * ----------------------------- GCMApplicationDescriptor interface
     */
    public void startDeployment() {
        synchronized (deploymentMutex) {
            if (isStarted) {
                GCMA_LOGGER.warn("A GCM Application descriptor cannot be started twice", new Exception());
                return;
            }

            isStarted = true;

            deploymentTree = buildDeploymentTree();
            for (GCMVirtualNodeInternal virtualNode : virtualNodes.values()) {
                virtualNode.setDeploymentTree(deploymentTree);
            }

            for (NodeProvider nodeProvider : nodeProviders.values()) {
                nodeProvider.start(commandBuilder, this);
            }
        }
    }

    public boolean isStarted() {
        synchronized (deploymentMutex) {
            return isStarted;
        }
    }

    private GCMVirtualNode vnAsRemoteObject(GCMVirtualNode vn) {
        // Export this GCMApplication as a remote object
        String name = this.getDeploymentId() + "/VirtualNode/" + vn.getName();
        RemoteObjectExposer<GCMVirtualNode> roe = new RemoteObjectExposer<GCMVirtualNode>(name,
            GCMVirtualNode.class.getName(), vn, GCMVirtualNodeRemoteObjectAdapter.class);
        try {
            roe.createRemoteObject(name, false);
            return (GCMVirtualNode) RemoteObjectHelper.generatedObjectStub(roe.getRemoteObject());
        } catch (ProActiveException e) {
            GCMA_LOGGER.error(e);
            return null;
        }
    }

    public GCMVirtualNode getVirtualNode(String vnName) {
        return this.ROvirtualNodes.get(vnName);
    }

    public Map<String, GCMVirtualNode> getVirtualNodes() {
        return this.ROvirtualNodes;
    }

    /**
     * Kills every registered PART &
     * every launched virtual machines. It also asks every registered hypervisors
     * to destroy cloned virtual machines to let environment as clean as it was
     * when we launched the deployment
     * ( see {@link AbstractVMM#stop()} ).
     */
    public void kill() {
        isKilled = true;
        for (ProActiveRuntime part : deployedRuntimes) {
            try {
                part.killRT(false);
            } catch (Exception e) {
                // Connection between the two runtimes will be interrupted 
                // Eat the exception: Miam Miam Miam
            }
        }

        Set<String> keys = nodeProviders.keySet();
        for (String key : keys) {
            NodeProvider np = nodeProviders.get(key);
            for (GCMDeploymentDescriptor dd : np.getDescriptors()) {
                try {
                    GCMDeploymentResources resources = dd.getResources();
                    for (GCMVirtualMachineManager vmm : resources.getVMM()) {
                        vmm.stop();
                    }
                } catch (Exception e) {
                    GCMA_LOGGER.warn("GCM Deployment failed to clean the virtual machine environment.", e);
                }
            }
        }

        if (dataSpacesEnabled) {
            stopDataSpaces();
        }
    }

    public Topology getTopology() throws ProActiveException {
        if (!virtualNodes.isEmpty())
            throw new ProActiveException("getTopology cannot be called if a VirtualNode is defined");

        this.updateNodes();
        // To not block other threads too long we make a snapshot of the node set
        Set<Node> nodesCopied;
        synchronized (nodes) {
            nodesCopied = new HashSet<Node>(nodes);
        }
        return TopologyImpl.createTopology(deploymentTree, nodesCopied);
    }

    public List<Node> getAllNodes() {
        if (virtualNodes.size() != 0) {
            throw new IllegalStateException("getAllNodes cannot be called if a VirtualNode is defined");
        }

        this.updateNodes();
        return nodes;
    }

    public String getDebugInformation() {
        Set<FakeNode> fakeNodes = nodeMapper.getUnusedNode(false);
        StringBuilder sb = new StringBuilder();
        sb.append("Number of unmapped nodes: " + fakeNodes.size() + "\n");
        for (FakeNode fakeNode : fakeNodes) {
            sb.append("\t" + fakeNode.getRuntimeURL() + "(capacity=" + fakeNode.getCapacity() + ")\n");
        }
        return sb.toString();
    }

    public void updateTopology(Topology topology) throws ProActiveException {
        if (!virtualNodes.isEmpty())
            throw new ProActiveException("updateTopology cannot be called if a VirtualNode is defined");

        this.updateNodes();
        // To not block other threads too long we make a snapshot of the node set
        Set<Node> nodesCopied;
        synchronized (nodes) {
            nodesCopied = new HashSet<Node>(nodes);
        }
        TopologyImpl.updateTopology(topology, nodesCopied);
    }

    public VariableContractImpl getVariableContract() {
        return this.vContract;
    }

    public URL getDescriptorURL() {
        return descriptor;
    }

    /*
     * ----------------------------- GCMApplicationDescriptorInternal interface
     */
    public long getDeploymentId() {
        return deploymentId;
    }

    public NodeProvider getNodeProviderFromTopologyId(Long topologyId) {
        return topologyIdToNodeProviderMapping.get(topologyId);
    }

    public void addNode(Node node) {
        synchronized (nodes) {
            nodes.add(node);
        }
    }

    public void addDeployedRuntime(ProActiveRuntime part) {
        if (isKilled) {
            try {
                part.killRT(false);
            } catch (Exception e) {
                // Connection between the two runtimes will be interrupted 
                // Eat the exception: Miam Miam Miam
            }
        } else {
            deployedRuntimes.add(part);
        }
    }

    /*
     * ----------------------------- Internal Methods
     */
    protected TopologyRootImpl buildDeploymentTree() {
        // make root node from local JVM
        TopologyRootImpl rootNode = new TopologyRootImpl();

        ProActiveRuntimeImpl proActiveRuntime = ProActiveRuntimeImpl.getProActiveRuntime();
        currentDeploymentPath.clear();
        pushDeploymentPath(proActiveRuntime.getVMInformation().getName());

        rootNode.setDeploymentDescriptorPath("none"); // no deployment descriptor here

        rootNode.setApplicationDescriptorPath(descriptor.toExternalForm());

        rootNode.setDeploymentPath(getCurrentDeploymentPath());
        popDeploymentPath();

        // Build leaf nodes
        for (NodeProvider nodeProvider : nodeProviders.values()) {
            for (GCMDeploymentDescriptor gdd : nodeProvider.getDescriptors()) {
                GCMDeploymentDescriptorImpl gddi = (GCMDeploymentDescriptorImpl) gdd;
                GCMDeploymentResources resources = gddi.getResources();

                HostInfo hostInfo = resources.getHostInfo();
                if (hostInfo != null) {
                    buildHostInfoTreeNode(rootNode, rootNode, hostInfo, nodeProvider, gdd);
                }

                for (Group group : resources.getGroups()) {
                    buildGroupTreeNode(rootNode, rootNode, group, nodeProvider, gdd);
                }

                for (Bridge bridge : resources.getBridges()) {
                    buildBridgeTree(rootNode, rootNode, bridge, nodeProvider, gdd);
                }

                TopologyImpl node = new TopologyImpl();//a unique topologyID for all vms...
                for (GCMVirtualMachineManager vmm : resources.getVMM()) {
                    buildVMMTree(rootNode, rootNode, vmm, nodeProvider, gdd, node);
                }
            }
        }

        return rootNode;
    }

    /**
     * return a copy of the current deployment path
     * 
     * @return
     */
    private List<String> getCurrentDeploymentPath() {
        return new ArrayList<String>(currentDeploymentPath);
    }

    private TopologyImpl buildHostInfoTreeNode(TopologyRootImpl rootNode, TopologyImpl parentNode,
            HostInfo hostInfo, NodeProvider nodeProvider, GCMDeploymentDescriptor gcmd) {
        pushDeploymentPath(hostInfo.getId());
        TopologyImpl node = new TopologyImpl();
        node.setDeploymentDescriptorPath(gcmd.getDescriptorURL().toExternalForm());
        node.setApplicationDescriptorPath(rootNode.getApplicationDescriptorPath());
        node.setDeploymentPath(getCurrentDeploymentPath());
        node.setNodeProvider(nodeProvider.getId());
        hostInfo.setTopologyId(node.getId());
        topologyIdToNodeProviderMapping.put(node.getId(), nodeProvider);
        rootNode.addNode(node, parentNode);
        popDeploymentPath(); // ???
        return node;
    }

    private void buildGroupTreeNode(TopologyRootImpl rootNode, TopologyImpl parentNode, Group group,
            NodeProvider nodeProvider, GCMDeploymentDescriptor gcmd) {
        pushDeploymentPath(group.getId());
        buildHostInfoTreeNode(rootNode, parentNode, group.getHostInfo(), nodeProvider, gcmd);
        popDeploymentPath();
    }

    private void buildBridgeTree(TopologyRootImpl rootNode, TopologyImpl parentNode, Bridge bridge,
            NodeProvider nodeProvider, GCMDeploymentDescriptor gcmd) {
        pushDeploymentPath(bridge.getId());

        TopologyImpl node = parentNode;

        // first look for a host info...
        //
        if (bridge.getHostInfo() != null) {
            HostInfo hostInfo = bridge.getHostInfo();
            node = buildHostInfoTreeNode(rootNode, parentNode, hostInfo, nodeProvider, gcmd);
        }

        // then groups...
        //
        if (bridge.getGroups() != null) {
            for (Group group : bridge.getGroups()) {
                buildGroupTreeNode(rootNode, node, group, nodeProvider, gcmd);
            }
        }

        // then bridges (and recurse)
        if (bridge.getBridges() != null) {
            for (Bridge subBridge : bridge.getBridges()) {
                buildBridgeTree(rootNode, node, subBridge, nodeProvider, gcmd);
            }
        }

        popDeploymentPath();
    }

    /**
     * To build vmm tag associated deploymentTree.
     * @param rootNode
     * @param rootNode2
     * @param vmm
     * @param nodeProvider
     * @param gcmd
     */
    private void buildVMMTree(TopologyRootImpl rootNode, TopologyRootImpl parentNode,
            GCMVirtualMachineManager vmm, NodeProvider nodeProvider, GCMDeploymentDescriptor gcmd,
            TopologyImpl node) {
        pushDeploymentPath(vmm.getId());
        for (VMBean vm : vmm.getVms()) {
            node.setDeploymentDescriptorPath(gcmd.getDescriptorURL().toExternalForm());
            node.setApplicationDescriptorPath(rootNode.getApplicationDescriptorPath());
            node.setDeploymentPath(getCurrentDeploymentPath());
            node.setNodeProvider(nodeProvider.getId());
            topologyIdToNodeProviderMapping.put(node.getId(), nodeProvider);
            vm.setNode(node);
        }
        rootNode.addNode(node, parentNode);
        popDeploymentPath(); // ???
    }

    private boolean pushDeploymentPath(String pathElement) {
        return currentDeploymentPath.add(pathElement);
    }

    private void popDeploymentPath() {
        currentDeploymentPath.remove(currentDeploymentPath.size() - 1);
    }

    public void waitReady(long timeout) throws ProActiveTimeoutException {
        TimeoutAccounter time = TimeoutAccounter.getAccounter(timeout);
        for (GCMVirtualNode vn : virtualNodes.values()) {
            try {
                vn.waitReady(time.getRemainingTimeout());
            } catch (ProActiveTimeoutException e) {
                if (time.isTimeoutElapsed()) { // should always be true
                    StringBuilder sb = new StringBuilder();
                    sb.append("Timeout reached while waiting for all virtual nodes to be ready.");
                    for (GCMVirtualNode v : virtualNodes.values()) {
                        sb.append(" ");
                        sb.append(v.getName());
                        sb.append(": ");
                        sb.append(v.isReady());
                        throw new ProActiveTimeoutException(sb.toString());
                    }
                }
            }
        }
    }

    public void waitReady() {
        for (GCMVirtualNode vn : virtualNodes.values()) {
            vn.waitReady();
        }
    }

    public Set<String> getVirtualNodeNames() {
        return new HashSet<String>(ROvirtualNodes.keySet());
    }

    public ProActiveSecurityManager getProActiveApplicationSecurityManager() {
        return proactiveApplicationSecurityManager;
    }

    public void setProActiveApplicationSecurityManager(
            ProActiveSecurityManager proactiveApplicationSecurityManager) {
        this.proactiveApplicationSecurityManager = proactiveApplicationSecurityManager;
    }

    /*
     * MUST NOT BE USED IF A VIRTUAL NODE IS DEFINED
     * 
     * Asks all unused fakeNodes to the node mapper and creates corresponding nodes.
     */
    private void updateNodes() {
        Set<FakeNode> fakeNodes = nodeMapper.getUnusedNode(true);
        for (FakeNode fakeNode : fakeNodes) {
            try {
                // create should not be synchronized since it's remote call
                Node node = fakeNode.create(GCMVirtualNodeImpl.DEFAULT_VN, null);
                synchronized (nodes) {
                    nodes.add(node);
                }
            } catch (NodeException e) {
                GCMA_LOGGER.warn("GCM Deployment failed to create a node on " + fakeNode.getRuntimeURL() +
                    ". Please check your network configuration", e);
            }
        }
    }

    public String getLogCollectorUrl() {
        return this.logCollector.getCollectorURL();
    }

    private void startDataSpaces() throws ProActiveException {
        if (namingServiceURL == null) {
            namingServiceDeployer = new NamingServiceDeployer(this.deploymentId + "/namingService");
            namingServiceURL = namingServiceDeployer.getNamingServiceURL();
            if (GCMA_LOGGER.isDebugEnabled()) {
                GCMA_LOGGER.debug("Started Naming Service at URL: " + namingServiceURL);
            }
        }

        try {
            namingService = NamingService.createNamingServiceStub(namingServiceURL);
        } catch (ProActiveException e) {
            GCMA_LOGGER.error("Cannot connect to Naming Service at URL: " + namingServiceURL, e);
            return;
        } catch (URISyntaxException e) {
            GCMA_LOGGER.error("Invalid syntax of provided Naming Service URL: " + namingServiceURL, e);
            return;
        }

        Set<SpaceInstanceInfo> spacesInstances = null;
        if (spacesConfigurations != null) {
            spacesInstances = new HashSet<SpaceInstanceInfo>();
            for (final InputOutputSpaceConfiguration config : spacesConfigurations) {
                try {
                    spacesInstances.add(new SpaceInstanceInfo(deploymentId, config));
                } catch (ConfigurationException e) {
                    ProActiveLogger.logImpossibleException(GCMA_LOGGER, e);
                }
            }
        }

        try {
            namingService.registerApplication(deploymentId, spacesInstances);
        } catch (ApplicationAlreadyRegisteredException e) {
            GCMA_LOGGER.error(
                    String.format("Application with id=%d is already registered in specified Naming Serivce",
                            deploymentId), e);
        } catch (WrongApplicationIdException e) {
            ProActiveLogger.logImpossibleException(GCMA_LOGGER, e);
        }
    }

    private void stopDataSpaces() {
        if (namingService != null) {
            try {
                namingService.unregisterApplication(deploymentId);
            } catch (WrongApplicationIdException e) {
                ProActiveLogger.logImpossibleException(GCMA_LOGGER, e);
            }
        }

        if (namingServiceDeployer != null) {
            try {
                namingServiceDeployer.terminate();
                if (GCMA_LOGGER.isDebugEnabled()) {
                    GCMA_LOGGER.debug("Stopped Naming Service at URL: " + namingServiceURL);
                }
            } catch (ProActiveException e) {
                GCMA_LOGGER.error("Cannot stop started Data Spaces Naming Service cleanly", e);
            }
        }
    }
}
