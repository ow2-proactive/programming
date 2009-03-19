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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.annotation.activeobject;

/**
 * Abstracts away the algorithm used to test if a field has getters/setters
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
public class GenerateGettersSetters {

    /**
     * generate a pattern which the getter method should match 
     * @param fieldName 
     * @return the pattern
     */
    public static String getterPattern(String fieldName) {

        String name = fieldName;
        if (name.length() > 0) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        return "get.*" + name + ".*";
    }

    /**
     * generate a pattern which the setter method should match 
     * @param fieldName
     * @return the pattern
     */
    public static String setterPattern(String fieldName) {
        String name = fieldName;
        if (name.length() > 0) {
            name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        return "set.*" + name + ".*";
    }

}
