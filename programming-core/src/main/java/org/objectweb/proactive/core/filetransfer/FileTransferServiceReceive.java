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
import java.io.IOException;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


public interface FileTransferServiceReceive {
    public void openWrite(File file) throws IOException;

    public BooleanWrapper closeWrite(File f);

    public void saveFileBlock(File dstFile, FileBlock block) throws IOException;

    public void saveFileBlockWithoutThrowingException(File dstFile, FileBlock block);

    public void putBackInLocalPool();

    public OperationStatus mkdirs(File dstFile);

    public boolean remove(File file);

    public boolean exists(File path);

    public boolean isDirectory(File path);

    public boolean isFile(File path);
}
