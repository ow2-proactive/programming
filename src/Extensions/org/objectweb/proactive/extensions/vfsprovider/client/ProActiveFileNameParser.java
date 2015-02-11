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

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.HostFileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;


/**
 * VFS file name parser for ProActive file access protocol, transforming String representation of
 * URL to {@link ProActiveFileName} instances.
 *
 * @see ProActiveFileName
 */
public class ProActiveFileNameParser extends HostFileNameParser {
    private final static ProActiveFileNameParser INSTANCE = new ProActiveFileNameParser();

    public static ProActiveFileNameParser getInstance() {
        return INSTANCE;
    }

    private static String extractServicePath(StringBuilder path) throws FileSystemException {
        if (path.length() > 0 && path.charAt(0) != FileName.SEPARATOR_CHAR) {
            throw new FileSystemException(
                "Invalid path in URI: service path after host name does not begin with slash");
        }

        int idx = path.indexOf(ProActiveFileName.SERVICE_AND_FILE_PATH_SEPARATOR);
        if (idx == -1) {
            // simply assume that whole path is a service path
            final String servicePath = path.toString();
            path.delete(0, path.length());
            return servicePath;
        }
        final String servicePath = path.substring(0, idx);
        path.delete(0, idx + ProActiveFileName.SERVICE_AND_FILE_PATH_SEPARATOR.length());
        return servicePath;
    }

    public ProActiveFileNameParser() {
        // dummy number, as we do not have one default port
        super(-1);
    }

    @Override
    public FileName parseUri(VfsComponentContext context, FileName base, String filename)
            throws FileSystemException {
        final StringBuilder name = new StringBuilder();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath(filename, name);

        // Extract the server service path before processing the file path.
        final String servicePath = extractServicePath(name);

        // Decode and adjust separators
        UriParser.canonicalizePath(name, 0, name.length(), this);
        UriParser.fixSeparators(name);

        // Normalize the path.
        final FileType fileType = UriParser.normalisePath(name);
        final String path = name.toString();

        try {
            return new ProActiveFileName(auth.getScheme(), auth.getHostName(), auth.getPort(), auth.getUserName(), auth.getPassword(),
                servicePath, path, fileType);
        } catch (UnknownProtocolException e) {
            throw new FileSystemException("Unknown protocol scheme of URL", e);
        }
    }
}
