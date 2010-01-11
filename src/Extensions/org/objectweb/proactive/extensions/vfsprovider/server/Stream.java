/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
