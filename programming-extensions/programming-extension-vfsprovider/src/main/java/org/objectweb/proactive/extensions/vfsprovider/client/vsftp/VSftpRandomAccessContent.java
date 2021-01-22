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
package org.objectweb.proactive.extensions.vfsprovider.client.vsftp;

/**
 * @author ActiveEon Team
 * @since 08/01/2021
 */

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.AbstractRandomAccessStreamContent;
import org.apache.commons.vfs2.util.RandomAccessMode;


/**
 * Random access content.
 */
class VSftpRandomAccessContent extends AbstractRandomAccessStreamContent {

    /**
     * file pointer
     */
    protected long filePointer = 0;

    private final VSftpFileObject fileObject;

    private DataInputStream dis = null;

    private InputStream mis = null;

    VSftpRandomAccessContent(final VSftpFileObject fileObject, final RandomAccessMode mode) {
        super(mode);

        this.fileObject = fileObject;
        // fileSystem = (FtpFileSystem) this.fileObject.getFileSystem();
    }

    @Override
    public long getFilePointer() throws IOException {
        return filePointer;
    }

    @Override
    public void seek(final long pos) throws IOException {
        if (pos == filePointer) {
            // no change
            return;
        }

        if (pos < 0) {
            throw new FileSystemException("vfs.provider/random-access-invalid-position.error", Long.valueOf(pos));
        }
        if (dis != null) {
            close();
        }

        filePointer = pos;
    }

    @Override
    protected DataInputStream getDataInputStream() throws IOException {
        if (dis != null) {
            return dis;
        }

        // FtpClient client = fileSystem.getClient();
        mis = fileObject.getInputStream(filePointer);
        dis = new DataInputStream(new FilterInputStream(mis) {
            @Override
            public int read() throws IOException {
                final int ret = super.read();
                if (ret > -1) {
                    filePointer++;
                }
                return ret;
            }

            @Override
            public int read(final byte[] b) throws IOException {
                final int ret = super.read(b);
                if (ret > -1) {
                    filePointer += ret;
                }
                return ret;
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                final int ret = super.read(b, off, len);
                if (ret > -1) {
                    filePointer += ret;
                }
                return ret;
            }

            @Override
            public void close() throws IOException {
                VSftpRandomAccessContent.this.close();
            }
        });

        return dis;
    }

    @Override
    public void close() throws IOException {
        if (dis != null) {
            // mis.abort();
            mis.close();

            // this is to avoid recursive close
            final DataInputStream oldDis = dis;
            dis = null;
            oldDis.close();
            mis = null;
        }
    }

    @Override
    public long length() throws IOException {
        return fileObject.getContent().getSize();
    }
}
