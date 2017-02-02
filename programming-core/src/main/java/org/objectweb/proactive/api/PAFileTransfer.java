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
package org.objectweb.proactive.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.filetransfer.FileBlock;
import org.objectweb.proactive.core.filetransfer.FileTransferEngine;
import org.objectweb.proactive.core.filetransfer.FileTransferService;
import org.objectweb.proactive.core.filetransfer.FileTransferServiceReceive;
import org.objectweb.proactive.core.filetransfer.FileTransferServiceSend;
import org.objectweb.proactive.core.filetransfer.OperationStatus;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.filetransfer.RemoteFileImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class provides a standard entry point for FileTransfer API tools.
 *
 * Transfer between the calling thread's node and a remote node is supported
 * through the <code>pull</code> and <code>push</code> methods.
 *
 * Transfer between third party nodes is supported using the <code>transfer</code> family methods
 *
 * All file transfer operations are performed asynchronously, and return a {@link org.objectweb.proactive.core.filetransfer.RemoteFile  RemoteFile} instance which can
 * be queried for termination.
 *
 * Note that an active objec's node can be obtained with:
 *
 * <pre>
 *                 String nodeURL = PAActiveObject.getActiveObjectNodeUrl(Object activeobject);
 *                 Node node = NodeFactory.getNode(nodeURL);
 * </pre>
 *
 * @author The ProActive Team
 * @since ProActive 3.9 (November 2007)
 */
@PublicAPI
public class PAFileTransfer {
    static Logger logger = ProActiveLogger.getLogger(Loggers.FILETRANSFER);

    protected static Node getLocalNode() throws IOException {
        Node localNode;

        try {
            localNode = NodeFactory.getDefaultNode();
        } catch (NodeException e) {
            //TODO change when moving to Java 1.6
            //throw new IOException("Can't get local node", e);
            throw new IOException("Can't get local node " + e.getMessage());
        }

        return localNode;
    }

    /**
     * Push a list of local files to a remote Node with the specified destinations.
     *
     * @param srcFile The list of local, existent, <code>File</code>s.
     * @param dstNode The destination node.
     * @param dstFile The list of remote, writable, <code>File<code>s.
     * @return A list of {@link org.objectweb.proactive.core.filetransfer.RemoteFile  RemoteFile} instances representing the file transfer operation of each file.
     * @throws IOException If an initialization error was detected.
     */
    public static List<RemoteFile> push(File[] srcFile, Node dstNode, File[] dstFile) throws IOException {
        return transfer(getLocalNode(), srcFile, dstNode, dstFile);
    }

    /**
     * Push a single local file to the remote node/location.
     * @see  #push(File[], Node,  File[])
     */
    public static RemoteFile push(File srcFile, Node dstNode, File dstFile) throws IOException {
        return transfer(getLocalNode(), srcFile, dstNode, dstFile);
    }

    /**
     * Pull a single remote file from a source Node to the local node.
     *
     * @param srcNode
     * @param srcFile
     * @param dstFile
     * @return A {@link org.objectweb.proactive.core.filetransfer.RemoteFile  RemoteFile} instance representing the file transfer operation of the file.
     * @see  #pull( Node, File[], File[])
     * @throws IOException If an initialization error was detected.
     */
    public static RemoteFile pull(Node srcNode, File srcFile, File dstFile) throws IOException {
        return transfer(srcNode, srcFile, getLocalNode(), dstFile);
    }

    /**
     * Pull a list of remote files from a source Node to the local node.
     *
     * @param srcNode
     * @param srcFile
     * @param dstFile
     * @throws IOException If an initialization error was detected.    
     * @see  #pull( Node, File, File)
     */
    public static List<RemoteFile> pull(Node srcNode, File[] srcFile, File[] dstFile) throws IOException {
        return transfer(srcNode, srcFile, getLocalNode(), dstFile);
    }

    /**
     * Transfers a single file between third parties.
     * @see #transfer(Node, File[], Node, File[])
     */
    public static RemoteFile transfer(Node srcNode, File srcFile, Node dstNode, File dstFile) throws IOException {
        List<RemoteFile> rfiles = transfer(srcNode, new File[] { srcFile }, dstNode, new File[] { dstFile });

        return rfiles.get(0);
    }

    /**
     * Transfers a list of files on a remote Node to another remote node.
     *
     * @param srcNode
     * @param srcFile
     * @param dstNode
     * @param dstFile
     * @return A list of {@link org.objectweb.proactive.core.filetransfer.RemoteFile  RemoteFile}.
     * @throws IOException  If an initialization error was detected.
     */
    public static List<RemoteFile> transfer(Node srcNode, File[] srcFile, Node dstNode, File[] dstFile)
            throws IOException {
        return transfer(srcNode,
                        srcFile,
                        dstNode,
                        dstFile,
                        FileBlock.DEFAULT_BLOCK_SIZE,
                        FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS);
    }

    /**
     * Transfers a list of files, with parameters different than default.
     *
     * @param bsize The size of each file block.
     * @param numFlyingBlocks The maximum number of blocks to send before synchronizing.
     *
     * @see #transfer(Node, File[], Node, File[])
     */
    public static List<RemoteFile> transfer(Node srcNode, File[] srcFile, Node dstNode, File[] dstFile, int bsize,
            int numFlyingBlocks) throws IOException {
        if (srcFile.length != dstFile.length) {
            throw new IOException("Error, number destination and source file lists do not match in length");
        }

        Node localNode = getLocalNode();

        // ArrayList<RemoteFile> rfile = new ArrayList<RemoteFile>(srcFile.length);

        // local side verifications
        for (int i = 0; i < srcFile.length; i++) {
            //Case srcNode is local
            if (FileTransferEngine.nodeEquals(srcNode, localNode)) {
                if (!srcFile[i].canRead()) {
                    logger.error("Can't read or doesn't exist: " + srcFile[i].getAbsoluteFile());
                    throw new IOException("Can't read or doesn't exist: " + srcFile[i].getAbsoluteFile());
                }
            }

            //Case dstNode is local
            if (FileTransferEngine.nodeEquals(dstNode, localNode)) {
                if (dstFile[i].exists() && !dstFile[i].canWrite()) {
                    logger.error("Can't write to file: " + dstFile[i].getAbsoluteFile());
                    throw new IOException("Can't overrite existant file: " + dstFile[i].getAbsoluteFile());
                }
            }

            //Case srcNode == dstNode
            if (FileTransferEngine.nodeEquals(srcNode, dstNode)) {
                if (dstFile[i].equals(srcFile[i])) {
                    logger.error("Can't copy, src and destination are the same: " + srcFile[i].getAbsolutePath());
                }
            }
        }

        return internalTransfer(srcNode, srcFile, dstNode, dstFile, bsize, numFlyingBlocks);
    }

    /**
     * This is the real method that invokes the file transfer.
     *
     * @param srcNode The source node.
     * @param srcFile The list of source files locations.
     * @param dstNode The destination node.
     * @param dstFile The list of destination locations.
     * @param bsize The block size to be used
     * @param numFlyingBlocks The maximum number of blocks to send before synchronizing
     * @return  A list of {@link org.objectweb.proactive.core.filetransfer.RemoteFile  RemoteFile} instances representing the file transfer operation of each file.
     * @throws IOException  If an initialization error was detected.
     */
    protected static List<RemoteFile> internalTransfer(Node srcNode, File[] srcFile, Node dstNode, File[] dstFile,
            int bsize, int numFlyingBlocks) throws IOException {
        FileTransferServiceSend ftsSrc;
        FileTransferServiceReceive ftsDst;

        try {
            ftsSrc = FileTransferEngine.getFileTransferEngine(srcNode).getFTS();
            ftsDst = FileTransferEngine.getFileTransferEngine(dstNode).getFTS();
        } catch (Exception e) {
            //TODO change when moving to Java 1.6
            //throw new IOException("Unable to connect or use ProActive Node: " + srcNode + " -> " + dstNode, e);
            throw new IOException("Unable to connect or use ProActive Node: " + srcNode + " -> " + dstNode + " " +
                                  e.getMessage());
        }

        ArrayList<RemoteFile> rfile = new ArrayList<RemoteFile>(srcFile.length);

        for (int i = 0; i < srcFile.length; i++) {
            OperationStatus status = ftsSrc.send(srcFile[i], ftsDst, dstFile[i], bsize, numFlyingBlocks);

            rfile.add(new RemoteFileImpl(dstNode, dstFile[i], status));
        }

        ftsSrc.putBackInPool(ftsDst);

        return rfile;
    }

    /**
     * Recursively creates a remote directory.
     *
     * @return true if the remote file is a file, false otherwise
     * @throws IOException If an initialization error was detected.
     */
    public static RemoteFile mkdirs(Node node, File path) throws IOException {
        FileTransferServiceReceive ftsDst;

        try {
            ftsDst = FileTransferEngine.getFileTransferEngine(node).getFTS();
        } catch (Exception e) {
            //TODO change when moving to Java 1.6
            //throw new IOException("Unable to connect or use ProActive Node: " + node, e);
            throw new IOException("Unable to connect or use ProActive Node: " + node + e.getMessage());
        }

        OperationStatus status = ftsDst.mkdirs(path);

        return new RemoteFileImpl(node, path, status);
    }
}
