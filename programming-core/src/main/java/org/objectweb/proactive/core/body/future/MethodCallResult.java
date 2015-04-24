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
package org.objectweb.proactive.core.body.future;

import java.io.Serializable;

import org.objectweb.proactive.core.exceptions.ExceptionHandler;


class ThisIsNotAnException extends Exception {

    private static final long serialVersionUID = 62L;
    public ThisIsNotAnException() {
        super("This is the call in the proxy");
    }
}

/**
 * This class is a placeholder for the result of a method call,
 * it can be an Object or a thrown Exception.
 */
public class MethodCallResult implements Serializable {

    private static final long serialVersionUID = 62L;

    /** The object to be returned */
    private Object result;

    /** The exception to throw */
    private Throwable exception;

    /**
     * 
     * @param result the method call's result
     * @param exception the exception thrown during the method call execution
     */
    public MethodCallResult(Object result, Throwable exception) {
        this.result = result;
        this.exception = exception;
    }

    /**
     * returns the exception thrown during the method call's execution
     * or null if no exception has been raised.
     * @return returns the exception thrown during the method call's execution
     * or null if no exception has been raised.
     */
    public Throwable getException() {
        return exception;
    }

    /**
     * Return the result of the method call.
     * If an exception has been raised during the method call's execution then
     * the exception is thrown.
     * @return Return the result of the method call. If an exception has been 
     * raised during the method call's execution then the exception is thrown. 
     */
    public Object getResult() {
        if (exception != null) {
            ExceptionHandler.throwException(exception);
        }

        return result;
    }

    @Override
    public String toString() {
        String str = "[";
        if (exception != null) {
            str += ("ex:" + exception.getClass().getName());
        } else if (result != null) {
            str += result.getClass().getName();
        } else {
            str += "null";
        }

        return str + "]";
    }

    public void augmentException(StackTraceElement[] stackTrace) {
        Throwable cause = exception;
        if ((cause != null) && (stackTrace != null)) {
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            Exception origCause = new ThisIsNotAnException();
            origCause.setStackTrace(stackTrace);
            cause.initCause(origCause);
        }
    }

    /**
     * Provide access to the result object.
     * Override the behavior of getResult() by not throwing the exception if the method
     * call has thrown an exception.
     * @return the result -- can be null if an exception has been thrown.
     */
    public Object getResultObjet() {
        return this.result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

}
