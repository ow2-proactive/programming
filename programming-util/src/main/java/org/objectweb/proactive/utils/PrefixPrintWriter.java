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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Locale;


/**
 * PrefixPrintWriter this PrintWriter adds a prefix to every line printed.
 * PrefixPrintWriters can be chained to have for example several levels of indentation
 *
 * @author The ProActive Team
 **/
public class PrefixPrintWriter extends PrintWriter {

    private final String prefix;

    private final LinePosition linePosition = new LinePosition();

    String lineSeparator = System.getProperty("line.separator");

    public PrefixPrintWriter(Writer out, String prefix) {
        super(out);
        this.prefix = prefix;
    }

    public PrefixPrintWriter(Writer out, String prefix, boolean autoFlush) {
        super(out, autoFlush);
        this.prefix = prefix;
    }

    public PrefixPrintWriter(OutputStream out, String prefix) {
        super(out);
        this.prefix = prefix;
    }

    public PrefixPrintWriter(OutputStream out, String prefix, boolean autoFlush) {
        super(out, autoFlush);
        this.prefix = prefix;
    }

    public PrefixPrintWriter(String fileName, String prefix) throws FileNotFoundException {
        super(fileName);
        this.prefix = prefix;
    }

    public PrefixPrintWriter(String fileName, String prefix, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
        this.prefix = prefix;
    }

    public PrefixPrintWriter(File file, String prefix) throws FileNotFoundException {
        super(file);
        this.prefix = prefix;
    }

    public PrefixPrintWriter(File file, String prefix, String csn)
            throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
        this.prefix = prefix;
    }

    @Override
    public void println() {
        super.println();
        linePosition.reset();
    }

    @Override
    public void println(boolean x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(char x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(int x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(long x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(float x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(double x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(char[] x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(String x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void println(Object x) {
        super.println(x);
        linePosition.reset();
    }

    @Override
    public void write(int c) {
        maybePrintPrefix();
        super.write(c);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        maybePrintPrefix();
        super.write(buf, off, len);
    }

    @Override
    public void write(char[] buf) {
        maybePrintPrefix();
        super.write(buf);
    }

    @Override
    public void write(String s, int off, int len) {
        maybePrintPrefix();
        super.write(s, off, len);
    }

    @Override
    public void write(String s) {
        maybePrintPrefix();
        super.write(s);
        if (s.contains(lineSeparator)) {
            linePosition.reset();
        }
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    /** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }

    private void maybePrintPrefix() {
        if (linePosition.atStart()) {
            try {
                synchronized (lock) {
                    ensureOpen();
                    out.write(prefix, 0, prefix.length());
                }
            } catch (InterruptedIOException x) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                setError();
            }
            linePosition.update();
        }
    }
}
