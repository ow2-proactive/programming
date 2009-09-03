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
