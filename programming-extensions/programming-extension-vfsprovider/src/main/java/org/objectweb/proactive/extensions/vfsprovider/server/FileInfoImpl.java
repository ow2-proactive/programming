/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.vfsprovider.server;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileInfo;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileType;


public class FileInfoImpl implements FileInfo {

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
            throw new IOException6(sec);
        }
        if (lastModifiedTime == 0) {
            throw new IOException("An error occurred while reading \"last modified time\" attribute on " +
                                  (file.isDirectory() ? "directory" : "file") + " " + file.getPath());
        }
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
