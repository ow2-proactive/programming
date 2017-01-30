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

import java.io.IOException;

import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.StreamOperations;


/**
 * Interface defining a set of operations that can be performed on a file stream from
 * {@link FileSystemServerImpl}.
 * <p>
 * Implementations of this interface adapt variety of streams, hence
 * {@link WrongStreamTypeException} is thrown when particular operation is not supported. Methods
 * without {@link WrongStreamTypeException} in their <code>throws</code> clause must be supported by
 * each implementation.
 */
public interface Stream {

    /**
     * @throws IOException
     * @see StreamOperations#streamClose(long)
     */
    public abstract void close() throws IOException;

    /**
     * @throws IOException
     * @throws WrongStreamTypeException
     * @see StreamOperations#streamGetLength(long)
     */
    public abstract long getLength() throws IOException, WrongStreamTypeException;

    /**
     * @throws IOException
     * @throws WrongStreamTypeException
     * @see StreamOperations#streamGetPosition(long)
     */
    public abstract long getPosition() throws IOException, WrongStreamTypeException;

    /**
     * @param bytes
     * @throws IOException
     * @throws WrongStreamTypeException
     * @see StreamOperations#streamRead(long, int)
     */
    public abstract byte[] read(int bytes) throws IOException, WrongStreamTypeException;

    /**
     * @param position
     * @throws IOException
     * @throws WrongStreamTypeException
     * @see StreamOperations#streamSeek(long, long)
     */
    public abstract void seek(long position) throws IOException, WrongStreamTypeException;

    /**
     * @param bytes
     * @throws IOException
     * @throws WrongStreamTypeException
     * @see StreamOperations#streamSkip(long, long)
     */
    public abstract long skip(long bytes) throws IOException, WrongStreamTypeException;

    /**
     * @param data
     * @throws IOException
     * @throws WrongStreamTypeException
     * @see StreamOperations#streamWrite(long, byte[])
     */
    public abstract void write(byte[] data) throws IOException, WrongStreamTypeException;

    /**
     * @throws IOException
     * @throws WrongStreamTypeException
     * @see StreamOperations#streamFlush(long)
     */
    public abstract void flush() throws IOException, WrongStreamTypeException;
}
