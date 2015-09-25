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
package org.objectweb.proactive.extensions.vfsprovider.client;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;


/**
 * VFS Provider for ProActive file access protocol, as specified in {@link FileSystemServer}.
 * <p>
 * This provider supports only predefined schemes, as specified in
 * {@link ProActiveFileName#getAllVFSSchemes()}. This limitations is caused by usage of wide-used
 * protocols as a transport for {@link FileSystemServer}, while VFS manager does not allow to
 * specify multiple providers per scheme and differentiate between them (consider
 * {@link FileSystemServer} exposed as remote object through HTTP).
 * <p>
 * Note regarding content access implementation: input stream, output stream and random access read
 * provide internal buffering, while random access write does not.
 */
public class ProActiveFileProvider extends AbstractOriginatingFileProvider {

    static final Set<Capability> CAPABILITIES = Collections.unmodifiableSet(new HashSet<Capability>(Arrays
            .asList(new Capability[] { Capability.READ_CONTENT, Capability.WRITE_CONTENT,
                    Capability.RANDOM_ACCESS_READ, Capability.RANDOM_ACCESS_WRITE, Capability.APPEND_CONTENT,
                    Capability.LAST_MODIFIED, Capability.GET_LAST_MODIFIED,
                    Capability.SET_LAST_MODIFIED_FILE, Capability.SET_LAST_MODIFIED_FOLDER,
                    Capability.CREATE, Capability.DELETE, Capability.RENAME, Capability.GET_TYPE,
                    Capability.LIST_CHILDREN, Capability.URI })));

    public ProActiveFileProvider() {
        setFileNameParser(ProActiveFileNameParser.getInstance());
    }

    public Collection<Capability> getCapabilities() {
        return CAPABILITIES;
    }

    @Override
    protected FileSystem doCreateFileSystem(FileName rootName, FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        return new ProActiveFileSystem(rootName, fileSystemOptions);
    }
}
