/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.core;

import java.io.Serializable;


/**
 * Data space type, defining its semantics.
 *
 * This enum has defined order and it is possible to access it successor through {@link #succ()}
 * method.
 */
public enum SpaceType implements Serializable, Comparable<SpaceType> {
    /**
     * Application input.
     */
    INPUT,
    /**
     * Application output.
     */
    OUTPUT,
    /**
     * Temporary storage, associated with node, but used by/partially associated to an application.
     */
    SCRATCH;

    /**
     * @return directory name in URI for that data space type
     */
    public String getDirectoryName() {
        return name().toLowerCase();
    }

    /**
     * @return next enum in order, or null this is the last one
     */
    public SpaceType succ() {
        final SpaceType[] v = SpaceType.values();
        final int n = this.ordinal() + 1;

        if (n < v.length)
            return v[n];
        else
            return null;
    }
}
