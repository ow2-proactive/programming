/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.calcium.system.files;

import java.io.File;
import java.util.IdentityHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.environment.FileServerClient;
import org.objectweb.proactive.extensions.calcium.stateness.Handler;
import org.objectweb.proactive.extensions.calcium.system.ProxyFile;


/**
 * This class is used to transfor File objects into ProxyFile objects.
 *
 * @author The ProActive Team
 */
class HandlerFile2ProxyFile implements Handler<File> {
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_SYSTEM);
    FileServerClient fserver;
    IdentityHashMap<ProxyFile, ProxyFile> files;

    HandlerFile2ProxyFile(FileServerClient fserver, IdentityHashMap<ProxyFile, ProxyFile> files) {
        this.fserver = fserver;
        this.files = files;
    }

    public ProxyFile transform(File f) throws Exception {
        ProxyFile pfile = new ProxyFile(f.getParentFile(), f.getName());

        pfile.store(fserver, 1);

        files.put(pfile, pfile);

        return pfile;
    }

    public boolean matches(Object o) {
        return File.class.isAssignableFrom(o.getClass()) && !ProxyFile.class.isAssignableFrom(o.getClass());
    }
}
