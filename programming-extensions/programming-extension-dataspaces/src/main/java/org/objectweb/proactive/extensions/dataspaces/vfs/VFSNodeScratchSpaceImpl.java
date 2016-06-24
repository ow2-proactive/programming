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
package org.objectweb.proactive.extensions.dataspaces.vfs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.core.ApplicationScratchSpace;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.NodeScratchSpace;
import org.objectweb.proactive.extensions.dataspaces.core.ScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


/**
 * Implementation of {@link NodeScratchSpace} using Apache Commons VFS library.
 */
public class VFSNodeScratchSpaceImpl implements NodeScratchSpace {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES_CONFIGURATOR);

    private BaseScratchSpaceConfiguration baseScratchConfiguration;

    private Node node;

    private boolean configured;

    private FileObject partialSpaceFile;

    private DefaultFileSystemManager fileSystemManager;

    private boolean closed = false;

    /**
     * Inner class to implement {@link ApplicationScratchSpace} interface.
     */
    private class AppScratchSpaceImpl implements ApplicationScratchSpace {
        private final FileObject spaceFile;

        private final Map<String, DataSpacesURI> scratches = new HashMap<String, DataSpacesURI>();

        private final SpaceInstanceInfo spaceInstanceInfo;

        private AppScratchSpaceImpl(final String appId) throws FileSystemException {
            logger.debug("Initializing application node scratch space");
            final String runtimeId = Utils.getRuntimeId(node);
            final String nodeId = Utils.getNodeId(node);

            try {
                this.spaceFile = createEmptyDirectoryRelative(partialSpaceFile, appId);
                spaceFile.close();
            } catch (org.apache.commons.vfs2.FileSystemException x) {
                logger.error("Could not create directory for application scratch space", x);
                throw new FileSystemException(x);
            }
            try {
                final ScratchSpaceConfiguration scratchSpaceConf = baseScratchConfiguration
                        .createScratchSpaceConfiguration(runtimeId, nodeId, appId);
                this.spaceInstanceInfo = new SpaceInstanceInfo(appId, runtimeId, nodeId, scratchSpaceConf);
            } catch (ConfigurationException x) {
                ProActiveLogger.logImpossibleException(logger, x);
                close();
                throw new ProActiveRuntimeException(x);
            }
            logger.debug("Initialized application node scratch space");
        }

        public void close() throws FileSystemException {
            logger.debug("Closing application scratch space");

            try {
                final int filesNumber = spaceFile.delete(Selectors.SELECT_ALL);
                logger.debug("Deleted " + filesNumber + " files in scratch application directory");
            } catch (org.apache.commons.vfs2.FileSystemException e) {
                logger.warn("Could not delete " + spaceFile, e);
            } finally {
                try {
                    // the close operation is just a hint to the implementation
                    // that it can release any resources associated with the file.
                    spaceFile.close();
                } catch (org.apache.commons.vfs2.FileSystemException e) {
                    throw new FileSystemException(e);
                }
            }

            logger.debug("Closed application scratch space");
        }

        public synchronized DataSpacesURI getScratchForAO(Body body) throws FileSystemException {
            // TODO performance can be improved using more fine-grained synchronization
            final String aoid = Utils.getActiveObjectId(body);
            if (logger.isDebugEnabled())
                logger.debug("Request for scratch for Active Object with id: " + aoid);

            final DataSpacesURI uri;
            if (!scratches.containsKey(aoid)) {
                try {
                    // TODO we could use VFSSpacesMountManagerImpl for that if it returned FileObject,
                    // so we can avoid unnecessarily double mounting resulting in opening,
                    // closing and opening again the same file
                    createEmptyDirectoryRelative(spaceFile, aoid).close();
                    // the close operation is just a hint to the implementation
                    // that it can release any resources associated with the file.
                    spaceFile.close();
                } catch (org.apache.commons.vfs2.FileSystemException x) {
                    logger.error(String.format(
                            "Could not create directory for Active Object (id: %s) scratch", aoid), x);
                    throw new FileSystemException(x);
                }
                uri = spaceInstanceInfo.getMountingPoint().withActiveObjectId(aoid);
                if (logger.isDebugEnabled())
                    logger.debug(String.format("Created scratch for Active Object with id: %s, URI: %s",
                            aoid, uri));
                scratches.put(aoid, uri);
            } else
                uri = scratches.get(aoid);

            return uri;
        }

        public SpaceInstanceInfo getSpaceInstanceInfo() {
            return spaceInstanceInfo;
        }

        public DataSpacesURI getSpaceMountingPoint() {
            return spaceInstanceInfo.getMountingPoint();
        }
    }

    public synchronized void init(Node node, BaseScratchSpaceConfiguration conf) throws FileSystemException,
            ConfigurationException, IllegalStateException {
        logger.debug("Initializing node scratch space");
        if (configured) {
            logger.error("Attempting to configure already configured node scratch space");
            throw new IllegalStateException("Instance already configured");
        }

        if (conf.getUrls() == null) {
            throw new ConfigurationException("No remote access URL defined in base scratch configuration");
        }

        this.node = node;
        this.baseScratchConfiguration = conf;

        try {
            fileSystemManager = VFSFactory.createDefaultFileSystemManager();
        } catch (org.apache.commons.vfs2.FileSystemException x) {
            logger.error("Could not create and configure VFS manager", x);
            throw new FileSystemException(x);
        }

        try {
            final String nodeId = Utils.getNodeId(node);
            final String runtimeId = Utils.getRuntimeId(node);
            final String[] originalUrls = baseScratchConfiguration.getUrls();
            // find the file url in the list (it should be the first one)
            URI fileUri = null;
            for (String url : originalUrls) {
                try {
                    URI uri = new URI(url);
                    if (uri.getScheme().equals("file")) {
                        fileUri = uri;
                        break;
                    }
                } catch (URISyntaxException e) {
                    logger.error("Could not initialize scratch space");
                    throw new FileSystemException(e);
                }
            }

            String localAccessUrl;
            if (fileUri != null) {
                // if a file url was among the list use it
                localAccessUrl = Utils.getLocalAccessURL((new File(fileUri)).getAbsolutePath(),
                        baseScratchConfiguration.getPath(), Utils.getHostname());
            } else {
                // otherwise compute a new url using the configuration root path
                localAccessUrl = Utils.getLocalAccessURL(originalUrls[0], baseScratchConfiguration.getPath(),
                        Utils.getHostname());
            }

            final String partialSpacePath = Utils.appendSubDirs(localAccessUrl, runtimeId, nodeId);

            logger.debug("Accessing scratch space location: " + partialSpacePath);
            try {
                partialSpaceFile = fileSystemManager.resolveFile(partialSpacePath);
                checkCapabilities(partialSpaceFile.getFileSystem());
                partialSpaceFile.delete(Selectors.EXCLUDE_SELF);
                partialSpaceFile.createFolder();
                if (!partialSpaceFile.isWriteable()) {
                    throw new org.apache.commons.vfs2.FileSystemException(
                        "Created directory is unexpectedly not writable");
                }
                // the close operation is just a hint to the implementation
                // that it can release any resources associated with the file.
                partialSpaceFile.close();
            } catch (org.apache.commons.vfs2.FileSystemException x) {
                logger.error("Could not initialize scratch space at: " + partialSpacePath);
                throw new FileSystemException(x);
            }
            configured = true;
            logger.debug("Initialized node scratch space at: " + partialSpacePath);
        } finally {
            if (!configured)
                fileSystemManager.close();
        }
    }

    public synchronized ApplicationScratchSpace initForApplication(final String appId)
            throws FileSystemException, IllegalStateException {

        checkIfConfigured();
        return new AppScratchSpaceImpl(appId);
    }

    public synchronized void close() throws IllegalStateException {
        if (closed) {
            return;
        }

        logger.debug("Closing node scratch space");
        checkIfConfigured();

        try {
            final FileObject fRuntime = partialSpaceFile.getParent();

            // rm -r node
            partialSpaceFile.delete(Selectors.SELECT_ALL);

            // try to remove runtime file
            // IMPORTANT FIXME: it seems that despite of VFS FileObject documentation,
            // looking at AbstractFileObject docs suggests that it does not implement this
            // delete-if-empty behavior! at least, it appears to be not atomic (and probably may be never atomic
            // as some protocols may not support this kind of atomic operation?)
            // refreshing file before deleting may minimize the risk of delete-when-non-empty behavior
            fRuntime.refresh();
            try {
                final boolean deleted = fRuntime.delete();
                if (deleted)
                    logger.debug("Scratch directory for whole runtime was deleted (considered as empty)");
                else
                    logger.debug("Scratch directory for whole runtime was not deleted (not considered as empty)");
            } catch (org.apache.commons.vfs2.FileSystemException x) {
                logger.debug(
                        "Could not delete scratch directory for whole runtime - perhaps it was not empty", x);
            }

            // it is probably not needed to close files if manager is closed, but with VFS you never know...
            fRuntime.close();
            partialSpaceFile.close();
        } catch (org.apache.commons.vfs2.FileSystemException x) {
            ProActiveLogger.logEatedException(logger, "Could not close correctly node scratch space", x);
        } finally {
            this.fileSystemManager.close();
        }
        logger.debug("Closed node scratch space");

        closed = true;
    }

    private FileObject createEmptyDirectoryRelative(final FileObject parent, final String path)
            throws org.apache.commons.vfs2.FileSystemException {

        FileObject f = parent.resolveFile(path);

        try {
            f.delete(Selectors.EXCLUDE_SELF);
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            logger.warn("Could not delete directory " + f, e);
        }

        try {
            f.createFolder();
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            logger.warn("Could not create folder " + f, e);
        }

        return f;
    }

    private void checkCapabilities(FileSystem fs) throws ConfigurationException {
        // let's have at least those capabilities that scratch space does
        // final Capability[] expected = PADataSpaces.getCapabilitiesForSpaceType(SpaceType.SCRATCH);

        // but you never know what is there.. therefore:
        final Capability[] expected = new Capability[] { Capability.CREATE, Capability.DELETE,
                Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT,
                Capability.WRITE_CONTENT };

        for (int i = 0; i < expected.length; i++) {
            final Capability capability = expected[i];

            if (fs.hasCapability(capability))
                continue;

            logger.error("Scratch file system does not support capability: " + capability);
            throw new ConfigurationException("Scratch file system does not support capability: " + capability);
        }
    }

    private void checkIfConfigured() throws IllegalStateException {
        if (!configured) {
            logger.error("Attempting to perform operation on not configured node scratch space");
            throw new IllegalStateException("Instance not configured");
        }
    }
}
