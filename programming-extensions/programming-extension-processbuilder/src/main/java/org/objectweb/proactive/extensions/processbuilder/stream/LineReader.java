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
package org.objectweb.proactive.extensions.processbuilder.stream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * Class that will read lines of output from a ByteStepStreamReader.
 * It is used instead of InputStreamReader+{@link BufferedReader} because this way we
 * can be sure that no output remains in the buffer when we exit the method using the 
 * reader.
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
public class LineReader {
    private String NEWLINE = (System.getProperty("line.separator") != null) ? System.getProperty("line.separator")
                                                                            : "\n";

    private ByteStepStreamReader reader;

    private String buffer;

    public LineReader(InputStream is) {
        try {
            reader = ByteStepStreamReader.createDecoderFor(is, (String) null);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Block until a newline-terminated string is read from the stream.
     * @return The string read (without the newline)
     */
    public String readLine() {
        int ch;
        char c;

        buffer = "";

        while ((ch = reader.read()) != -1) {
            c = (char) ch;

            buffer += c;

            if (buffer.endsWith(NEWLINE)) {
                return buffer.replace(NEWLINE, "");
            }

        }
        return null;
    }

}
