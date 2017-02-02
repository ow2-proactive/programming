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
