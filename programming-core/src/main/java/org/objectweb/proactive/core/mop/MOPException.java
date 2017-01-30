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
package org.objectweb.proactive.core.mop;

/**
 */
public class MOPException extends Exception {
    public Throwable detail;

    /**
     * Constructs a <code>ProActiveException</code> with no specified
     * detail message.
     */
    public MOPException() {
    }

    /**
     * Constructs a <code>ProActiveException</code> with the specified detail message.
     * @param s the detail message
     */
    public MOPException(String s) {
        super(s);
    }

    /**
     * Constructs a <code>ProActiveException</code> with the specified
     * detail message and nested exception.
     *
     * @param s the detail message
     * @param ex the nested exception
     */
    public MOPException(String s, Throwable ex) {
        super(s);
        detail = ex;
    }

    /**
     * Constructs a <code>ProActiveException</code> with the specified
     * detail message and nested exception.
     *
     * @param ex the nested exception
     */
    public MOPException(Throwable ex) {
        super();
        detail = ex;
    }

    public Throwable getTargetException() {
        return detail;
    }

    /**
     * Returns the detail message, including the message from the nested
     * exception if there is one.
     */
    @Override
    public String getMessage() {
        if (detail == null) {
            return super.getMessage();
        } else {
            if (super.getMessage() == null) {
                return detail.getMessage();
            } else {
                return super.getMessage() + "; nested exception is: \n" + detail.toString();
            }
        }
    }

    /**
     * Prints the composite message and the embedded stack trace to
     * the specified stream <code>ps</code>.
     * @param ps the print stream
     */
    @Override
    public void printStackTrace(java.io.PrintStream ps) {
        if (detail == null) {
            super.printStackTrace(ps);
        } else {
            synchronized (ps) {
                ps.println(getMessage());
                detail.printStackTrace(ps);
            }
        }
    }

    /**
     * Prints the composite message to <code>System.err</code>.
     */
    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * Prints the composite message and the embedded stack trace to
     * the specified print writer <code>pw</code>.
     * @param pw the print writer
     */
    @Override
    public void printStackTrace(java.io.PrintWriter pw) {
        if (detail == null) {
            super.printStackTrace(pw);
        } else {
            synchronized (pw) {
                pw.println(getMessage());
                detail.printStackTrace(pw);
            }
        }
    }
}
