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

public enum FileType {

    /**
     * Represents a folder type
     */
    FOLDER(true, false, true),

    /**
     * Represents an ordinary file type
     */
    FILE(false, true, true),

    /**
     * Represents yet unknown file type
     */
    ABSTRACT(false, false, false);

    private final boolean hasChildren;

    private final boolean hasContent;

    private final boolean hasAttrs;

    private FileType(final boolean hasChildren, final boolean hasContent, final boolean hasAttrs) {

        this.hasChildren = hasChildren;
        this.hasContent = hasContent;
        this.hasAttrs = hasAttrs;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public boolean hasContent() {
        return hasContent;
    }

    public boolean hasAttrs() {
        return hasAttrs;
    }
}
