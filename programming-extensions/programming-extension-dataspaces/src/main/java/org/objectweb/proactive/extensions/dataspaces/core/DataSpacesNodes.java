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
package org.objectweb.proactive.extensions.dataspaces.core;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.AlreadyConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;


/**
 * Class that provides static methods for managing and accessing node and application specific Data
 * Spaces classes instances.
 * <p>
 * This class may be used for configuring Data Spaces nodes through {@link NodeConfigurator} and
 * accessing configured {@link DataSpacesImpl} instances.
 *
 * @see DataSpacesImpl
 * @see NodeConfigurator
 */
public class DataSpacesNodes {

    private final static Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES_CONFIGURATOR);

    private static Map<String, NodeConfigurator> nodeConfigurators = new HashMap<String, NodeConfigurator>();

    /**
     * Returns DataSpacesImpl instance for a node with configured application.
     *
     * This method is usable after setting up this node with
     * {@link #configureNode(Node, BaseScratchSpaceConfiguration)} and
     * {@link #configureApplication(Node, long, String)} calls.
     *
     * Returned instance is usable while node is kept configured for the application, with
     * particular application identifier associated to this node during that time.
     *
     * @param node
     *            node that is asked for Data Spaces implementation
     * @return configured Data Spaces implementation for application
     * @throws NotConfiguredException
     *             when node is not configured for Data Spaces at all or application-specific Data
     *             Spaces configuration is not applied on this node
     * @see NodeConfigurator#getDataSpacesImpl()
     */
    public static DataSpacesImpl getDataSpacesImpl(Node node) throws NotConfiguredException {
        final NodeConfigurator nodeConfig = getOrFailNodeConfigurator(node);

        final DataSpacesImpl impl = nodeConfig.getDataSpacesImpl();
        if (impl == null) {
            logger.debug("Requested Data Spaces implementation for node without DS configured");
            throw new NotConfiguredException("Node is not configured for DataSpaces application");
        }
        return impl;
    }

    /**
     * Configures Data Spaces on node and stores that configuration, so it can be later configured
     * for specific application by {@link #configureApplication(Node, long, String)} or closed by
     * {@link #closeNodeConfig(Node)}.
     *
     * @param node
     *            node to be configured for Data Spaces
     * @param baseScratchConfiguration
     *            base configuration of scratch data space for a specified node
     * @throws IllegalArgumentException
     *             when trying to configure node that is on different runtime/JVM
     * @throws FileSystemException
     *             when VFS configuration creation or scratch initialization fails
     * @throws ConfigurationException
     *             something failed during node scratch space configuration (ex. capabilities
     *             checking)
     * @throws AlreadyConfiguredException
     *             when node is already configured for Data Spaces
     * @see NodeConfigurator#configureNode(Node, BaseScratchSpaceConfiguration)
     */
    public static void configureNode(Node node, BaseScratchSpaceConfiguration baseScratchConfiguration)
            throws IllegalArgumentException, AlreadyConfiguredException, FileSystemException, ConfigurationException {
        if (!NodeFactory.isNodeLocal(node)) {
            logger.error("Node to configure is not on the same runtime/JVM as a caller");
            throw new IllegalArgumentException("Node to configure is not on the same runtime/JVM as a caller");
        }
        final NodeConfigurator nodeConfig = createNodeConfigurator(node);
        try {
            nodeConfig.configureNode(node, baseScratchConfiguration);
        } catch (IllegalStateException x) {
            logger.debug("Requested Data Spaces node configuration for already configured node");
            // it can occur only in case of concurrent configuration, let's wrap it
            throw new AlreadyConfiguredException(x.getMessage(), x);
        }
    }

    /**
     * @deprecated this method uses a single url to lookup the naming service. In case of multi-protocol,
     * it won't be able to react to multiple configurations. Use the method which takes a NamingService stub instead
     *
     * Configures Data Spaces node for a specific application and stores that configuration together
     * with Data Spaces implementation instance, so they can be later accessed by
     * {@link #getDataSpacesImpl(Node)} or closed through
     * {@link #tryCloseNodeApplicationConfig(Node)} or subsequent
     * {@link #configureApplication(Node, String, String)}.
     *
     * This method can be called on an already configured node (see
     * {@link #configureNode(Node, BaseScratchSpaceConfiguration)}) or even already
     * application-configured node - in that case previous application configuration is closed
     * before applying a new one.
     *
     * @param node
     *            node to be configured for Data Spaces application
     * @param appId
     *            identifier of application running on that node
     * @param namingServiceURL
     *            URL of a Naming Service to connect to
     * @throws URISyntaxException
     *             when exception occurred on namingServiceURL parsing
     * @throws ProActiveException
     *             occurred during contacting with NamingService
     * @throws NotConfiguredException
     *             when node is not configured for Data Spaces
     * @throws FileSystemException
     *             VFS related exception during scratch data space creation
     * @see NodeConfigurator#configureApplication(String, String)
     */
    @Deprecated
    public static void configureApplication(Node node, String appId, String namingServiceURL)
            throws ProActiveException, NotConfiguredException, URISyntaxException, FileSystemException {
        final NodeConfigurator nodeConfig = getOrFailNodeConfigurator(node);
        try {
            nodeConfig.configureApplication(appId, namingServiceURL);
        } catch (IllegalStateException x) {
            logger.debug("Requested Data Spaces node application configuration for not configured node");
            // it can occur only in case of concurrent configuration, let's wrap it
            throw new NotConfiguredException(x.getMessage(), x);
        }
    }

    /**
     * Configures Data Spaces node for a specific application and stores that configuration together
     * with Data Spaces implementation instance, so they can be later accessed by
     * {@link #getDataSpacesImpl(Node)} or closed through
     * {@link #tryCloseNodeApplicationConfig(Node)} or subsequent
     * {@link #configureApplication(Node, String, String)}.
     *
     * This method can be called on an already configured node (see
     * {@link #configureNode(Node, BaseScratchSpaceConfiguration)}) or even already
     * application-configured node - in that case previous application configuration is closed
     * before applying a new one.
     *
     * @param node
     *            node to be configured for Data Spaces application
     * @param appId
     *            identifier of application running on that node
     * @param namingServiceStub
     *            stub of a Naming Service to connect to
     * @throws URISyntaxException
     *             when exception occurred on namingServiceURL parsing
     * @throws ProActiveException
     *             occurred during contacting with NamingService
     * @throws NotConfiguredException
     *             when node is not configured for Data Spaces
     * @throws FileSystemException
     *             VFS related exception during scratch data space creation
     * @see NodeConfigurator#configureApplication(String, String)
     */
    public static void configureApplication(Node node, String appId, NamingService namingServiceStub)
            throws ProActiveException, NotConfiguredException, URISyntaxException, FileSystemException {
        final NodeConfigurator nodeConfig = getOrFailNodeConfigurator(node);
        try {
            nodeConfig.configureApplication(appId, namingServiceStub);
        } catch (IllegalStateException x) {
            logger.debug("Requested Data Spaces node application configuration for not configured node");
            // it can occur only in case of concurrent configuration, let's wrap it
            throw new NotConfiguredException(x.getMessage(), x);
        }
    }

    /**
     * Closes all node related configuration (possibly including application related node
     * configuration).
     *
     * Subsequent calls for node that is not configured anymore may result in undefined behavior.
     *
     * @param node
     *            node to be deconfigured for Data Spaces
     * @throws NotConfiguredException
     *             when node has not been configured yet
     */
    public static void closeNodeConfig(Node node) throws NotConfiguredException {
        final NodeConfigurator nodeConfig = removeOrFailNodeConfigurator(node);
        try {
            nodeConfig.close();
        } catch (IllegalStateException x) {
            logger.debug("Requested Data Spaces configuration close for not configured node");
            // it can occur only in case of concurrent configuration, let's wrap it
            throw new NotConfiguredException(x.getMessage(), x);
        }
    }

    /**
     * Closes all application related configuration for node if there is one.
     *
     * @param node
     *            node to be deconfigured for Data Spaces application
     */
    public static void tryCloseNodeApplicationConfig(Node node) {
        final NodeConfigurator nodeConfig = getNodeConfigurator(node);
        if (nodeConfig != null)
            nodeConfig.tryCloseAppConfigurator();
    }

    private static NodeConfigurator createNodeConfigurator(Node node) throws AlreadyConfiguredException {
        final String name = Utils.getNodeId(node);

        synchronized (nodeConfigurators) {
            final NodeConfigurator config = nodeConfigurators.get(name);
            if (config != null) {
                logger.debug("Attempted to create Data Spaces node configurator for already being configured node");
                throw new AlreadyConfiguredException("Node is already configured for Data Spaces");
            }

            final NodeConfigurator newConfig = new NodeConfigurator();
            nodeConfigurators.put(name, newConfig);
            return newConfig;
        }
    }

    private static NodeConfigurator getNodeConfigurator(Node node) {
        final String name = Utils.getNodeId(node);

        synchronized (nodeConfigurators) {
            return nodeConfigurators.get(name);
        }
    }

    private static NodeConfigurator getOrFailNodeConfigurator(Node node) throws NotConfiguredException {
        final String name = Utils.getNodeId(node);

        synchronized (nodeConfigurators) {
            final NodeConfigurator config = nodeConfigurators.get(name);
            if (config == null) {
                logger.debug("Attempted to get Data Spaces node configurator for not configured node");
                throw new NotConfiguredException("Node is not configured");
            }

            return config;
        }
    }

    private static NodeConfigurator removeOrFailNodeConfigurator(Node node) throws NotConfiguredException {
        final String name = Utils.getNodeId(node);

        synchronized (nodeConfigurators) {
            final NodeConfigurator config = nodeConfigurators.remove(name);
            if (config == null) {
                logger.debug("Attempted to remove Data Spaces node configurator for not configured node");
                throw new NotConfiguredException("Node is not configured");
            }

            return config;
        }
    }
}
