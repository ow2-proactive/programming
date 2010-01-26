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
package org.objectweb.proactive.extensions.calcium.environment.multithreaded;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.objectweb.proactive.extensions.calcium.environment.FileServer;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.environment.StoredFile;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;


public class FileServerClientImpl implements FileServerClient {
    FileServer fserver;

    public FileServerClientImpl(FileServer fserver) {
        this.fserver = fserver;
    }

    public void commit(long fileId, int refCountDelta) {
        fserver.commit(fileId, refCountDelta);
    }

    public void fetch(StoredFile rfile, File localDst) throws IOException {
        fserver.canFetch(rfile);

        SkeletonSystemImpl.copyFile(rfile.location, localDst);
    }

    public void shutdown() {
        fserver.shutdown();
    }

    public StoredFile store(File current, int refCount) throws IOException {
        StoredFile rfile = fserver.register();

        SkeletonSystemImpl.copyFile(current, rfile.location);

        //now mark as stored
        return fserver.dataHasBeenStored(rfile, refCount);
    }

    public StoredFile store(URL current) throws IOException {
        return fserver.registerAndStore(current);
    }
}
