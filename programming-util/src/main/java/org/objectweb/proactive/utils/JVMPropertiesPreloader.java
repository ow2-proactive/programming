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
package org.objectweb.proactive.utils;

import java.util.ArrayList;
import java.util.List;


/**
 * JVMPropertiesPreloader is used to parse arguments and set JVM properties.
 * Use this class if your arguments line contains java properties (ie : -Dname=value)
 * This properties will be set to JVM (this cause the existing one to be overridden.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0 (removed 3.2.0)
 * @since ProActive Programming 5.2.0
 */
public final class JVMPropertiesPreloader {

    /**
     * Get the argument line, parse it and override JVM properties with the specified one.
     * This method returns a copy of the args line without the JVM properties notation arguments.
     *
     * @param args the arguments line to be parsed (containing JVM properties notation arguments)
     */
    public static String[] overrideJVMProperties(String[] args) {
        List<String> argsToReturn = new ArrayList<String>();
        for (String arg : args) {
            if (arg.matches("^-D.+=.+$")) {
                setPropertyWithValue(arg);
            } else if (arg.matches("^-D.+$")) {
                setEmptyProperty(arg);
            } else {
                argsToReturn.add(arg);
            }
        }
        return argsToReturn.toArray(new String[] {});
    }

    /**
     * Set the parsed argument and value to the JVM properties.
     *
     * @param argument the argument to add (must be -Dname=value)
     */
    private static void setPropertyWithValue(String argument) {
        String[] split = argument.substring(2).split("=");
        System.setProperty(split[0], split[1]);
    }

    /**
     * Set the parsed argument with an empty string value to the JVM properties.
     *
     * @param argument the argument to add (must be -Dname)
     */
    private static void setEmptyProperty(String argument) {
        System.setProperty(argument.substring(2), "");
    }

}
