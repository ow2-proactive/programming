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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.NameScope;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.exceptions.MalformedURIException;


/**
 * Implementation of a VFS FileObject with some restrictions applied. This class represents file
 * within data space (with URI suitable for user path) that is accessed through VFS library. It is
 * intended to be used with VFS-DataSpaces adapter.
 * <p>
 * To conform to general Data Spaces guarantees, basing on file URI and Active Object id write
 * access is limited for:
 * <ul>
 * <li>input data space - for every AO</li>
 * <li>AO's scratch space - for AO different that scratch's owner</li>
 * </ul>
 * <p>
 * General file access is limited to files within spaces, with URIs suitable for user path.
 * <p>
 * Instances of this class conform to the same rules regarding concurrent access, resources
 * management etc. as pure {@link FileObject} does.
 */
public class DataSpacesLimitingFileObject extends AbstractLimitingFileObject<DataSpacesLimitingFileObject> {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES);

    private final DataSpacesURI spaceRootUri;
    private final FileName spaceRootFileName;
    private final String ownerActiveObjectId;
    private final boolean readOnly;

    /**
     * Creates an instance of DataSpacesLimitingFileObject. Before any usage of this class, id of an
     * active object has to be set accordingly.
     *
     * @param fileObject
     *            file object that is going to be represented as DataSpacesFileObject; cannot be
     *            <code>null</code>
     * @param spaceRootUri
     *            Data Spaces URI of this file object's space; must have space part fully defined
     *            and only this part; cannot be <code>null</code>
     * @param spaceRootFileName
     *            VFS path of the space root FileObject; cannot be <code>null</code>
     * @param ownerActiveObjectId
     *            id of active object owning this FileObject instance; may be <code>null</code>,
     *            which corresponds to anonymous (unimportant) owner.
     */
    public DataSpacesLimitingFileObject(FileObject fileObject, DataSpacesURI spaceRootUri,
            FileName spaceRootFileName, String ownerActiveObjectId) {
        super(fileObject);
        this.spaceRootUri = spaceRootUri;
        this.spaceRootFileName = spaceRootFileName;
        this.ownerActiveObjectId = ownerActiveObjectId;
        this.readOnly = computeIsReadOnly();
    }

    /**
     * Returned URI is always suitable for user path.
     *
     * @see DataSpacesURI#isSuitableForUserPath()
     */
    public String getURI() {
        try {
            return getDataSpacesURI().toString();
        } catch (MalformedURIException x) {
            ProActiveLogger.logImpossibleException(logger, x);
            throw new ProActiveRuntimeException(x);
        }
    }

    /**
     * @return URI of a file, always suitable for user path.
     * @throws MalformedURIException
     *             when this file is not representing any URI
     * @see DataSpacesURI#isSuitableForUserPath()
     * @see #getURI()
     */
    public DataSpacesURI getDataSpacesURI() throws MalformedURIException {
        final FileName name = getName();
        if (!spaceRootFileName.isDescendent(name, NameScope.DESCENDENT_OR_SELF)) {
            throw new MalformedURIException(
                "VFS path of this DataSpacesFileObject does not start with its space VFS path");
        }

        String relativePath;
        try {
            relativePath = spaceRootFileName.getRelativeName(name);
        } catch (org.apache.commons.vfs2.FileSystemException e) {
            ProActiveLogger.logImpossibleException(logger, e);
            throw new ProActiveRuntimeException(e);
        }
        if (".".equals(relativePath))
            relativePath = null;

        return spaceRootUri.withRelativeToSpace(relativePath);
    }

    @Override
    protected boolean isReadOnly() {
        return readOnly;
    }

    @Override
    protected boolean canReturnAncestor(final DataSpacesLimitingFileObject fileObject) {
        try {
            final DataSpacesURI uri = fileObject.getDataSpacesURI();
            return uri.isSuitableForUserPath();
        } catch (MalformedURIException x) {
            return false;
        }
    }

    @Override
    protected DataSpacesLimitingFileObject doDecorateFile(FileObject file) {
        return new DataSpacesLimitingFileObject(file, spaceRootUri, spaceRootFileName, ownerActiveObjectId);
    }

    private boolean computeIsReadOnly() {
        final DataSpacesURI uri;
        try {
            uri = getDataSpacesURI();
        } catch (MalformedURIException e) {
            // ignore that kind of files
            return true;
        }

        // real logic
        switch (uri.getSpaceType()) {
            case INPUT:
                return true;
            case OUTPUT:
                return false;
            case SCRATCH:
                final String uriAoId = uri.getActiveObjectId();
                return uriAoId == null || !uriAoId.equals(ownerActiveObjectId);
            default:
                throw new IllegalStateException("Unexpected space type");
        }
    }
}
