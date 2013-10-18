/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.vfsprovider.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;


/**
 * URIHelper
 *
 * @author The ProActive Team
 **/
public class URIHelper {

    /**
     * This method replace non-URI-valid characters with their replacement code counterpart
     * (issue PROACTIVE-1314)
     * @param input
     * @return a string without main illegal characters
     */
    public static String convertToEncodedURIString(String input) {
        StringBuilder answer = new StringBuilder();
        // the input must be first decoded as it may contain %XX characters and % is illegal
        try {
            input = URLDecoder.decode(input, "UTF8");
        } catch (UnsupportedEncodingException e) {
            // it should never happen
            e.printStackTrace();
            return null;
        }
        // chars which must be replaced
        char[] inputChars = new char[] { ' ', '\"', '%', '<', '>', '#', '[', '\\', ']', '^', '`', '{', '|',
                '}' };
        // the replacement code
        String[] replacements = new String[] { "%20", "%22", "%25", "%3C", "%3E", "%23", "%5B", "%5C", "%5D",
                "%5E", "%60", "%7B", "%7C", "%7D" };
        for (char c : input.toCharArray()) {
            int pos;
            if ((pos = Arrays.binarySearch(inputChars, c)) >= 0) {
                answer.append(replacements[pos]);
            } else {
                answer.append(c);
            }
        }
        return answer.toString();
    }

    public static String[] convertAllToURIString(String[] input) {
        if (input == null) {
            return null;
        }
        String[] output = new String[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = convertToEncodedURIString(input[i]);
        }
        return output;
    }

    public static boolean findFileUrl(String[] input) {
        for (String url : input) {
            try {
                URI uri = new URI(url);
                if ("file".equals(uri.getScheme())) {
                    return true;
                }
            } catch (URISyntaxException e) {
                // ignore
            }
        }
        return false;
    }
}
