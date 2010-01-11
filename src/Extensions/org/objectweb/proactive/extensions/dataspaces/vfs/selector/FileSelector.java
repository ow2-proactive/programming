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
package org.objectweb.proactive.extensions.dataspaces.vfs.selector;

/**
 * This interface is used to select files when traversing a file hierarchy.
 */
public interface FileSelector {
    /**
     * Determines if a file or folder should be selected.  This method is
     * called in depthwise order (that is, it is called for the children
     * of a folder before it is called for the folder itself).
     *
     * @param fileInfo the file or folder to select.
     * @return true if the file should be selected.
     * @throws Exception if an error occurs.
     */
    boolean includeFile(FileSelectInfo fileInfo) throws Exception;

    /**
     * Determines whether a folder should be traversed.  If this method returns
     * true, {@link #includeFile} is called for each of the children of
     * the folder, and each of the child folders is recursively traversed.
     * <p/>
     * <p>This method is called on a folder before {@link #includeFile}
     * is called.
     *
     * @param fileInfo the file or folder to select.
     * @return true if the folder should be traversed.
     * @throws Exception if an error occurs.
     */
    boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception;
}
