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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.chartit.util;

public class Utils {

    public static final String EMPTY_STRING = "";

    public static final String PRIMITIVE_TYPE_INT = "int";

    public static final int MAX_RGB_VALUE = 255;

    public static final long SEED = 19580427l;

    /**
     * Checks if a string is contained in an array of string.
     * @param ar The array of string
     * @param o the element 
     * @return Returns <code>true</code> if str is contained in ar; <code>false</code> otherwise
     */
    public static final boolean contains(final Object[] ar, final Object o) {
        for (final Object oo : ar) {
            if (oo.equals(o))
                return true;
        }
        return false;
    }
}
