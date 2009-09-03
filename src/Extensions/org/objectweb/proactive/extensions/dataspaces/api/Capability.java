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
     * Provides directories which allows you to read its content through {@link org.apache.commons.vfs.FileContent#getInputStream()}
     */
    DIRECTORY_READ_CONTENT;
}
