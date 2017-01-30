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
package org.objectweb.proactive.core.filetransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class represents a fraction of a file.
 * It is used to load file blocks from a file, to send them
 * through the network, and later to store the file block on the remote machines.
 *
 * @author The ProActive Team
 *
 */
public class FileBlock implements Serializable {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);

    public static final int DEFAULT_BLOCK_SIZE = CentralPAPropertyRepository.PA_FILETRANSFER_MAX_BLOCK_SIZE.getValue() *
                                                 1024; //Bytes

    private byte[] buffer;

    private int usage;

    private long offset;

    private int blockSize;

    private Exception exception;

    /**
     * Empty ProActive constructor.
     */
    public FileBlock() {
    }

    public FileBlock(long offset) {
        this(offset, DEFAULT_BLOCK_SIZE);
    }

    public FileBlock(long offset, int blockSize) {
        this.offset = offset;
        this.blockSize = blockSize;
        this.buffer = new byte[blockSize];

        this.exception = null;
        this.usage = 0;
    }

    /**
     * Loads the FileBlock object with a block from the source file this block references.
     * If the parameter is null, then it will create a new buffer from the parameters stored in
     * the block instance. Note that creating a new block requires performing a skip (seek) on the
     * stream, which is very slow. Therefore it is better to pass the buffered stream as parameter.
     */
    public void loadNextBlock(BufferedInputStream bis) throws IOException {
        if (bis == null) {
            throw new IllegalArgumentException("Can not handle null BufferInputStream parameter.");
        }

        try {
            usage = bis.read(buffer, 0, blockSize);
            offset += usage;
        } catch (IOException e) {
            usage = 0;
            throw e;
        }
    }

    /**
     * Saves the current block into an output stream buffer.
     * @param bos The BufferedOutputStream to save the file
     * @throws IOException If an error is encountered.
     */
    public void saveCurrentBlock(BufferedOutputStream bos) throws IOException {
        if (bos == null) {
            throw new IllegalArgumentException("Can not handle null BufferedOutputStream parameter.");
        }

        bos.write(buffer, 0, usage);
    }

    /**
     * @return Returns the offset.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @return Returns the blockSize.
     */
    public int getBlockSize() {
        return blockSize;
    }

    public Exception getException() {
        return exception;
    }

    public boolean hasException() {
        return exception != null;
    }

    public void setException(Exception e) {
        exception = e;
    }
}
