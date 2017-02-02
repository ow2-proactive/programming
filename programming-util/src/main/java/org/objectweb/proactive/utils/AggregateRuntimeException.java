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
 * AggregateRuntimeException is the RuntimeException version of AggregateException
 *
 * @author The ProActive Team
 **/
public class AggregateRuntimeException extends RuntimeException {

    List<Throwable> causes = new ArrayList<Throwable>();

    private static final String CAUSE_CAPTION = "Caused by: ";

    public AggregateRuntimeException() {
        super();
    }

    public AggregateRuntimeException(List<Throwable> causes) {
        super();
        this.causes = causes;
    }

    public AggregateRuntimeException(String message) {
        super(message);
    }

    public AggregateRuntimeException(String message, List<Throwable> causes) {
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
        printStackTrace(new AggregateException.WrappedPrintStream(s));
    }

    /**
     * prints the AggregateException stack and as well the list of all causes
     * @param s output
     */
    private void printStackTrace(AggregateException.PrintStreamOrWriter s) {
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
            s.println("");
            AggregateException.printTheCauses(s, causes);
        }
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        printStackTrace(new AggregateException.WrappedPrintWriter(s));
    }

}
