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
