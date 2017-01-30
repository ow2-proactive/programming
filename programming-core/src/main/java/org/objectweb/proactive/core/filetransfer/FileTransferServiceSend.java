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

import java.io.File;


public interface FileTransferServiceSend {

    /**
     * This method handles the sending of a file.
     * @param ftsRemote The remote FileTransferService object that will receive the file.
     * @param srcFile The local source of the file.
     * @param dstFile The remote destination of the file.
     * @param bsize The size of the blocks the file will be split into.
     * @param numFlyingBlocks The number of simultaneous blocks that will be sent.
     * @return The result status of the operation.
     */
    public OperationStatus send(File srcFile, FileTransferServiceReceive ftsRemote, File dstFile, int bsize,
            int numFlyingBlocks);

    public void putBackInPool(FileTransferServiceReceive ftsDst);
}
