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
package org.objectweb.proactive.extensions.vfsprovider.protocol;

import java.io.IOException;


/**
 * Protocol definition made of two parts: {@link StreamOperations} and {@link FileOperations}.
 * <p>
 * Instances of this class are intended to work as remote objects and hence are thread-safe.
 * <p>
 * All paths are absolute and refer to a file system tree that root definition depends on a
 * particular implementation. Hence every access is limited to that "change rooted" file system.
 * Each path is expected to begin with UNIX styled <code>/</code> separator, although DOS like
 * separators <code>\</code> are allowed. Any violation of above mentioned rules cause that
 * {@link IOException} is thrown.
 */
public interface FileSystemServer extends StreamOperations, FileOperations {
}
