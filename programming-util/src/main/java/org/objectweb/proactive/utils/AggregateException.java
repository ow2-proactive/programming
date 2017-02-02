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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;


/**
 * AggregateException is used to create an aggregator of Exceptions. While by default Exceptions have a single cause,
 * there can be scenarios, where a failure can be initiated by several causes altogether.
 *
 * In that case, Java (before Java 1.7 with "suppressed Exceptions" ) does not provide a standard mechanism to throw a single Exception which describes all failures.
 * This class is meant to solve this need.
 *
 *
 * @author The ProActive Team
 **/
public class AggregateException extends Exception {

    List<Throwable> causes = new ArrayList<Throwable>();

    static final String CAUSE_CAPTION = "Caused by: ";

    public AggregateException() {
        super();
    }

    public AggregateException(List<Throwable> causes) {
        super();
        this.causes = causes;
    }

    public AggregateException(String message) {
        super(message);
    }

    public AggregateException(String message, List<Throwable> causes) {
        super(message);
        this.causes = causes;
    }

    /**
     * Adds a cause to the list of causes
     * @param cause cause to add
     */
    public void addCause(Throwable cause) {
        this.causes.add(cause);
    }

    /**
     * Returns the list of causes of this AggregateException
     * @return list of causes
     */
    public synchronized List<Throwable> getCauses() {
        return causes;
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        printStackTrace(new WrappedPrintStream(s));
    }

    /**
     * prints the AggregateException stack and as well the list of all causes
     * @param s output
     */
    private void printStackTrace(PrintStreamOrWriter s) {
        // Guard against malicious overrides of Throwable.equals by
        // using a Set with identity equality semantics.
        Set<Throwable> dejaVu = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        dejaVu.add(this);

        synchronized (s.lock()) {
            // Print our stack trace
            s.println(this);
            StackTraceElement[] trace = getStackTrace();
            for (StackTraceElement traceElement : trace)
                s.println("\tat " + traceElement);

            // Print causes, if any
            List<Throwable> ourCauses = getCauses();
            printTheCauses(s, causes);
        }
    }

    static void printTheCauses(PrintStreamOrWriter s, List<Throwable> causes) {

        s.println(CAUSE_CAPTION);
        int index = 1;
        for (Throwable cause : causes) {
            if (cause != null) {
                s.println("\t[" + index + "]:");
                if (s.getPrintStream() != null) {
                    cause.printStackTrace(new PrefixPrintStream(s.getPrintStream(), "\t"));
                } else {
                    cause.printStackTrace(new PrefixPrintWriter(s.getPrintWriter(), "\t"));
                }
                index++;
            }
        }
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        printStackTrace(new WrappedPrintWriter(s));
    }

    /**
     * Wrapper class for PrintStream and PrintWriter to enable a single
     * implementation of printStackTrace.
     */
    abstract static class PrintStreamOrWriter {
        /** Returns the object to be locked when using this StreamOrWriter */
        abstract Object lock();

        /** Prints the specified string as a line on this StreamOrWriter */
        abstract void println(Object o);

        abstract PrintStream getPrintStream();

        abstract PrintWriter getPrintWriter();
    }

    static class WrappedPrintStream extends PrintStreamOrWriter {
        private final PrintStream printStream;

        WrappedPrintStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        Object lock() {
            return printStream;
        }

        void println(Object o) {
            printStream.println(o);
        }

        @Override
        PrintStream getPrintStream() {
            return printStream;
        }

        @Override
        PrintWriter getPrintWriter() {
            return null;
        }
    }

    static class WrappedPrintWriter extends PrintStreamOrWriter {
        private final PrintWriter printWriter;

        WrappedPrintWriter(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }

        Object lock() {
            return printWriter;
        }

        void println(Object o) {
            printWriter.println(o);
        }

        @Override
        PrintStream getPrintStream() {
            return null;
        }

        @Override
        PrintWriter getPrintWriter() {
            return printWriter;
        }
    }
}
