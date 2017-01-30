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

import org.objectweb.proactive.api.PAFileTransfer;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;


public class RemoteFileImpl implements RemoteFile {
    Node node;

    File file;

    OperationStatus status;

    public RemoteFileImpl(Node node, File file, OperationStatus status) {
        this.node = node;
        this.file = file;
        this.status = status;
    }

    //@Override
    public RemoteFile pull(File localDst) throws IOException {
        waitFor();

        Node localNode;

        try {
            localNode = NodeFactory.getDefaultNode();
        } catch (NodeException e) {
            //TODO change when moving to Java 1.6
            //throw new IOException("Can't get local node", e);
            throw new IOException("Can't get local node " + e.getMessage());
        }

        return PAFileTransfer.transfer(node, file, localNode, localDst);
    }

    //@Override
    public RemoteFile push(Node dstNode, File dstFile) throws IOException {
        waitFor();

        return PAFileTransfer.transfer(getRemoteNode(), getRemoteFilePath(), dstNode, dstFile);
    }

    //@Override
    public File getRemoteFilePath() {
        return file;
    }

    //@Override
    public Node getRemoteNode() {
        return node;
    }

    //@Override
    public boolean isFinished() {
        return !PAFuture.isAwaited(status);
    }

    //@Override
    public void waitFor() throws IOException {
        PAFuture.waitFor(status);

        if (status.hasException()) {
            throw status.getException();
        }
    }

    public boolean exists() throws IOException {
        waitFor();
        FileTransferServiceReceive ftsDst = getRemoteFileTransferService();

        return ftsDst.exists(file);
    }

    public boolean isDirectory() throws IOException {
        waitFor();
        FileTransferServiceReceive ftsDst = getRemoteFileTransferService();

        return ftsDst.isDirectory(file);
    }

    public boolean isFile() throws IOException {
        waitFor();
        FileTransferServiceReceive ftsDst = getRemoteFileTransferService();

        return ftsDst.isFile(file);
    }

    public boolean delete() throws IOException {
        waitFor();
        FileTransferServiceReceive ftsDst = getRemoteFileTransferService();

        return ftsDst.remove(file);
    }

    protected FileTransferServiceReceive getRemoteFileTransferService() throws IOException {
        try {
            return FileTransferEngine.getFileTransferEngine(node).getFTS();
        } catch (Exception e) {
            //TODO change when moving to Java 1.6
            //throw new IOException("Unable to connect or use ProActive Node: " + node, e);
            throw new IOException("Unable to connect or use ProActive Node: " + node + e.getMessage());
        }
    }
}
