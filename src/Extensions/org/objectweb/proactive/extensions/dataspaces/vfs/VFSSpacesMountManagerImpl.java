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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystem;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.SpacesMountManager;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.objectweb.proactive.extensions.dataspaces.vfs.adapter.VFSFileObjectAdapter;
import org.objectweb.proactive.utils.StackTraceUtil;


/**
 * Implementation of SpacesMountManager using Apache Commons VFS library.
 * <p/>
 * Manager creates and maintains Apache VFS manager to imitate virtual view of file system for each
 * application.
 * <p/>
 * Manager maintains mountings of data spaces in local Map, using lazy on-request strategy. Space is
 * mounted only when there is request to provide DataSpacesFileObject for its content. Proper, local
 * or remote access is determined using {@link Utils#getLocalAccessURL(String, String, String)}
 * method. Write-capabilities of returned DataSpacesFileObjects are induced from used protocols'
 * providers. VFSSpacesMountManagerImpl applies also restriction policies for returned FileObjects,
 * to conform with general Data Spaces guarantees, as implemented in
 * {@link DataSpacesLimitingFileObject}.
 */
public class VFSSpacesMountManagerImpl implements SpacesMountManager {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES_MOUNT_MANAGER);

    private final DefaultFileSystemManager vfsManager;
    private final SpacesDirectory directory;
    private final Map<DataSpacesURI, HashMap<String, FileObject>> mountedSpaces = new HashMap<DataSpacesURI, HashMap<String, FileObject>>();
    private final Map<DataSpacesURI, LinkedHashSet<String>> availableSpacesList = new HashMap<DataSpacesURI, LinkedHashSet<String>>();

    /*
     * These two locks represent two levels of synchronization. In any execution only readLock is
     * acquired or both locks are acquired in constant order (writeLock, then readLock) to avoid
     * deadlocks.
     *
     * readLock secures access to mountedSpaces and vfs instances. It is the only lock used for read
     * queries for already mounted data spaces. It is used to avoid heavy synchronization on queries
     * for already mounted data spaces.
     *
     * writeLock is responsible for potentially costly synchronization of queries requiring mounting
     * data spaces or on unmount. Therefore, only one data space can be mounted or unmounted at a
     * time.
     *
     * Phantom read phenomena can occur for some execution paths acquiring only readLock, for 2
     * times. Now it is not an issue actually, as unmount takes place only within close() method,
     * that is supposed to be called when object is not used anymore.
     */
    private final Object readLock = new Object();
    private final Object writeLock = new Object();

    /**
     * Creates SpaceMountManager instance, that must be finally closed through {@link #close()}
     * method.
     *
     * @param directory data spaces directory to use for serving requests
     * @throws FileSystemException when VFS configuration fails
     */
    public VFSSpacesMountManagerImpl(SpacesDirectory directory) throws FileSystemException {
        logger.debug("Initializing spaces mount manager");
        this.directory = directory;

        try {
            // FIXME: depends on VFS-256 (fixed in VFS fork)
            // in vanilla VFS version, this manager will always return FileObjects with broken
            // delete(FileSelector) method. Anyway, it is rather better to do it this way, than returning
            // shared FileObjects with broken concurrency
            this.vfsManager = VFSFactory.createDefaultFileSystemManager(false);
        } catch (org.apache.commons.vfs.FileSystemException x) {
            logger.error("Could not create and configure VFS manager", x);
            throw new FileSystemException(x);
        }
        logger.debug("Mount manager initialized, VFS instance created");
    }

    public DataSpacesFileObject resolveFile(final DataSpacesURI queryUri, final String ownerActiveObjectId)
            throws FileSystemException, SpaceNotFoundException {
        return resolveFile(queryUri, ownerActiveObjectId, null);

    }

    public DataSpacesFileObject resolveFile(final DataSpacesURI queryUri, final String ownerActiveObjectId,
            String spaceRootFOUri) throws FileSystemException, SpaceNotFoundException {
        if (logger.isDebugEnabled())
            logger.debug("File access request: " + queryUri);

        if (!queryUri.isSuitableForUserPath()) {
            logger.error("Requested URI is not suitable for user path");
            throw new IllegalArgumentException("Requested URI is not suitable for user path");
        }

        final DataSpacesURI spaceURI = queryUri.getSpacePartOnly();
        // it is about a concrete space, nothing abstract
        ensureVirtualSpaceIsMounted(spaceURI, null);

        return doResolveFile(queryUri, ownerActiveObjectId, spaceRootFOUri);
    }

    public Map<DataSpacesURI, DataSpacesFileObject> resolveSpaces(final DataSpacesURI queryUri,
            final String ownerActiveObjectId) throws FileSystemException {

        final Map<DataSpacesURI, DataSpacesFileObject> result = new HashMap<DataSpacesURI, DataSpacesFileObject>();
        if (logger.isDebugEnabled())
            logger.debug("Spaces access request: " + queryUri);

        final Set<SpaceInstanceInfo> spaces = directory.lookupMany(queryUri);
        if (spaces != null) {
            for (final SpaceInstanceInfo space : spaces) {
                final DataSpacesURI spaceUri = space.getMountingPoint();
                if (!spaceUri.isSuitableForUserPath()) {
                    logger.error("Resolved space is not suitable for user path: " + spaceUri);
                    throw new IllegalArgumentException("Resolved space is not suitable for user path: " +
                        spaceUri);
                }
                try {
                    ensureVirtualSpaceIsMounted(spaceUri, space);
                } catch (SpaceNotFoundException e) {
                    ProActiveLogger.logImpossibleException(logger, e);
                    throw new RuntimeException(e);
                }
                result.put(spaceUri, doResolveFile(spaceUri, ownerActiveObjectId, null));
            }
        }
        return result;
    }

    public void close() {
        logger.debug("Closing mount manager");
        synchronized (writeLock) {
            synchronized (readLock) {
                for (final DataSpacesURI spaceUri : new ArrayList<DataSpacesURI>(mountedSpaces.keySet())) {
                    unmountAllSpaces(spaceUri);
                }
                vfsManager.close();
            }
        }
        logger.debug("Mount manager closed");
    }

    private void ensureVirtualSpaceIsMounted(final DataSpacesURI spaceURI, SpaceInstanceInfo info)
            throws SpaceNotFoundException, FileSystemException {

        final boolean mounted;
        DataSpacesURI spacePart = spaceURI.getSpacePartOnly();

        synchronized (readLock) {
            mounted = mountedSpaces.containsKey(spacePart);
        }

        if (!mounted) {
            if (info == null) {
                info = directory.lookupOne(spaceURI);
            }
            if (info == null) {
                logger.warn("Could not find data space in spaces directory: " + spacePart);
                throw new SpaceNotFoundException(
                    "Requested data space is not registered in spaces directory.");
            }

            synchronized (writeLock) {
                // kind of double-checked lock ->
                // check once more within writeLock
                synchronized (readLock) {
                    if (mountedSpaces.containsKey(spacePart) && (mountedSpaces.get(spacePart).size() > 0))
                        return;
                }
                mountAllAvailableSpace(info);
            }
        }
    }

    /*
     * Assumed to be called within writeLock
     *
     * TODO: support concurrent mounting of more than one data space at a time if needed (requires a
     * little bit more complex synchronization)
     */
    private void mountAllAvailableSpace(final SpaceInstanceInfo spaceInfo) throws FileSystemException {

        final DataSpacesURI mountingPoint = spaceInfo.getMountingPoint();

        if (!availableSpacesList.containsKey(spaceInfo)) {
            LinkedHashSet<String> srl = new LinkedHashSet<String>();
            srl.addAll(spaceInfo.getUrls());
            availableSpacesList.put(mountingPoint, srl);
        }

        if (!mountedSpaces.containsKey(mountingPoint)) {
            mountedSpaces.put(mountingPoint, new HashMap<String, FileObject>());
        }

        String nl = System.getProperty("line.separator");
        String errorMessage = "An error occurred while trying to mount " + spaceInfo.getName() +
            " here are all the errors received : " + nl;
        int nbMountedSpace = 0;
        if (spaceInfo.getUrls().size() == 0) {
            throw new IllegalStateException("Empty Space configuration");
        }
        for (String accessUrl : spaceInfo.getUrls()) {
            // for backward compatibility, in the case where there is only one url, we preserve the behaviour where the local (file system)
            // path will be computed and used instead of the provided url for access on the same host
            // in the case where there are at least two urls, a file:// url should be provided
            if (spaceInfo.getUrls().size() == 1) {
                accessUrl = Utils.getLocalAccessURL(accessUrl, spaceInfo.getPath(), spaceInfo.getHostname());
            }
            try {
                if (ensureSpaceIsMounted(mountingPoint, accessUrl)) {
                    nbMountedSpace++;
                }
            } catch (FileSystemException e) {
                errorMessage += StackTraceUtil.getStackTrace(e) + nl;
            }
        }
        if (nbMountedSpace == 0) {
            throw new FileSystemException(errorMessage);
        }
    }

    /*
     * Assumed to be called within writeLock
     *
     * TODO: support concurrent mounting of more than one data space at a time if needed (requires a
     * little bit more complex synchronization)
     */
    private boolean ensureSpaceIsMounted(final DataSpacesURI mountingPoint, final String spaceRootFOUri)
            throws FileSystemException {
        HashMap<String, FileObject> fos = null;
        DataSpacesURI spacePart = mountingPoint.getSpacePartOnly();
        synchronized (readLock) {
            fos = mountedSpaces.get(spacePart);
            // already mounted
            if (fos.get(spaceRootFOUri) != null) {
                return true;
            }
        }
        FileObject mountedRoot = null;
        LinkedHashSet<String> allRootUris = availableSpacesList.get(spacePart);
        try {
            mountedRoot = vfsManager.resolveFile(spaceRootFOUri);

            if (mountedRoot != null && !mountedRoot.exists()) {
                String err = String.format("Could not access URL %s to mount %s", spaceRootFOUri, spacePart);
                logger.info(err);
                allRootUris.remove(spaceRootFOUri);
                availableSpacesList.put(spacePart, allRootUris);
                throw new FileSystemException(err);
            }
        } catch (org.apache.commons.vfs.FileSystemException x) {
            String err = String.format("Could not access URL %s to mount %s", spaceRootFOUri, spacePart);
            logger.info(err);

            allRootUris.remove(spaceRootFOUri);
            availableSpacesList.put(spacePart, allRootUris);
            throw new FileSystemException(err, x);

        }
        if (mountedRoot != null) {
            synchronized (readLock) {
                fos.put(spaceRootFOUri, mountedRoot);
                mountedSpaces.put(spacePart, fos);
            }
            if (logger.isDebugEnabled())
                logger.debug(String.format("Mounted space: %s (access URL: %s)", spacePart, spaceRootFOUri));
            return true;
        }
        allRootUris.remove(spaceRootFOUri);
        availableSpacesList.put(spacePart, allRootUris);
        return false;
    }

    /*
     * Assumed to be called within writeLock and readLock, mountedSpaces contains specified spaceUri
     */
    private void unmountAllSpaces(final DataSpacesURI spaceUri) {

        DataSpacesURI spacePart = spaceUri.getSpacePartOnly();

        final HashMap<String, FileObject> spaceRoots = mountedSpaces.remove(spacePart);
        for (FileObject spaceRoot : spaceRoots.values()) {
            final FileSystem spaceFileSystem = spaceRoot.getFileSystem();

            // we may not need to close FileObject, but with VFS you never know...
            try {
                spaceRoot.close();
            } catch (org.apache.commons.vfs.FileSystemException x) {
                ProActiveLogger.logEatedException(logger, String.format(
                        "Could not close data space %s root file object", spacePart), x);
            }
            vfsManager.closeFileSystem(spaceFileSystem);
            if (logger.isDebugEnabled())
                logger.debug("Unmounted space: " + spacePart);
        }
    }

    private DataSpacesFileObject doResolveFile(final DataSpacesURI uri, final String ownerActiveObjectId,
            String spaceRootFOUri) throws FileSystemException {

        DataSpacesURI spacePart = uri.getSpacePartOnly();

        if (spaceRootFOUri != null) {
            synchronized (writeLock) {
                ensureSpaceIsMounted(spacePart, spaceRootFOUri);
            }
        } else {
            synchronized (readLock) {
                LinkedHashSet<String> los = availableSpacesList.get(spacePart);
                spaceRootFOUri = los.iterator().next();
                ensureSpaceIsMounted(spacePart, spaceRootFOUri);
            }
        }
        final String relativeToSpace = uri.getRelativeToSpace();
        synchronized (readLock) {

            if (!mountedSpaces.containsKey(spacePart)) {
                throw new FileSystemException("Could not access file that should exist (be mounted)");
            }

            final HashMap<String, FileObject> spaceRoots = mountedSpaces.get(spacePart);
            FileObject spaceRoot = spaceRoots.get(spaceRootFOUri);
            org.apache.commons.vfs.FileSystemException lastException = null;
            FileName dataSpaceVFSFileName = null;

            final FileObject file;
            // the dataspace "File name" (it is actually a File Path) is computed using the Virtual Space root
            if (dataSpaceVFSFileName == null) {
                dataSpaceVFSFileName = spaceRoot.getName();
            }
            try {
                if (relativeToSpace == null)
                    file = spaceRoot;
                else
                    file = spaceRoot.resolveFile(relativeToSpace);
                final DataSpacesLimitingFileObject limitingFile = new DataSpacesLimitingFileObject(file,
                    spacePart, spaceRoot.getName(), ownerActiveObjectId);
                return new VFSFileObjectAdapter(limitingFile, spacePart, dataSpaceVFSFileName,
                    new ArrayList<String>(availableSpacesList.get(spacePart)), this, ownerActiveObjectId);
            } catch (org.apache.commons.vfs.FileSystemException x) {
                logger.error("Could not access file within a space: " + uri);

                throw new FileSystemException(x);
            } catch (FileSystemException e) {
                ProActiveLogger.logImpossibleException(logger, e);
                throw new ProActiveRuntimeException(e);
            }

        }
    }
}
