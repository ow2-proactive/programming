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
package org.objectweb.proactive.extensions.vfsprovider.server;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileInfo;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileType;


public class FileInfoImpl implements FileInfo {

    private static final long serialVersionUID = 60L;

    private final long lastModifiedTime;

    private final long size;

    private final FileType fileType;

    private final boolean hidden;

    private final boolean readable;

    private final boolean writable;

    public FileInfoImpl(File file) throws IOException {
        try {
            lastModifiedTime = file.lastModified();
            size = file.length();
            fileType = file.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
            hidden = file.isHidden();
            readable = file.canRead();
            writable = file.canWrite();
        } catch (SecurityException sec) {
            throw new IOException6("", sec);
        }
        if (lastModifiedTime == 0)
            throw new IOException("Unable to read \"last modified time\" attribute");
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public long getSize() {
        return size;
    }

    public FileType getType() {
        return fileType;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }
}
