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
package org.objectweb.proactive.extensions.dataspaces.api;

public enum FileSelector {

    /**
     * A {@link FileSelector} that selects only the base file/folder.
     */
    SELECT_SELF,

    /**
     * A {@link FileSelector} that selects the base file/folder and its
     * direct children.
     */
    SELECT_SELF_AND_CHILDREN,

    /**
     * A {@link FileSelector} that selects only the direct children
     * of the base folder.
     */
    SELECT_CHILDREN,

    /**
     * A {@link FileSelector} that selects all the descendents of the
     * base folder, but does not select the base folder itself.
     */
    EXCLUDE_SELF,

    /**
     * A {@link FileSelector} that only files (not folders).
     */
    SELECT_FILES,

    /**
     * A {@link FileSelector} that only folders (not files).
     */
    SELECT_FOLDERS,

    /**
     * A {@link FileSelector} that selects the base file/folder, plus all
     * its descendents.
     */
    SELECT_ALL
}
