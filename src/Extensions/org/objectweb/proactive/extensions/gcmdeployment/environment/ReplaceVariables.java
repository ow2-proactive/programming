/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.extensions.gcmdeployment.environment;

/**
 * ReplaceVariables
 *
 * @author The ProActive Team
 */
public class ReplaceVariables {

    static private java.util.ArrayList<String> nameList = new java.util.ArrayList<String>();
    static private java.util.ArrayList<String> valueList = new java.util.ArrayList<String>();

    public static String init(String nameListStr, String valueListStr) {
        nameList.clear();
        valueList.clear();

        String sep = "" + ((char) 5);

        String[] nameSplit = nameListStr.split(sep);
        String[] valueSplit = valueListStr.split(sep);

        if (nameSplit.length != valueSplit.length) {
            throw new IllegalStateException(
                "A bug occured during the XSLT variable replacement. The number of variables and values does not match: #variables=" +
                    nameSplit.length + " #values" + valueSplit.length);
        }

        for (int i = 0; i < nameSplit.length; i++) {
            nameList.add(nameSplit[i]);
            valueList.add(valueSplit[i]);
        }

        return "";
    }

    public static String replaceAll(String input) {
        String buffer = input;
        if (buffer.matches(".*\\$\\{[A-Za-z_0-9.]+\\}.*")) {
            for (int i = 0; i < nameList.size(); i++) {
                buffer = buffer.replaceAll("\\$\\{" + nameList.get(i) + "\\}", valueList.get(i));
            }
            if (buffer.matches(".*\\$\\{[A-Za-z_0-9.]+\\}.*")) {
                throw new IllegalArgumentException(
                    "Undefined variable or recursive definition in following expression'" + buffer + "'");
            }
        }
        return buffer;
    }

}
