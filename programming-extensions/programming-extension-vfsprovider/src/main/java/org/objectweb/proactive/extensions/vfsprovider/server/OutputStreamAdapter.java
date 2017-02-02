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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;


/**
 * Stream adapter for {@link OutputStream} of a {@link File}, allowing the sequential writings.
 */
public class OutputStreamAdapter implements Stream {

    private final OutputStream adaptee;

    /**
     * Adapt output stream of specified file, in append or normal mode.
     *
     * @param file
     *            of which stream is to be open
     * @param append
     *            indicates if open stream in append or normal mode
     * @throws FileNotFoundException
     *             when specified file does not exist
     */
    public OutputStreamAdapter(File file, boolean append) throws FileNotFoundException {
        adaptee = new FileOutputStream(file, append);
    }

    public void close() throws IOException {
        adaptee.close();
    }

    public long getLength() throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public long getPosition() throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public byte[] read(int bytes) throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public void seek(long position) throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public long skip(long bytes) throws IOException, WrongStreamTypeException {
        throw new WrongStreamTypeException();
    }

    public void write(byte[] data) throws IOException, WrongStreamTypeException {
        adaptee.write(data);
    }

    public void flush() throws IOException, WrongStreamTypeException {
        adaptee.flush();
    }
}
