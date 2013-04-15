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
package org.objectweb.proactive.extensions.dataspaces.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.core.naming.CachingSpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSNodeScratchSpaceImpl;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSSpacesMountManagerImpl;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;


/**
 * Represents immutable Data Spaces configuration for a node. It manages both node-specific and
 * application-specific configuration resulting in Data Spaces implementation for application (
 * {@link DataSpacesImpl}).
 * <p>
 * Objects life cycle:
 * <ol>
 * <li>Instance initialization by default constructor.</li>
 * <li>{@link #configureNode(Node, BaseScratchSpaceConfiguration)} method call for passing
 * node-specific and immutable settings. This can be called only once for each instance.</li>
 * <li>{@link #configureApplication(long, String)} method call for configuring application on a
 * node.</li>
 * <li>Obtaining {@link DataSpacesImpl} from application configuration if needed, by
 * {@link #getDataSpacesImpl()}.</li>
 * <li>Possibly subsequent {@link #configureApplication(long, String)} calls reconfiguring node for
 * given application.</li>
 * <li>Closing all created objects by {@link #close()} method call.</li>
 * </ol>
 * <p>
 * Instances of this class are thread-safe. They can be managed by {@link DataSpacesNodes} static
 * class or in some other way. It is assumed that Node's application will not change between
 * {@link #configureApplication(long, String)} and {@link #tryCloseAppConfigurator()} calls.
 *
 * @see DataSpacesImpl
 */
public class NodeConfigurator {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES_CONFIGURATOR);

    private boolean configured;

    private NodeScratchSpace nodeScratchSpace;

    private NodeApplicationConfigurator appConfigurator;

    private FileSystemServerDeployer providerServerDeployer;

    private Node node;

    /**
     * Set node-specific immutable settings and initialize components. This method must be called
     * exactly once for each instance.
     * <p>
     * Scratch space configuration is checked and initialized.
     * <p>
     * State of an instance remains not configured if exception appears.
     *
     * @param node
     *            node to configure
     * @param baseScratchConfiguration
     *            base scratch data space configuration, may be <code>null</code> if node does not
     *            provide a scratch space; not existing directory pointed by this configuration will
     *            be created
     * @throws IllegalStateException
     *             when trying to reconfigure already configured instance
     * @throws IllegalArgumentException
     *             when trying to configure node that is on different runtime/JVM
     * @throws ConfigurationException
     *             when configuration appears to be wrong during node scratch space initialization
     *             (e.g. capabilities checking)
     * @throws FileSystemException
     *             when VFS creation or scratch initialization fails
     */
    synchronized public void configureNode(Node node, BaseScratchSpaceConfiguration baseScratchConfiguration)
            throws IllegalStateException, IllegalArgumentException, FileSystemException,
            ConfigurationException {
        logger.debug("Configuring node for Data Spaces");
        checkNotConfigured();
        if (!NodeFactory.isNodeLocal(node)) {
            logger.error("Node to configure is not on the same runtime/JVM as a caller");
            throw new IllegalArgumentException("Node to configure is not on the same runtime/JVM as a caller");
        }

        this.node = node;
        try {
            if (baseScratchConfiguration != null) {
                if (baseScratchConfiguration.getUrl() == null) {
                    baseScratchConfiguration = startProActiveProviderServer(baseScratchConfiguration);
                }

                final NodeScratchSpace configuringScratchSpace = new VFSNodeScratchSpaceImpl();
                configuringScratchSpace.init(node, baseScratchConfiguration);
                this.nodeScratchSpace = configuringScratchSpace;
            }
            configured = true;
        } finally {
            if (!configured) {
                tryCloseProviderServer();
                // node scratch space is not configured (does not need close) for sure
            }
        }
        logger.debug("Node configured for Data Spaces");
    }

    /**
     * Configures node for a specific application with its identifier, resulting in creation of
     * configured {@link DataSpacesImpl}.
     * <p>
     * Configuration of a node for an application involves association to provided NamingService and
     * registration of application scratch space for this node, if it exists.
     * <p>
     * This method may be called several times for different applications, after node has been
     * configured through {@link #configureNode(Node, BaseScratchSpaceConfiguration)}. Subsequent
     * calls will close existing application-specific configuration and create a new one.
     * <p>
     * If configuration fails, instance of this class remains not configured for an application, any
     * subsequent {@link #getDataSpacesImpl()} call will throw {@link IllegalStateException} until
     * successful configuration.
     *
     * @param appId
     *            id of application running on this node
     * @param namingServiceURL
     *            URL of naming service remote object for that application
     * @throws IllegalStateException
     *             when node has not been configured yet in terms of node-specific configuration
     * @throws URISyntaxException
     *             when exception occurred on namingServiceURL parsing
     * @throws ProActiveException
     *             when exception occurred during contacting with NamingService
     * @throws ConfigurationException
     *             when space appears to be already registered or application is not registered in
     *             Naming Service
     * @throws FileSystemException
     *             VFS related exception during scratch data space creation
     */
    synchronized public void configureApplication(long appId, String namingServiceURL)
            throws IllegalStateException, FileSystemException, ProActiveException, ConfigurationException,
            URISyntaxException {
        logger.debug("Configuring node for Data Spaces application");
        checkConfigured();

        tryCloseAppConfigurator();
        appConfigurator = new NodeApplicationConfigurator();
        boolean appConfigured = false;
        try {
            appConfigurator.configure(appId, namingServiceURL);
            appConfigured = true;
        } finally {
            if (!appConfigured)
                appConfigurator = null;
        }
        logger.debug("Node configured for Data Spaces application");
    }

    /**
     * Returns Data Spaces implementation for an application, if it has been successfully
     * configured.
     *
     * @return configured implementation of Data Spaces for application or <code>null</code> when
     *         node has not been configured yet (in terms of node-specific or application-specific
     *         configuration)
     */
    synchronized public DataSpacesImpl getDataSpacesImpl() {
        if (appConfigurator == null) {
            logger.debug("Requested unavailable Data Spaces implementation for an application");
            return null;
        }
        return appConfigurator.getDataSpacesImpl();
    }

    /**
     * Closes all resources opened by this configurator, also possibly created application
     * configuration.
     * <p>
     * Any subsequent call on node configuration-specific objects after calling this method may
     * result in undefined behavior.
     *
     * @throws IllegalStateException
     *             when node is not configured in terms of node-specific configuration
     */
    synchronized public void close() throws IllegalStateException {
        logger.debug("Closing Data Spaces node configuration");
        checkConfigured();

        tryCloseAppConfigurator();
        tryCloseProviderServer();
        if (nodeScratchSpace != null)
            nodeScratchSpace.close();
        nodeScratchSpace = null;
        configured = false;
        logger.debug("Data Space node configuration closed, resources released");
    }

    /**
     * Closes application-specific configuration when needed (there is one opened).
     * <p>
     * That involves unregistering application scratch space from NamingService and closing all
     * objects configured for produced {@link DataSpacesImpl}. {@link DataSpacesImpl} will not be
     * usable after this call.
     * <p>
     * If no application is configured, it does nothing. If closing fails, application-specific
     * configuration will be silently deleted.
     */
    public synchronized void tryCloseAppConfigurator() {
        if (appConfigurator == null)
            return;
        logger.debug("Closing Data Spaces application node configuration");
        appConfigurator.close();
        appConfigurator = null;
        logger.debug("Closed Data Spaces application node configuration");
    }

    private void tryCloseProviderServer() {
        if (providerServerDeployer == null)
            return;
        try {
            providerServerDeployer.terminate();
        } catch (ProActiveException e) {
            ProActiveLogger.logEatedException(logger, "Could not close correctly the ProActive provider", e);
        }
        providerServerDeployer = null;
    }

    private BaseScratchSpaceConfiguration startProActiveProviderServer(
            BaseScratchSpaceConfiguration baseScratchConfiguration) throws FileSystemException,
            ConfigurationException {

        final String rootPath = baseScratchConfiguration.getPath();
        final File rootFile = new File(rootPath);

        try {
            if (!rootFile.isDirectory())
                rootFile.mkdirs();
        } catch (SecurityException x) {
            throw new FileSystemException(x);
        }
        try {
            final String serviceId = Utils.getRuntimeId(node) + '/' + Utils.getNodeId(node) +
                "/fileSystemServer";
            providerServerDeployer = new FileSystemServerDeployer(serviceId, rootPath, true);
        } catch (IOException e) {
            throw new FileSystemException(e);
        }
        final String vfsRootURL = providerServerDeployer.getVFSRootURL();
        return baseScratchConfiguration.getWithRemoteAccess(vfsRootURL);
    }

    private void checkConfigured() throws IllegalStateException {
        if (!configured) {
            logger.error("Attempting to perform operation on not configured node");
            throw new IllegalStateException("Node is not configured for Data Spaces");
        }
    }

    private void checkNotConfigured() throws IllegalStateException {
        if (configured) {
            logger.error("Attempting to configure already configured node");
            throw new IllegalStateException("Node is already configured for Data Spaces");
        }
    }

    private class NodeApplicationConfigurator {

        private SpacesMountManager spacesMountManager;

        private ApplicationScratchSpace applicationScratchSpace;

        private SpacesDirectory cachingDirectory;

        private DataSpacesImpl impl;

        private void configure(final long appId, final String namingServiceURL) throws FileSystemException,
                URISyntaxException, ProActiveException, ConfigurationException {

            // create naming service stub with URL and decorate it with local cache
            // use local variables so GC can collect them if something fails
            final NamingService namingService;
            try {
                namingService = NamingService.createNamingServiceStub(namingServiceURL);
            } catch (ProActiveException x) {
                logger.error("Could not access Naming Service", x);
                throw x;
            } catch (URISyntaxException x) {
                logger.error("Wrong Naming Service URI", x);
                throw x;
            }
            final CachingSpacesDirectory cachingDir = new CachingSpacesDirectory(namingService);

            // create scratch data space for this application and register it
            if (nodeScratchSpace != null) {
                applicationScratchSpace = nodeScratchSpace.initForApplication(appId);
                final SpaceInstanceInfo scratchInfo = applicationScratchSpace.getSpaceInstanceInfo();

                boolean registered = false;
                try {
                    cachingDir.register(scratchInfo);
                    registered = true;
                    logger.debug("Scratch space for application registered");
                } catch (SpaceAlreadyRegisteredException e) {
                    logger.error("Could not register application scratch space to Naming Service", e);
                    throw e;
                } catch (WrongApplicationIdException e) {
                    logger.error("Could not register application scratch space to Naming Service", e);
                    throw e;
                } finally {
                    if (!registered) {
                        nodeScratchSpace.close();
                    }
                }
            }
            // no exception can be thrown since now
            cachingDirectory = cachingDir;

            // create VFSSpacesMountManagerImpl
            spacesMountManager = new VFSSpacesMountManagerImpl(cachingDirectory);

            // create implementation object connected to the application's
            // configuration
            impl = new DataSpacesImpl(appId, spacesMountManager, cachingDirectory, applicationScratchSpace);
        }

        private DataSpacesImpl getDataSpacesImpl() {
            return impl;
        }

        private void close() {
            spacesMountManager.close();
            if (applicationScratchSpace != null) {
                cachingDirectory.unregister(applicationScratchSpace.getSpaceMountingPoint());
                try {
                    applicationScratchSpace.close();
                } catch (FileSystemException x) {
                    ProActiveLogger.logEatedException(logger,
                            "Could not close correctly application scratch space", x);
                }
                applicationScratchSpace = null;
            }
        }
    }
}
