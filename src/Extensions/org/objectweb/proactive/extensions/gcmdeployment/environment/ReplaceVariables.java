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
package org.objectweb.proactive.extensions.gcmdeployment.environment;

import java.util.StringTokenizer;


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

        StringTokenizer nametk = new StringTokenizer(nameListStr, sep);
        StringTokenizer valutk = new StringTokenizer(valueListStr, sep);
        while (nametk.hasMoreTokens()) {
            String name = nametk.nextToken();
            String value = valutk.nextToken();

            nameList.add(name);
            valueList.add(value);
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
