/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.vfsprovider.client;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.HostFileNameParser;
import org.apache.commons.vfs.provider.UriParser;
import org.apache.commons.vfs.provider.VfsComponentContext;
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

    private static String extractServicePath(StringBuffer path) throws FileSystemException {
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
        final StringBuffer name = new StringBuffer();

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
            return new ProActiveFileName(auth.scheme, auth.hostName, auth.port, auth.userName, auth.password,
                servicePath, path, fileType);
        } catch (UnknownProtocolException e) {
            throw new FileSystemException("Unknown protocol scheme of URL", e);
        }
    }
}
