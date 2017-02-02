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
package org.objectweb.proactive.core.body.future;

import java.io.Serializable;

import org.objectweb.proactive.core.exceptions.ExceptionHandler;


class ThisIsNotAnException extends Exception {
    public ThisIsNotAnException() {
        super("This is the call in the proxy");
    }
}

/**
 * This class is a placeholder for the result of a method call,
 * it can be an Object or a thrown Exception.
 */
public class MethodCallResult implements Serializable {

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
