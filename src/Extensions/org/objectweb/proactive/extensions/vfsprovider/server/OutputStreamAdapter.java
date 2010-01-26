/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
