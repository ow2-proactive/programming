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
