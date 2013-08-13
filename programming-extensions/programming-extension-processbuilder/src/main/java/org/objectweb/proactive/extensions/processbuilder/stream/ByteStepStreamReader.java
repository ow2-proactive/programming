/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.processbuilder.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;


/**
 * This class is a replacement class for {@link InputStreamReader}.
 * It is actually a simplified version, that returns one character at a time.
 * <br><br>
 * Background story:<br>
 * {@link InputStreamReader} is a class that conveniently wraps an {@link InputStream} and
 * saves the developer from the trouble of decoding the input with a given character set.
 * The only issue is that it's underlying implementation will read from the input two bytes
 * at a time - even if the stream could contain characters on one byte. The main issue with
 * this is that in case someone opens an InputStreamReader, and reads from the stream, than 
 * opens an other reader, and continues reading, if the first reader was last decoding a character
 * which would not use up all its buffer, than the leftover byte(s) would go to waste.
 * <br>
 * This class ensures that bytes are read from the stream only to enable decoding the next character.
 * <br><br> 
 * Remark: Most modern programs will use UTF-8 encoding on output, which means that characters are
 * encoded on a variable number of bytes (one for plain English characters).
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
public class ByteStepStreamReader {
    //TODO guess the correct size :)
    private static final int BYTE_BUFFER_LENGTH = 64;

    private InputStream inputStream;
    private CharsetDecoder decoder;
    private ByteBuffer inputBuffer;

    /* CONSTRUCTORS */

    protected ByteStepStreamReader(InputStream is, Charset cs) {
        this(is, cs.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(
                CodingErrorAction.REPLACE));
    }

    protected ByteStepStreamReader(InputStream is, CharsetDecoder cd) {
        this.inputStream = is;
        this.decoder = cd;
        byte buff[] = new byte[BYTE_BUFFER_LENGTH];
        this.inputBuffer = ByteBuffer.wrap(buff);
        this.inputBuffer.flip();
    }

    /* FACTORY METHODS */

    public static ByteStepStreamReader createDecoderFor(InputStream in, String charsetName)
            throws UnsupportedEncodingException {
        String csn = charsetName;
        if (csn == null)
            csn = Charset.defaultCharset().name();
        try {
            if (Charset.isSupported(csn))
                return new ByteStepStreamReader(in, Charset.forName(csn));
        } catch (IllegalCharsetNameException x) {
        }
        throw new UnsupportedEncodingException(csn);
    }

    public static ByteStepStreamReader createDecoderFor(InputStream in, Charset cs) {
        return new ByteStepStreamReader(in, cs);
    }

    public static ByteStepStreamReader createDecoderFor(InputStream in, CharsetDecoder dec) {
        return new ByteStepStreamReader(in, dec);
    }

    /* INSTANCE METHODS */

    /**
     * Read a single character.
     *
     * @return The character read, or -1 if the end of the stream has been
     *         reached
     */
    public int read() {
        char outbuf[] = new char[2];

        try {
            int success = decodeTo(outbuf);
            if (success == -1)
                return -1;
        } catch (CharacterCodingException e) {
            return -1;
        }

        return outbuf[0];
    }

    /**
     * Read a single bute from the input stream into the buffer.
     * The buffer is used to collect bytes until the decoder is able
     * to decode them with a given character set.
     * @return
     */
    private int readByte() {
        inputBuffer.compact();
        int in = -1;

        try {
            in = inputStream.read();
            if (in != -1)
                inputBuffer.put((byte) in);
        } catch (IOException e) {
            // shhh...
        }

        inputBuffer.flip();

        return (in == -1) ? -1 : 0;
    }

    /**
     * This method will convert the bytes contained in the buffer into a character of
     * the used character set.<br>
     * The buffer will automatically fetch more bytes until the decoder is able to convert 
     * a character.
     * <br>
     * It is <u>guaranteed</u> that no bytes are read in advance from the input stream.
     * @param cbuf A buffer in which the character is written
     * @return -1 for failure
     * @throws CharacterCodingException
     */
    private int decodeTo(char cbuf[]) throws CharacterCodingException {
        CharBuffer output = CharBuffer.wrap(cbuf, 0, 2);

        if (inputBuffer.position() == 0)
            readByte();

        boolean finished = false;
        boolean needMore = true;
        // fetch new bytes until we can decode a character
        while (needMore && !finished) {
            //try to decode
            CoderResult res = decoder.decode(inputBuffer, output, finished);

            if (res.isUnderflow()) {
                if (finished || !output.hasRemaining() || output.position() > 0) {
                    needMore = false;
                } else {
                    //fetch a new byte
                    if (readByte() < 0) {
                        finished = true;
                    }
                }
            } else if (res.isOverflow()) {
                needMore = false;
            } else {
                // character set is not the one we believe it is?
                res.throwException();
            }

        }

        if (finished) {
            decoder.reset();
        }

        if (output.position() == 0 && finished) {
            return -1;
        }
        return output.position();
    }

}
