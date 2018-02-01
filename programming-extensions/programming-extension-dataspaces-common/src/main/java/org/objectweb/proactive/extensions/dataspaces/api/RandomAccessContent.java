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
package org.objectweb.proactive.extensions.dataspaces.api;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;


public interface RandomAccessContent extends DataOutput, DataInput {
    /**
     * Returns the current offset in this file.
     *
     * @return the offset from the beginning of the file, in bytes, at which the next read or write
     *         occurs.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public long getFilePointer() throws IOException;

    /**
     * Sets the file-pointer offset, measured from the beginning of this file, at which the next
     * read or write occurs. The offset may be set beyond the end of the file. Setting the offset
     * beyond the end of the file does not change the file length. The file length will change only
     * by writing after the offset has been set beyond the end of the file. <br/>
     * <b>Notice: If you use {@link #getInputStream()} you have to reget the InputStream after
     * calling {@link #seek(long)}</b>
     *
     * @param pos
     *            the offset position, measured in bytes from the beginning of the file, at which to
     *            set the file pointer.
     * @throws IOException
     *             if <code>pos</code> is less than <code>0</code> or if an I/O error occurs.
     */
    public void seek(long pos) throws IOException;

    /**
     * Returns the length of this file.
     *
     * @return the length of this file, measured in bytes.
     * @throws IOException
     *             if an I/O error occurs.
     */
    public long length() throws IOException;

    /**
     * Closes this random access file stream and releases any system resources associated with the
     * stream. A closed random access file cannot perform input or output operations and cannot be
     * reopened.
     * <p/>
     * <p>
     * If this file has an associated channel then the channel is closed as well.
     *
     * @throws IOException
     *             if an I/O error occurs.
     */
    public void close() throws IOException;

    /**
     * get the input stream <br/>
     * <b>Notice: If you use {@link #seek(long)} you have to reget the InputStream</b>
     */
    public InputStream getInputStream() throws IOException;
}
