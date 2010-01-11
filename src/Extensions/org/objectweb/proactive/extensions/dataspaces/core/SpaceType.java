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
