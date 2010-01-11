/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
