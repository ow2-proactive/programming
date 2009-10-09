/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
