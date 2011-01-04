/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.objectweb.proactive.api.PAFileTransfer;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.StoredFile;


public class FileServerClientImpl implements FileServerClient, java.io.Serializable {
    Node node;
    FileServer fserver;

    public FileServerClientImpl(Node node, FileServer fserver) {
        this.node = node;
        this.fserver = fserver;
    }

    public void commit(long fileId, int refCountDelta) {
        logger.debug(fserver.getClass());
        fserver.commit(fileId, refCountDelta);
    }

    public void fetch(StoredFile rfile, File localDst) throws IOException {
        fserver.canFetch(rfile);

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Pulling file:" + rfile.location + " -> " + localDst);
            }
            RemoteFile fetchedFile = PAFileTransfer.pull(node, rfile.location, localDst);
            fetchedFile.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("Unable to fetch remote file: " + rfile);
        }
    }

    public StoredFile store(File current, int refCount) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Storing data for file:" + current);
        }
        StoredFile rfile = fserver.register();

        try {
            //SkeletonSystemImpl.copyFile(localFile, dst);
            RemoteFile sentFile = PAFileTransfer.push(current, node, rfile.location);
            sentFile.waitFor();
        } catch (Exception e) {
            //If exception happens, then unstore the file.
            fserver.unregister(rfile.fileId);
            e.printStackTrace();
            throw new IOException("Unable to store file on File Server: src=" + current);
        }

        //now mark as stored
        return fserver.dataHasBeenStored(rfile, refCount);
    }

    public StoredFile store(URL current) throws IOException {
        return fserver.registerAndStore(current);
    }

    public void shutdown() {
        fserver.shutdown();
    }
}
