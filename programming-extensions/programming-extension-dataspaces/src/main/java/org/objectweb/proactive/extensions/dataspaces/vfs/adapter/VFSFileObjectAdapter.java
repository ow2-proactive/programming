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
package org.objectweb.proactive.extensions.dataspaces.vfs.adapter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.Selectors;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.Capability;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileContent;
import org.objectweb.proactive.extensions.dataspaces.api.FileSelector;
import org.objectweb.proactive.extensions.dataspaces.api.FileType;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSSpacesMountManagerImpl;
import org.objectweb.proactive.extensions.vfsprovider.util.URIHelper;


/**
 * VFS {@link FileObject} adapter to {@link DataSpacesFileObject} interface, adding getURI
 * functionality.
 * <p>
 * Adapted FileObject should provide any access limitation as required by Data Spaces specification.
 */
public class VFSFileObjectAdapter implements DataSpacesFileObject {
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES);

    /**
     * Apache VFS FileObject handle
     */
    private FileObject currentFileObject;

    /**
     * Virtual Data Space URI
     */
    private final DataSpacesURI dataSpaceURI;

    /**
     * List of urls of the available roots (can be switched to any of them)
     */
    private final LinkedHashSet<String> rootFOUriSet;

    /**
     * url of the root of the current FileObject
     */
    private String currentRootFOUri;

    /**
     * Apache VFS object describing the file
     */
    private FileName dataSpaceVFSFileName;

    private final VFSSpacesMountManagerImpl manager;

    private final String ownerActiveObjectId;

    public VFSFileObjectAdapter(FileObject adaptee, DataSpacesURI dataSpaceURI,
            FileName dataSpaceVFSFileName, ArrayList<String> spaceRoots, String currentRoot)
            throws FileSystemException {
        this(adaptee, dataSpaceURI, dataSpaceVFSFileName, spaceRoots, currentRoot, null, null);
    }

    public VFSFileObjectAdapter(FileObject adaptee, DataSpacesURI dataSpaceURI,
            FileName dataSpaceVFSFileName, ArrayList<String> spaceRoots, String currentRoot,
            VFSSpacesMountManagerImpl manager) throws FileSystemException {
        this(adaptee, dataSpaceURI, dataSpaceVFSFileName, spaceRoots, currentRoot, manager, null);
    }

    /**
     * @param adaptee
     *            file object that is going to be represented as DataSpacesFileObject; cannot be
     *            <code>null</code>
     * @param dataSpaceURI
     *            Data Spaces virtual URI of this file object's space; must have space part fully defined
     *            and only this part; cannot be <code>null</code>
     * @param dataSpaceVFSFileName
     *            VFS file name of the space root FileObject; cannot be <code>null</code>
     * @throws FileSystemException
     *             when data space file name does not match adaptee's name
     */
    public VFSFileObjectAdapter(FileObject adaptee, DataSpacesURI dataSpaceURI,
            FileName dataSpaceVFSFileName, ArrayList<String> spaceRoots, String currentRoot,
            VFSSpacesMountManagerImpl manager, String ownerActiveObjectId) throws FileSystemException {
        this.dataSpaceURI = dataSpaceURI;
        this.dataSpaceVFSFileName = dataSpaceVFSFileName;
        this.currentFileObject = adaptee;
        this.rootFOUriSet = new LinkedHashSet<String>(spaceRoots);
        this.manager = manager;
        this.ownerActiveObjectId = ownerActiveObjectId;
        this.currentRootFOUri = currentRoot;
        checkFileNamesConsistencyOrWound();
    }

    private VFSFileObjectAdapter(FileObject adaptee, VFSFileObjectAdapter fileObjectAdapter)
            throws FileSystemException {
        this(adaptee, fileObjectAdapter.dataSpaceURI, fileObjectAdapter.dataSpaceVFSFileName,
                new ArrayList<String>(fileObjectAdapter.rootFOUriSet), fileObjectAdapter.currentRootFOUri,
                fileObjectAdapter.manager, fileObjectAdapter.ownerActiveObjectId);
    }

    public void close() throws FileSystemException {
        try {
            currentFileObject.close();
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public void copyFrom(DataSpacesFileObject srcFile, FileSelector selector) throws FileSystemException {
        final FileObject srcAdaptee = getVFSAdapteeOrWound(srcFile);
        final org.apache.commons.vfs2.FileSelector vfsSelector = buildFVSSelector(selector);

        try {
            currentFileObject.copyFrom(srcAdaptee, vfsSelector);
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public void createFile() throws FileSystemException {
        try {
            currentFileObject.createFile();
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public void createFolder() throws FileSystemException {
        try {
            currentFileObject.createFolder();
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public boolean delete() throws FileSystemException {
        try {
            return currentFileObject.delete();
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public int delete(FileSelector selector) throws FileSystemException {
        final org.apache.commons.vfs2.FileSelector vfsSelector = buildFVSSelector(selector);

        try {
            return currentFileObject.delete(vfsSelector);
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public boolean exists() throws FileSystemException {
        try {
            return currentFileObject.exists();
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public List<DataSpacesFileObject> findFiles(FileSelector selector) throws FileSystemException {
        final org.apache.commons.vfs2.FileSelector vfsSelector = buildFVSSelector(selector);
        final List<DataSpacesFileObject> result = new ArrayList<DataSpacesFileObject>();

        try {
            final FileObject[] vfsResult = currentFileObject.findFiles(vfsSelector);

            adaptVFSResult(vfsResult, result);
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
        return result;
    }

    public void findFiles(FileSelector selector, boolean depthwise, List<DataSpacesFileObject> selected)
            throws FileSystemException {

        final org.apache.commons.vfs2.FileSelector vfsSelector = buildFVSSelector(selector);

        try {
            final List<FileObject> vfsResult = new ArrayList<FileObject>();
            currentFileObject.findFiles(vfsSelector, depthwise, vfsResult);

            adaptVFSResult(vfsResult, selected);
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public DataSpacesFileObject getChild(String name) throws FileSystemException {
        try {
            return adaptVFSResult(currentFileObject.getChild(name));
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public List<DataSpacesFileObject> getChildren() throws FileSystemException {
        List<DataSpacesFileObject> adapted = new ArrayList<DataSpacesFileObject>();

        try {
            adaptVFSResult(currentFileObject.getChildren(), adapted);
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
        return adapted;
    }

    public FileContent getContent() throws FileSystemException {

        try {
            return new VFSContentAdapter(currentFileObject.getContent(), this);
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public DataSpacesFileObject getParent() throws FileSystemException {
        final FileObject vfsParent;
        try {
            vfsParent = currentFileObject.getParent();
        } catch (Exception e) {
            throw new FileSystemException(e);
        }

        if (vfsParent == null)
            throw new FileSystemException("Operation cannot be performed due to file system limitations");
        return adaptVFSResult(vfsParent);
    }

    public FileType getType() throws FileSystemException {
        try {
            return adaptVFSResult(currentFileObject.getType());
        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    public FileObject getFileObject() {
        return currentFileObject;
    }

    public boolean isContentOpen() {
        return currentFileObject.isContentOpen();
    }

    public boolean isHidden() throws FileSystemException {
        try {
            return currentFileObject.isHidden();
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public boolean isReadable() throws FileSystemException {
        try {
            return currentFileObject.isReadable();
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public boolean isWritable() throws FileSystemException {
        try {
            return currentFileObject.isWriteable();
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public void moveTo(DataSpacesFileObject destFile) throws FileSystemException {
        final FileObject destAdaptee = getVFSAdapteeOrWound(destFile);

        try {
            currentFileObject.moveTo(destAdaptee);
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public void refresh() throws FileSystemException {
        try {
            currentFileObject.refresh();
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public DataSpacesFileObject resolveFile(String path) throws FileSystemException {
        if (path.startsWith("/"))
            throw new FileSystemException("Cannot resolve an absolute path");
        try {
            return adaptVFSResult(currentFileObject.resolveFile(path));
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public FileObject getAdaptee() {
        return currentFileObject;
    }

    public boolean hasSpaceCapability(Capability capability) {
        final org.apache.commons.vfs2.Capability vfsCapability = buildVFSCapability(capability);
        final org.apache.commons.vfs2.FileSystem vfsFileSystem = currentFileObject.getFileSystem();

        return vfsFileSystem.hasCapability(vfsCapability);
    }

    /**
     * @param array
     *            may be null
     * @param adapted
     *            may be null only if <code>array</code> is
     * @throws FileSystemException
     *
     */
    private <T extends Collection<DataSpacesFileObject>> void adaptVFSResult(FileObject[] array, T adapted)
            throws FileSystemException {
        if (array == null)
            return;
        for (int i = 0; i < array.length; i++) {
            adapted.add(new VFSFileObjectAdapter(array[i], this));
        }
    }

    /**
     * @param vfsResult
     *            cannot be null
     * @param adapted
     *            cannot be null
     * @throws FileSystemException
     */
    private <T extends Collection<DataSpacesFileObject>, E extends Collection<FileObject>> void adaptVFSResult(
            E vfsResult, T adapted) throws FileSystemException {

        for (FileObject fo : vfsResult) {
            adapted.add(new VFSFileObjectAdapter(fo, this));
        }
    }

    /**
     * @param vfsFileObject
     *            may be null
     * @throws FileSystemException
     */
    private VFSFileObjectAdapter adaptVFSResult(final FileObject vfsFileObject) throws FileSystemException {
        return vfsFileObject == null ? null : new VFSFileObjectAdapter(vfsFileObject, this);
    }

    private static FileType adaptVFSResult(org.apache.commons.vfs2.FileType vfsResult) {
        if (vfsResult == org.apache.commons.vfs2.FileType.FILE)
            return FileType.FILE;

        if (vfsResult == org.apache.commons.vfs2.FileType.FOLDER)
            return FileType.FOLDER;

        if (vfsResult == org.apache.commons.vfs2.FileType.IMAGINARY)
            return FileType.ABSTRACT;

        return null;
    }

    /**
     * @param selector
     *            may be null
     */
    private static org.apache.commons.vfs2.FileSelector buildFVSSelector(FileSelector selector) {
        switch (selector) {
            case EXCLUDE_SELF:
                return Selectors.EXCLUDE_SELF;
            case SELECT_ALL:
                return Selectors.SELECT_ALL;
            case SELECT_FILES:
                return Selectors.SELECT_FILES;
            case SELECT_FOLDERS:
                return Selectors.SELECT_FOLDERS;
            case SELECT_CHILDREN:
                return Selectors.SELECT_CHILDREN;
            case SELECT_SELF:
                return Selectors.SELECT_SELF;
            case SELECT_SELF_AND_CHILDREN:
                return Selectors.SELECT_SELF_AND_CHILDREN;
            default:
                return null;
        }
    }

    /**
     * @param capability
     *            may be null
     */
    private static org.apache.commons.vfs2.Capability buildVFSCapability(Capability capability) {
        switch (capability) {
            case APPEND_CONTENT:
                return org.apache.commons.vfs2.Capability.APPEND_CONTENT;
            case ATTRIBUTES:
                return org.apache.commons.vfs2.Capability.ATTRIBUTES;
            case COMPRESS:
                return org.apache.commons.vfs2.Capability.COMPRESS;
            case CREATE:
                return org.apache.commons.vfs2.Capability.CREATE;
            case DELETE:
                return org.apache.commons.vfs2.Capability.DELETE;
            case DIRECTORY_READ_CONTENT:
                return org.apache.commons.vfs2.Capability.DIRECTORY_READ_CONTENT;
            case FS_ATTRIBUTES:
                return org.apache.commons.vfs2.Capability.FS_ATTRIBUTES;
            case GET_LAST_MODIFIED:
                return org.apache.commons.vfs2.Capability.GET_LAST_MODIFIED;
            case GET_TYPE:
                return org.apache.commons.vfs2.Capability.GET_TYPE;
            case LAST_MODIFIED:
                return org.apache.commons.vfs2.Capability.LAST_MODIFIED;
            case LIST_CHILDREN:
                return org.apache.commons.vfs2.Capability.LIST_CHILDREN;
            case MANIFEST_ATTRIBUTES:
                return org.apache.commons.vfs2.Capability.MANIFEST_ATTRIBUTES;
            case RANDOM_ACCESS_READ:
                return org.apache.commons.vfs2.Capability.RANDOM_ACCESS_READ;
            case RANDOM_ACCESS_WRITE:
                return org.apache.commons.vfs2.Capability.RANDOM_ACCESS_WRITE;
            case READ_CONTENT:
                return org.apache.commons.vfs2.Capability.READ_CONTENT;
            case RENAME:
                return org.apache.commons.vfs2.Capability.RENAME;
            case SET_LAST_MODIFIED_FILE:
                return org.apache.commons.vfs2.Capability.SET_LAST_MODIFIED_FILE;
            case SET_LAST_MODIFIED_FOLDER:
                return org.apache.commons.vfs2.Capability.SET_LAST_MODIFIED_FOLDER;
            case SIGNING:
                return org.apache.commons.vfs2.Capability.SIGNING;
            case URI:
                return org.apache.commons.vfs2.Capability.URI;
            case VIRTUAL:
                return org.apache.commons.vfs2.Capability.VIRTUAL;
            case WRITE_CONTENT:
                return org.apache.commons.vfs2.Capability.WRITE_CONTENT;
            default:
                return null;
        }
    }

    private void checkFileNamesConsistencyOrWound() throws FileSystemException {
        final FileName adapteeName = currentFileObject.getName();

        if (!dataSpaceVFSFileName.isDescendent(adapteeName, NameScope.DESCENDENT_OR_SELF))
            throw new FileSystemException("Specified data space file name does not match adaptee's name");
    }

    private FileObject getVFSAdapteeOrWound(DataSpacesFileObject file) throws FileSystemException {

        if (file instanceof VFSFileObjectAdapter) {
            return ((VFSFileObjectAdapter) file).getAdaptee();
        }
        throw new FileSystemException("Operation unsupported: destination file system unknown");
    }

    private String computeRelativePath() {
        String relativePath = null;
        try {
            relativePath = dataSpaceVFSFileName.getRelativeName(currentFileObject.getName());
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            ProActiveLogger.logImpossibleException(logger, e);
            throw new ProActiveRuntimeException(e);
        }
        if (".".equals(relativePath)) {
            relativePath = null;
        }
        return relativePath;
    }

    public String getVirtualURI() {
        String relativePath = computeRelativePath();

        return dataSpaceURI.withRelativeToSpace(relativePath).toString();
    }

    public String getRealURI() {
        try {
            URL url = currentFileObject.getURL();
            return URIHelper.convertToEncodedURIString(url.toString());
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            //null of unknown
            return null;
        }
    }

    @Override
    public String getBaseName() {
        return currentFileObject.getName().getBaseName();
    }

    @Override
    public String getPath() {
        return currentFileObject.getName().getPath();
    }

    public List<String> getAllRealURIs() {
        String relativePath = computeRelativePath();
        ArrayList<String> answer = new ArrayList<String>();
        for (String root : rootFOUriSet) {
            if ((relativePath != null) && !(relativePath.equals("."))) {
                answer.add(URIHelper.convertToEncodedURIString(root + relativePath));
            } else {
                answer.add(URIHelper.convertToEncodedURIString(root));
            }
        }
        return answer;
    }

    @Override
    public String getSpaceRootURI() {
        return URIHelper.convertToEncodedURIString(currentRootFOUri);
    }

    @Override
    public DataSpacesFileObject ensureExistingOrSwitch(boolean checkWriteProtected)
            throws FileSystemException, SpaceNotFoundException {
        if (this.exists() && (!checkWriteProtected || checkIfFileWritable(this))) {
            return this;
        }
        logger
                .debug(getRealURI() + " does not exist" +
                    (checkWriteProtected ? " or is write protected" : ""));
        for (String newUri : rootFOUriSet) {
            if (!newUri.equals(currentRootFOUri)) {
                DataSpacesFileObject newdsfo = switchToSpaceRoot(newUri);
                if (newdsfo.exists() && (!checkWriteProtected || checkIfFileWritable(newdsfo))) {
                    return newdsfo;
                }
                logger.debug(newdsfo.getRealURI() + " does not exist" +
                    (checkWriteProtected ? " or is write protected" : ""));
            }
        }
        return null;
    }

    private static boolean checkIfFileWritable(DataSpacesFileObject dsfo) throws FileSystemException {
        if (!dsfo.isWritable()) {
            return false;
        }
        File file = null;
        try {
            file = convertDataSpaceToFileIfPossible(dsfo);
        } catch (URISyntaxException e) {
            throw new FileSystemException(e);
        } catch (DataSpacesException e) {
            throw new FileSystemException(e);
        }
        if (file != null) {
            return file.canWrite();
        }
        return true;
    }

    private static File convertDataSpaceToFileIfPossible(DataSpacesFileObject fo) throws URISyntaxException,
            DataSpacesException {
        URI foUri = new URI(fo.getRealURI());
        if (foUri.getScheme() == null || foUri.getScheme().equals("file")) {
            return new File(foUri);
        } else {
            return null;
        }
    }

    public List<String> getAllSpaceRootURIs() {
        ArrayList<String> answer = new ArrayList<String>(rootFOUriSet.size());
        for (String uri : rootFOUriSet) {
            answer.add(URIHelper.convertToEncodedURIString(uri));
        }
        return answer;
    }

    public DataSpacesFileObject switchToSpaceRoot(String spaceRootUri) throws FileSystemException,
            SpaceNotFoundException {
        if (!rootFOUriSet.contains(spaceRootUri)) {
            throw new IllegalArgumentException(spaceRootUri + " is not accessible by DFO " + getVirtualURI());
        }
        if (manager == null) {
            throw new UnsupportedOperationException("Mount Manager is not initialized.");
        }
        String relativePath = computeRelativePath();
        DataSpacesFileObject newDsfo = manager.resolveFile(dataSpaceURI.withRelativeToSpace(relativePath),
                ownerActiveObjectId, spaceRootUri);
        logger.debug("switched to " + newDsfo.getRealURI());
        return newDsfo;
    }

    @Override
    public boolean equals(Object candidate) {

        if (!(candidate instanceof DataSpacesFileObject))
            return false;

        final DataSpacesFileObject file = (DataSpacesFileObject) candidate;
        return this.getVirtualURI().equals(file.getVirtualURI());
    }
}
