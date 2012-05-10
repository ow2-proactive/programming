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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;


/**
 * Stream adapter for {@link InputStream} of a {@link File}, allowing the sequential readings.
 */
public class InputStreamAdapter implements Stream {

    private final InputStream adaptee;

    /**
     * Adapt input stream of a specified file.
     *
     * @param file
     *            of which stream is to be open
     * @throws FileNotFoundException
     *             when specified file does not exist
     */
    public InputStreamAdapter(File file) throws FileNotFoundException {
        adaptee = new FileInputStream(file);
    }

    public void close() throws IOException {
        adaptee.close();
    }

    public long getLength() throws WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public long getPosition() throws WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public byte[] read(int bytes) throws IOException {
        final byte[] data = new byte[bytes];
        final int count = adaptee.read(data);

        if (count == -1)
            return null;
        if (count < bytes) {
            byte[] ret = new byte[count];
            System.arraycopy(data, 0, ret, 0, ret.length);
            return ret;
        }

        return data;
    }

    public void seek(long position) throws WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    /**
     * This skip implementation must deal with the SUN's JVM closed Bug ID: 6294974 and 4454092
     * (duplicate) that redefined the {@link FileInputStream#skip(long)} behavior: "This method may
     * skip more bytes than are remaining in the backing file. This produces no exception and the
     * number of bytes skipped may include some number of bytes that were beyond the EOF of the
     * backing file". Hence skips only available bytes. (Or the Stream interface should be less
     * strict on the return value)
     **/
    public long skip(long bytes) throws IOException {
        final long avail = adaptee.available();
        if (avail < bytes)
            bytes = avail;
        return adaptee.skip(bytes);
    }

    public void write(byte[] data) throws WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public void flush() throws WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }
}
