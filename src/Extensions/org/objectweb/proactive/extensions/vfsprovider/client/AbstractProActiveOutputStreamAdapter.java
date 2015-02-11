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
package org.objectweb.proactive.extensions.vfsprovider.client;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileSystemException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;


/**
 * Generic adapter of remotely accessed {@link FileSystemServer} to {@link OutputStream} class.
 */
abstract class AbstractProActiveOutputStreamAdapter extends OutputStream {
    private static final Log log = LogFactory.getLog(AbstractProActiveOutputStreamAdapter.class);

    private final byte[] SINGLE_BYTE_BUF = new byte[1];

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len + off > b.length) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        final byte bytesToSent[];
        if (off != 0 || len != b.length) {
            bytesToSent = new byte[len];
            System.arraycopy(b, off, bytesToSent, 0, len);
        } else {
            bytesToSent = b;
        }

        try {
            try {
                getServer().streamWrite(getStreamId(), bytesToSent);
            } catch (StreamNotFoundException e) {
                reopenStream();
                getServer().streamWrite(getStreamId(), bytesToSent);
            }
            notifyBytesWritten(bytesToSent.length);
        } catch (WrongStreamTypeException e) {
            throw Utils.generateAndLogIOExceptionWrongStreamType(log, e);
        } catch (StreamNotFoundException e) {
            throw Utils.generateAndLogIOExceptionStreamNotFound(log, e);
        }
    }

    @Override
    public void write(int b) throws IOException {
        SINGLE_BYTE_BUF[0] = (byte) b;
        write(SINGLE_BYTE_BUF);
    }

    @Override
    public synchronized void flush() throws IOException {
        try {
            getServer().streamFlush(getStreamId());
        } catch (WrongStreamTypeException e) {
            throw Utils.generateAndLogIOExceptionWrongStreamType(log, e);
        } catch (StreamNotFoundException e) {
            // as long as FileSystemServer guarantees that this exception can occur
            // after streamOpen() only after proper close at server side,
            // we do not need to open it to flush it again - we can ignore it
        }
    }

    protected abstract long getStreamId();

    protected abstract FileSystemServer getServer() throws FileSystemException;

    @Override
    public abstract void close() throws IOException;

    protected abstract void notifyBytesWritten(long bytesNumber);

    protected abstract void reopenStream() throws IOException;
}