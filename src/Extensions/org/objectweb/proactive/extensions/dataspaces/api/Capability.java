/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package org.objectweb.proactive.extensions.dataspaces.api;

/**
 */
public enum Capability {
    /**
     * File content can be read.
     */
    READ_CONTENT,

    /**
     * File content can be written.
     */
    WRITE_CONTENT,

    /**
     * File content can be read in random mode.
     */
    RANDOM_ACCESS_READ,

    /**
     * File content can be written in random mode.
     */
    RANDOM_ACCESS_WRITE,

    /**
     * File content can be appended.
     */
    APPEND_CONTENT,

    /**
     * File attributes are supported.
     */
    ATTRIBUTES,

    /**
     * File last-modified time is supported.
     */
    LAST_MODIFIED,

    /**
     * File get last-modified time is supported.
     */
    GET_LAST_MODIFIED,

    /**
     * File set last-modified time is supported.
     */
    SET_LAST_MODIFIED_FILE,

    /**
     * folder set last-modified time is supported.
     */
    SET_LAST_MODIFIED_FOLDER,

    /**
     * File content signing is supported.
     */
    SIGNING,

    /**
     * Files can be created.
     */
    CREATE,

    /**
     * Files can be deleted.
     */
    DELETE,

    /**
     * Files can be renamed.
     */
    RENAME,
    /**
     * The file type can be determined.
     */
    GET_TYPE,

    /**
     * Children of files can be listed.
     */
    LIST_CHILDREN,

    /**
     * URI are supported.  Files without this capability use URI that do not
     * globally and uniquely identify the file.
     */
    URI,

    /**
     * File system attributes are supported.
     */
    FS_ATTRIBUTES,

    /**
     * The set of attributes defined by the Jar manifest specification are
     * supported.  The attributes aren't necessarily stored in a manifest file.
     */
    MANIFEST_ATTRIBUTES,

    /**
     * A compressed filesystem is a filesystem which use compression.
     */
    COMPRESS,

    /**
     * A virtual filesystem can be an archive like tar or zip.
     */
    VIRTUAL,

    /**
     * Provides directories which allows you to read its content through {@link org.apache.commons.vfs2.FileContent#getInputStream()}
     */
    DIRECTORY_READ_CONTENT;
}
