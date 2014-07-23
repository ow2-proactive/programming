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
package org.objectweb.proactive.utils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.apache.commons.lang3.mutable.MutableBoolean;


/**
 * PrefixPrintStream this PrintStream adds a prefix to every line printed.
 * PrefixPrintStreams can be chained to have for example several levels of indentation
 * Please not that this class should NOT be used when performance is important, as there it generates a big overhead.
 *
 * @author The ProActive Team
 **/
public class PrefixPrintStream extends PrintStream {

    String prefix;

    LineBeginBoolean lineBegin = new LineBeginBoolean();

    boolean isPrintln = false;

    String lineSeparator = System.getProperty("line.separator");

    public PrefixPrintStream(OutputStream out, String prefix) {
        super(out, true);
        this.prefix = prefix;
    }

    @Override
    public void println() {
        super.println();
        lineBegin.reset();
    }

    @Override
    public void println(boolean x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(char x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(int x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(long x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(float x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(double x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(char[] x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(String x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void println(Object x) {
        super.println(x);
        lineBegin.reset();
    }

    @Override
    public void flush() {
        synchronized (this) {
            try {
                ensureOpen();
                out.flush();
            } catch (IOException x) {
                setError();
            }
            boolean isNewLine = false;
            for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
                if (elem.getClassName().equals(PrintStream.class.getName()) &&
                    elem.getMethodName().equals("newLine")) {
                    isNewLine = true;
                }
            }
            if (isNewLine) {
                lineBegin.reset();
            }
        }

    }

    @Override
    public void write(int c) {
        maybePrintPrefix();
        super.write(c);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        maybePrintPrefix();
        super.write(buf, off, len);
    }

    @Override
    public void write(byte[] buf) throws IOException {
        maybePrintPrefix();
        super.write(buf);
    }

    public void write(String s) throws IOException {
        maybePrintPrefix();
        write(s.getBytes());
        if (s.contains(lineSeparator)) {
            lineBegin.reset();
        }
    }

    @Override
    public PrintStream printf(String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintStream printf(Locale l, String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintStream format(String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintStream format(Locale l, String format, Object... args) {
        throw new UnsupportedOperationException();
    }

    /** Checks to make sure that the stream has not been closed */
    private void ensureOpen() throws IOException {
        if (out == null)
            throw new IOException("Stream closed");
    }

    private void maybePrintPrefix() {
        if (lineBegin.booleanValue()) {
            try {
                synchronized (this) {
                    ensureOpen();
                    out.write(prefix.getBytes());
                }
            } catch (InterruptedIOException x) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                setError();
            }
            lineBegin.update();
        }
    }

    private class LineBeginBoolean extends MutableBoolean {

        public LineBeginBoolean() {
            super(true);
        }

        public void update() {
            setValue(false);
        }

        public void reset() {
            setValue(true);
        }
    }
}
