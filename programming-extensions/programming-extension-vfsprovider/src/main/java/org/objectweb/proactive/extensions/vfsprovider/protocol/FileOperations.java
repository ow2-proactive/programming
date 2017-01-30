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
import java.util.Map;
import java.util.Set;


/**
 * Defines set of protocol operations that are related to a file managing operations.
 * <p>
 * Implementations of this interface are thread-safe although simultaneous operations on the same
 * files may lead to undefined results.
 */
public interface FileOperations {
    /**
     * List names of the existing directory's direct children, specified by given abstract
     * <code>path</code>.
     *
     * @param path
     *            of a file, cannot be <code>null</code>
     * @return set of names of child files and directories, may be empty if the directory is empty;
     *         <code>null</code> when specified file is not an existing directory or cannot be
     *         accessed
     * @throws IOException
     *             when an security or I/O error occurred
     */
    public abstract Set<String> fileListChildren(String path) throws IOException;

    /**
     * List names and information of the existing directory's direct children, specified by given
     * abstract <code>path</code>.
     * <p>
     * Returned set of information of a single or all files may be not coherent, as the simultaneous
     * file system operations, that can change attributes while reading, are not prohibited.
     *
     * @param path
     *            of a file, cannot be <code>null</code>
     * @return map of names and file information of child files and directories, may be empty if the
     *         directory is empty; <code>null</code> when specified file is not an existing
     *         directory or cannot be accessed
     * @throws IOException
     *             when specified file is not an existing directory, when unable to read
     *             "last modified time" attribute, or when an security or I/O error occurred
     */
    public abstract Map<String, FileInfo> fileListChildrenInfo(String path) throws IOException;

    /**
     * Reads the information of an existing file specified by given abstract <code>path</code>.
     * <p>
     * Returned set of information may be not coherent, as the simultaneous file system operations,
     * that can change attributes while reading, are not prohibited.
     *
     * @param path
     *            of a file, cannot be <code>null</code>
     * @return the file information or <code>null</code> when file does not exist
     * @throws IOException
     *             when unable to read "last modified time" attribute or security exception occurred
     */
    public abstract FileInfo fileGetInfo(String path) throws IOException;

    /**
     * Create a file of a <code>type</code> with given abstract <code>path</code>, along with the
     * ancestor directories if they hasn't existed yet. If file is already created, it remains
     * unchanged and method terminates normally. This method checks the postcondition, that the file
     * exists and is of a proper <code>type</code>.
     * <p>
     * Note: The postcondition check may be influenced by any concurrent file system operation.
     *
     * @param path
     *            of a file that is to be created, cannot be <code>null</code>
     * @param type
     *            of a file that is to be created, cannot be <code>null</code>
     * @throws IOException
     *             when postcondition check failed or security exception occurred
     */
    public abstract void fileCreate(String path, FileType type) throws IOException;

    /**
     * Delete a file specified by given abstract <code>path</code>. When specified file is a
     * directory, then it must be empty, unless the recursive deleting is specified. This method
     * check the postcondition, that the file does not exist.
     * <p>
     * Note: The postcondition check may be influenced by any concurrent file system operation.
     * <p>
     * When attempting to delete not existing file, this method terminates normally.
     *
     * @param path
     *            of a file that is to be deleted, cannot be <code>null</code>
     * @param recursive
     *            <code>true</code> if file is to be deleted recursively, <code>false</code>
     *            otherwise
     * @throws IOException
     *             when postcondition check failed, deleting not empty directory without recursive
     *             option or security exception occurred
     */
    public abstract void fileDelete(String path, boolean recursive) throws IOException;

    /**
     * Rename a file specified by given abstract <code>path</code> to a <code>newPath</code>.
     * Behavior of this method is inherently platform-dependent, as no move or atomicy guarantees
     * can be assumed.
     *
     * @param path
     *            path of a file to rename, cannot be <code>null</code>
     * @param newPath
     *            a new path of a renamed file, cannot be <code>null</code>
     * @throws IOException
     *             when rename operation failed of security exception occurred
     */
    public abstract void fileRename(String path, String newPath) throws IOException;

    /**
     * Set the "last modified time" property of an existing file (ordinal file or directory)
     * specified by given abstract <code>path</code> to <code>time</code>.
     * <p>
     * Precision of the "last modified time" property is related to the particular file system,
     * although all platforms support file's last modification time to the nearest second.
     *
     * @param path
     *            path of a file to rename, cannot be <code>null</code>
     * @param time
     *            new last-modified time, measured in milliseconds since the epoch (00:00:00 GMT,
     *            January 1, 1970)
     * @return <code>true</code> if the operation succeeded, <code>false</code> otherwise
     * @throws IOException
     *             when file does not exist or security exception occurred
     */
    public abstract boolean fileSetLastModifiedTime(String path, long time) throws IOException;
}
