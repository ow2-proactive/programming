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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
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

    /*
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
    private final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = rwlock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = rwlock.writeLock();

    /**
     * The directory of dataspaces
     */
    private final SpacesDirectory directory;

    /**
     * stores all already mounted vfs for each virtual uri
     */
    private final Map<DataSpacesURI, ConcurrentHashMap<String, FileObject>> mountedSpaces = new HashMap<DataSpacesURI, ConcurrentHashMap<String, FileObject>>();

    /**
     * For each virtual dataspace, it stores the list of accessible Apache FileObject Urls
     */
    private final Map<DataSpacesURI, LinkedHashSet<String>> accessibleFileObjectUris = new HashMap<DataSpacesURI, LinkedHashSet<String>>();

    /**
     * Creates SpaceMountManager instance, that must be finally closed through {@link #close()}
     * method.
     *
     * @param directory data spaces directory to use for serving requests
     * @throws FileSystemException when VFS configuration fails
     */
    public VFSSpacesMountManagerImpl(SpacesDirectory directory) throws FileSystemException {
        this.directory = directory;
    }

    /**
     * resolves the given virtual uri into a DataSpaceFileObject. Mount the space if necessary
     * @param queryUri
     *            Data Spaces URI to get access to
     * @param ownerActiveObjectId
     *            Id of active object requesting this file, that will become owner of returned
     *            {@link DataSpacesFileObject} instance. May be <code>null</code>, which corresponds
     *            to anonymous (unimportant) owner.
     * @return
     * @throws FileSystemException
     * @throws SpaceNotFoundException
     */
    public DataSpacesFileObject resolveFile(final DataSpacesURI queryUri, final String ownerActiveObjectId)
            throws FileSystemException, SpaceNotFoundException {
        return resolveFile(queryUri, ownerActiveObjectId, null);

    }

    /**
     * resolves the given virtual uri into a DataSpaceFileObject. The VFS root is forced to the spaceRootFOUri parameter. Mount the space if necessary
     * @param queryUri
     *          Data Spaces URI to get access to
     * @param ownerActiveObjectId
     *          Id of active object requesting this file, that will become owner of returned
     *          {@link DataSpacesFileObject} instance. May be <code>null</code>, which corresponds
     *          to anonymous (unimportant) owner.
     * @param spaceRootFOUri
     *          forces the use of the provided VFS space root (in case there are several roots)
     * @return
     * @throws FileSystemException
     * @throws SpaceNotFoundException
     */
    public DataSpacesFileObject resolveFile(final DataSpacesURI queryUri, final String ownerActiveObjectId,
            String spaceRootFOUri) throws FileSystemException, SpaceNotFoundException {
        if (logger.isDebugEnabled())
            logger.debug("[VFSMountManager] File access request: " + queryUri);

        if (!queryUri.isSuitableForUserPath()) {
            logger.error("[VFSMountManager] Requested URI " + queryUri + " is not suitable for user path");
            throw new IllegalArgumentException("Requested URI " + queryUri + " is not suitable for user path");
        }

        final DataSpacesURI spaceURI = queryUri.getSpacePartOnly();

        ensureVirtualSpaceIsMounted(spaceURI, null);

        return doResolveFile(queryUri, ownerActiveObjectId, spaceRootFOUri);
    }

    /**
     * Resolves all spaces represented by the given query uri
     * @param queryUri
     *            Data Spaces URI to query for; must be URI without space part being fully defined,
     *            i.e. not pointing to any concrete data space; result spaces for that queries must
     *            be suitable for user path.
     * @param ownerActiveObjectId
     *            Id of active object requesting this files, that will become owner of returned
     *            {@link DataSpacesFileObject} instances. May be <code>null</code>, which
     *            corresponds to anonymous (unimportant) owner.
     * @return
     * @throws FileSystemException
     */
    public Map<DataSpacesURI, DataSpacesFileObject> resolveSpaces(final DataSpacesURI queryUri,
            final String ownerActiveObjectId) throws FileSystemException {

        final Map<DataSpacesURI, DataSpacesFileObject> result = new HashMap<DataSpacesURI, DataSpacesFileObject>();
        if (logger.isDebugEnabled())
            logger.debug("[VFSMountManager] Spaces access request: " + queryUri);

        final Set<SpaceInstanceInfo> spaces = directory.lookupMany(queryUri);
        if (spaces != null) {
            for (final SpaceInstanceInfo space : spaces) {
                final DataSpacesURI spaceUri = space.getMountingPoint();
                if (!spaceUri.isSuitableForUserPath()) {
                    logger.error("[VFSMountManager] Resolved space is not suitable for user path: " +
                        spaceUri);
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

    /**
     * Closes all virtual spaces mounted by this mount manager
     */
    public void close() {
        logger.debug("[VFSMountManager] Closing mount manager");
        try {
            writeLock.lock();
            for (final DataSpacesURI spaceUri : new ArrayList<DataSpacesURI>(mountedSpaces.keySet())) {
                unmountAllFileSystems(spaceUri);
            }

        } finally {
            writeLock.unlock();
        }
        logger.debug("[VFSMountManager] Mount manager closed");
    }

    /**
     * Ensures that the provided virtual space is mounted (at least one VFS file system is mounted)
     * @param spaceURI uri of the virtual space
     * @param info dataspace info
     * @throws SpaceNotFoundException
     * @throws FileSystemException
     */
    private void ensureVirtualSpaceIsMounted(final DataSpacesURI spaceURI, SpaceInstanceInfo info)
            throws SpaceNotFoundException, FileSystemException {

        final boolean mounted;
        DataSpacesURI spacePart = spaceURI.getSpacePartOnly();

        try {
            readLock.lock();
            mounted = mountedSpaces.containsKey(spacePart);
        } finally {
            readLock.unlock();
        }

        if (!mounted) {
            if (info == null) {
                info = directory.lookupOne(spaceURI);
            }
            if (info == null) {
                logger.warn("[VFSMountManager] Could not find data space in spaces directory: " + spacePart);
                throw new SpaceNotFoundException(
                    "Requested data space is not registered in spaces directory.");
            }

            try {
                readLock.lock();
                if (mountedSpaces.containsKey(spacePart) && (mountedSpaces.get(spacePart).size() > 0))
                    return;
            } finally {
                readLock.unlock();
            }
            mountFirstAvailableFileSystem(info);

        }
    }

    /**
     * Mounts the first available VFS file system on the given dataspace
     * @param spaceInfo space information
     * @throws FileSystemException if no file system could be mounted
     */
    private void mountFirstAvailableFileSystem(final SpaceInstanceInfo spaceInfo) throws FileSystemException {

        final DataSpacesURI mountingPoint = spaceInfo.getMountingPoint();

        try {
            writeLock.lock();
            if (!mountedSpaces.containsKey(mountingPoint)) {
                mountedSpaces.put(mountingPoint, new ConcurrentHashMap<String, FileObject>());
            }
            ConcurrentHashMap<String, FileObject> fileSystems = mountedSpaces.get(mountingPoint);

            if (spaceInfo.getUrls().size() == 0) {
                throw new IllegalStateException("Empty Space configuration");
            }

            DataSpacesURI spacePart = mountingPoint.getSpacePartOnly();
            ArrayList<String> urls = new ArrayList<String>(spaceInfo.getUrls());
            if (urls.size() == 1) {
                urls.add(0,
                        Utils.getLocalAccessURL(urls.get(0), spaceInfo.getPath(), spaceInfo.getHostname()));
            }

            logger.debug("[VFSMountManager] Request mounting VFS root list : " + urls);

            try {
                VFSMountManagerHelper.mountAny(urls, fileSystems);

                if (!accessibleFileObjectUris.containsKey(mountingPoint)) {
                    LinkedHashSet<String> srl = new LinkedHashSet<String>();
                    accessibleFileObjectUris.put(mountingPoint, srl);
                }

                LinkedHashSet<String> srl = accessibleFileObjectUris.get(mountingPoint);

                for (String uri : urls) {
                    if (fileSystems.containsKey(uri)) {
                        srl.add(uri);
                    }
                }
                if (srl.isEmpty()) {
                    throw new IllegalStateException("Invalid empty size list when trying to mount " + urls +
                        " mounted map content is " + fileSystems);
                }
                accessibleFileObjectUris.put(mountingPoint, srl);

                if (logger.isDebugEnabled())
                    logger.debug(String.format("[VFSMountManager] Mounted space: %s (access URL: %s)",
                            spacePart, srl));

                mountedSpaces.put(mountingPoint, fileSystems);

            } catch (org.apache.commons.vfs2.FileSystemException e) {
                mountedSpaces.remove(mountingPoint);
                throw new FileSystemException("An error occurred while trying to mount " +
                    spaceInfo.getName(), e);
            }
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Makes sure that the provided file system is mounted for the given dataspace (identified by its root url)
     * @param mountingPoint dataspace uri
     * @param spaceRootFOUri file system root
     * @return true if the file system is mounted
     * @throws FileSystemException
     */
    private boolean ensureFileSystemIsMounted(final DataSpacesURI mountingPoint, final String spaceRootFOUri)
            throws FileSystemException {
        ConcurrentHashMap<String, FileObject> fileSystems = null;
        DataSpacesURI spacePart = mountingPoint.getSpacePartOnly();
        try {
            readLock.lock();
            fileSystems = mountedSpaces.get(spacePart);
            // already mounted
            if (fileSystems.get(spaceRootFOUri) != null) {
                return true;
            }
        } finally {
            readLock.unlock();
        }
        logger.debug("[VFSMountManager] Request mounting VFS root = " + spaceRootFOUri);
        FileObject mountedRoot;
        try {
            mountedRoot = VFSMountManagerHelper.mount(spaceRootFOUri);

            // the fs is accessible
            try {
                writeLock.lock();
                fileSystems.put(spaceRootFOUri, mountedRoot);
                mountedSpaces.put(spacePart, fileSystems);
            } finally {
                writeLock.unlock();
            }
            if (logger.isDebugEnabled())
                logger.debug(String.format("[VFSMountManager] Mounted space: %s (access URL: %s)", spacePart,
                        spaceRootFOUri));
            return true;

        } catch (org.apache.commons.vfs2.FileSystemException x) {
            String err = String.format("[VFSMountManager] Could not access URL %s to mount %s",
                    spaceRootFOUri, spacePart);
            logger.info(err);
            removeSpaceRootUri(spacePart, spaceRootFOUri);
            throw new FileSystemException(err, x);

        }
    }

    /**
     * Removes the
     * @param spacePart
     * @param spaceRootFOUri
     */
    private void removeSpaceRootUri(DataSpacesURI spacePart, String spaceRootFOUri) {
        LinkedHashSet<String> allRootUris = accessibleFileObjectUris.get(spacePart);
        allRootUris.remove(spaceRootFOUri);
        if (allRootUris.isEmpty()) {
            accessibleFileObjectUris.remove(spacePart);
        } else {
            accessibleFileObjectUris.put(spacePart, allRootUris);
        }
    }

    /**
     * Unmount all file systems for the given dataspace
     * @param spaceUri dataspace uri
     */
    private void unmountAllFileSystems(final DataSpacesURI spaceUri) {

        DataSpacesURI spacePart = spaceUri.getSpacePartOnly();

        final ConcurrentHashMap<String, FileObject> spaceRoots = mountedSpaces.remove(spacePart);

        VFSMountManagerHelper.closeFileSystems(spaceRoots.keySet());
    }

    /**
     * Internal method for resolving a file, will mount the file system if it is not mounted yet
     * @param uri virtual uri of the file
     * @param ownerActiveObjectId Id of active object requesting this file
     * @param spaceRootFOUri root file system to use
     * @return
     * @throws FileSystemException
     */
    private DataSpacesFileObject doResolveFile(final DataSpacesURI uri, final String ownerActiveObjectId,
            String spaceRootFOUri) throws FileSystemException {

        DataSpacesURI spacePart = uri.getSpacePartOnly();

        if (spaceRootFOUri != null) {
            ensureFileSystemIsMounted(spacePart, spaceRootFOUri);
        } else {
            try {
                readLock.lock();
                LinkedHashSet<String> los = accessibleFileObjectUris.get(spacePart);
                spaceRootFOUri = los.iterator().next();
            } finally {
                readLock.unlock();
            }
            ensureFileSystemIsMounted(spacePart, spaceRootFOUri);
        }

        final String relativeToSpace = uri.getRelativeToSpace();
        try {
            readLock.lock();

            if (!mountedSpaces.containsKey(spacePart)) {
                throw new FileSystemException("Could not access file that should exist (be mounted)");
            }

            final ConcurrentHashMap<String, FileObject> spaceRoots = mountedSpaces.get(spacePart);
            FileObject spaceRoot = spaceRoots.get(spaceRootFOUri);
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
                    new ArrayList<String>(accessibleFileObjectUris.get(spacePart)), spaceRootFOUri, this,
                    ownerActiveObjectId);
            } catch (org.apache.commons.vfs2.FileSystemException x) {
                logger.error("[VFSMountManager] Could not access file within a space: " + uri);

                throw new FileSystemException(x);
            } catch (FileSystemException e) {
                ProActiveLogger.logImpossibleException(logger, e);
                throw new ProActiveRuntimeException(e);
            }

        } finally {
            readLock.unlock();
        }
    }
}
