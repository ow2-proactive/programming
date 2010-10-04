package org.objectweb.proactive.extensions.processbuilder.stream;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * Class that will read lines of output from a ByteStepStreamReader.
 * It is used instead of InputStreamReader+{@link BufferedReader} because this way we
 * can be sure that no output remains in the buffer when we exit the method using the 
 * reader.
 * @author Zsolt Istvan
 *
 */
public class LineReader {
    private String NEWLINE = (System.getProperty("line.separator") != null) ? System
            .getProperty("line.separator") : "\n";
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