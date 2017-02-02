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
package org.objectweb.proactive.gcmdeployment;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContract;


/**
 * A GCM Application is an instance of a distributed application.
 *
 *
 * A GCM Application defines
 * 
 * A GCM Application is described by a GCM Application Descriptor XML file. This interface offers
 * some services to manipulate a GCM Application:
 * <ul>
 * <li>Application life cycle management: start or stop application deployment</li>
 * <li>Virtual Node abstraction</li>
 * <li>Node {@link Topology} of the application </li>
 * <li>application's statistics/metrics</li>
 * </ul>
 * 
 * All methods, excepted startDeployment, require a ProActive application to do something. If a non
 * ProActive is started then it cannot be managed.
 *
 * GCMApplications are exported as Remote Objects (RPC). It means they are remotely accessible and
 * never Serialized. To achieve good performances, heavy GCMApplication manipulations should
 * occur on the deployer ProActive Runtime since it does not involve remote calls.
 *
 * @since ProActive 4.0
 *
 * @see GCMVirtualNode
 * @see Topology
 */
@PublicAPI
public interface GCMApplication {
    public long getDeploymentId();

    /**
     * Starts the deployment of this application instance
     *
     * Processes described in the GCM Application Descriptor are started on remote resources
     * described by all GCM Deployment Descriptors XML files.
     *
     * If the GCM Application Descriptor describes a ProActive application. Then all the methods
     * 
     * Creates remote ProActive Runtimes and Nodes described in all the GCM Deployment Descriptors
     * referenced by the GCM Application Descriptor.
     *
     * Do nothing if the deployment is already started
     */
    public void startDeployment();

    /**
     * Indicates if the deployment is already started
     * 
     * @return true if startDeployment has already been called, false otherwise
     */
    public boolean isStarted();

    /**
     * Terminates all the ProActive Runtimes that have been started by this Application. Acquired
     * resources are freed too.
     *
     * If some Runtime have been started but have yet registered a race condition can occur. Their is
     * not way to solve this issue.
     */
    public void kill();

    /**
     * Wait each GCMVirtualNode become ready
     *
     * This method should be used carefully since it can hang forever. Safer, with timeout, fine
     * grained methods are available at GCMVirtualNode level.
     *
     * @see GCMVirtualNode
     */
    public void waitReady();

    /**
     * Wait each GCMVirtualNode become ready or timeout
     *
     * @param timeout The timeout in ms
     *  
     * @see GCMVirtualNode
     */
    public void waitReady(long timeout) throws ProActiveTimeoutException;

    /**
     * Returns the GCMVirtualNode associated to this identifier
     *
     * GCMVirtualNode are defined in GCM Application Descriptor XML file.
     * 
     * @param vnName
     *            a GCMVirtualNode name
     * @return A GCMVirtualNode is the identifier is known, null otherwise.
     */
    // @snippet-start GCMApplication_getVirtualNode
    public GCMVirtualNode getVirtualNode(String vnName);

    // @snippet-end GCMApplication_getVirtualNode

    public Set<String> getVirtualNodeNames();

    /**
     * Returns all the GCMVirtualNodes known by this application and their identifiers.
     * 
     * 
     * @return All the Virtual Nodes known by this application
     */
    // @snippet-start GCMApplication_getVirtualNodes
    public Map<String, GCMVirtualNode> getVirtualNodes();

    // @snippet-end GCMApplication_getVirtualNodes

    /**
     * Returns the variable contract associated to this application
     * 
     * @return The variable contract associated to this application
     */
    public VariableContract getVariableContract();

    /**
     * Returns all created or acquired Nodes
     * 
     * <b>This method is only available if no Virtual Node is defined in the GCM Application
     * Descriptor. An {@link IllegalStateException} is thrown if at least one GCMVirtualNode is
     * defined</b>
     * 
     * This method allows application to perform smarter Node allocation than NodeMapper and
     * GCMVirtualNodes can do. If your application needs a fine control on where active objects are
     * created (like advanced co-allocation), then you probably have to implement your own Node
     * allocator.
     * 
     * @return all the Nodes that belong to this GCM Application
     */
    public List<Node> getAllNodes();

    /**
     * Returns the topology of this GCM Application
     *
     * <b>This method is only available if no Virtual Node is defined in the GCM Application
     * Descriptor. An {@link IllegalStateException} is thrown if at least one GCMVirtualNode is
     * defined</b>
     *
     *
     * @return the topology of this GCM Application
     *
     * @see GCMVirtualNode
     * @see GCMVirtualNode#getCurrentTopology()
     * @see Topology
     */
    public Topology getTopology() throws ProActiveException;

    /**
     * Updates the Topology passed in parameter
     * 
     * <b>This method is only available if no Virtual Node is defined in the GCM Application
     * Descriptor. An {@link IllegalStateException} is thrown if at least one GCMVirtualNode is
     * defined</b>
     *
     * 
     * @param topology
     *            topology to be updated
     *
     * @see GCMVirtualNode
     * @see GCMVirtualNode#updateTopology(Topology)
     * @see Topology
     */
    public void updateTopology(Topology topology) throws ProActiveException;

    /**
     * Provide information about Nodes Status
     * 
     * If a {@link GCMVirtualNode} never become ready this method can be used to diagnosis the error
     * in GCM Application or GCM Deployment descriptor
     *
     * @return various information about the deployment of this application
     */
    public String getDebugInformation();

    /**
     * Returns the descriptor url associated to this Application
     * @return descriptor url
     */
    public URL getDescriptorURL();
}
