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
package org.objectweb.proactive.extensions.gcmdeployment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class Helpers {

    /**
     * Checks that descriptor exist, is a file and is readable
     * 
     * @param descriptor
     *            The File to be checked
     * @throws IllegalArgumentException
     *             If the File is does not exist, is not a file or is not readable
     */
    public static URLConnection openConnectionTo(URL descriptor) throws IllegalArgumentException {
        URLConnection conn;
        try {
            conn = descriptor.openConnection();
        } catch (IOException e) {
            throw new IllegalArgumentException("Connection to " + descriptor.toExternalForm() +
                                               " could not be established.", e);
        }

        return conn;
    }

    public static URL fileToURL(File file) {
        if (file == null)
            return null;

        URL answer = null;
        try {
            answer = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return answer;
    }

    static public String escapeCommand(String command) {
        // At each step, the command must be protected with " or ' and the command
        // passed as parameter must be escaped. This can be quite difficult since
        // Runtime.getRuntime().exec() only take an array of String as parameter...    	
        String res = command.replaceAll("'", "'\\\\''");
        return "'" + res + "'";
    }

    static public String escapeWindowsCommand(String command) {
        String res = command.replaceAll("\"", "\\\"");
        return res;
    }

}
