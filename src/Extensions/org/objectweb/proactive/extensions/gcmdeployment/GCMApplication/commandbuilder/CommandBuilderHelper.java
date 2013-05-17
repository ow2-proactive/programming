/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * CommandBuilderHelper
 *
 * @author The ProActive Team
 */
public class CommandBuilderHelper {
    /**
     * Given an argument of the form "-option1 AAAAA -option2 -option3 "BBBB" -option4="CCCC" ...", separate each individual argument and return them as a list,
     * if there are double quotes around a value argument they will be removed
     * @param arg
     * @return
     */
    public static List<String> parseArg(String arg) {
        ArrayList<String> answer = new ArrayList<String>();
        if (arg != null) {
            arg = arg.trim();

            StringBuilder remaining = new StringBuilder(arg);
            while (remaining.length() > 0) {
                if (remaining.charAt(0) == '-') {
                    answer.add(readOptionToken(remaining));
                } else {
                    answer.add(readValueToken(remaining));
                }
                eatAllLeadingSpaces(remaining);
            }
        }
        return answer;
    }

    private static String readOptionToken(StringBuilder remaining) {
        StringBuilder token = new StringBuilder();
        while (remaining.length() > 0 && remaining.charAt(0) != ' ' && remaining.charAt(0) != '\t') {
            if (remaining.charAt(0) == '"' || remaining.charAt(0) == '\'') {
                // sometimes the option can contain a quoted value such as -option="XXXX", in that case we eat everything
                // inside the quotes, but we keep the quotes
                eatAllBetweenQuotes(remaining, token, false);
                continue;
            }
            token.append(remaining.charAt(0));
            remaining.deleteCharAt(0);
        }
        return token.toString();
    }

    private static String readValueToken(StringBuilder remaining) {
        StringBuilder token = new StringBuilder();
        if (remaining.charAt(0) == '"' || remaining.charAt(0) == '\'') {
            // if the token is wrapped with double quotes or quotes, we simply take everything between the quotes, without
            // the quotes themselves
            eatAllBetweenQuotes(remaining, token, false);
            return token.toString();
        } else {
            // otherwise we wait until we find the " -" pattern using lookahead
            while (remaining.length() >= 2) {
                String pattern = "" + remaining.charAt(0) + "" + remaining.charAt(1);
                if (pattern.equals(" -")) {
                    return token.toString();
                }
                token.append(remaining.charAt(0));
                remaining.deleteCharAt(0);
            }
            // if we exit this loop without finding the pattern, we need to treat the last character
            if (remaining.charAt(0) != ' ' && remaining.charAt(0) != '\t') {
                token.append(remaining.charAt(0));
                remaining.deleteCharAt(0);
            }
            return token.toString();
        }
    }

    private static void eatAllBetweenQuotes(StringBuilder remaining, StringBuilder token, boolean keepQuotes) {
        char firstChar = remaining.charAt(0);
        if (keepQuotes) {
            token.append(remaining.charAt(0));
        }
        remaining.deleteCharAt(0);
        while (remaining.length() > 0 && remaining.charAt(0) != firstChar) {
            token.append(remaining.charAt(0));
            remaining.deleteCharAt(0);
        }
        if (keepQuotes) {
            token.append(remaining.charAt(0));
        }
        remaining.deleteCharAt(0);
    }

    private static void eatAllLeadingSpaces(StringBuilder remaining) {
        while (remaining.length() > 0 && (remaining.charAt(0) == ' ' || remaining.charAt(0) == '\t')) {
            remaining.deleteCharAt(0);
        }
    }
}
