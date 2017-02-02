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
