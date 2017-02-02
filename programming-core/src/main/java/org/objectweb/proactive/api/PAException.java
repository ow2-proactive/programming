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
package org.objectweb.proactive.api;

import java.util.Collection;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.future.Future;
import org.objectweb.proactive.core.exceptions.ExceptionHandler;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides a mechanism to handle exceptions in asynchronous calls.
 * It also provide static methods that test if a future contains or not an exception.
 * @see org.objectweb.proactive.api.PAFuture
 * @author The ProActive Team
 * @since ProActive 3.9 (December 2007)
 */
@PublicAPI
public class PAException {

    /*** <Exceptions> See ExceptionHandler.java for the documentation ***/
    /**
     * This has to be called just before a try block for a single exception.
     *
     * @param c the caught exception type in the catch block
     */
    public static void tryWithCatch(Class<?> c) {
        tryWithCatch(new Class<?>[] { c });
    }

    /**
     * This has to be called just before a try block for many exceptions.
     *
     * @param c the caught exception types in the catch block
     */
    public static void tryWithCatch(Class<?>[] c) {
        ExceptionHandler.tryWithCatch(c);
    }

    /**
     * This has to be called at the end of the try block.
     */
    public static void endTryWithCatch() {
        ExceptionHandler.endTryWithCatch();
    }

    /**
     * This has to be called at the beginning of the finally block, so
     * it requires one.
     */
    public static void removeTryWithCatch() {
        ExceptionHandler.removeTryWithCatch();
    }

    /**
     * This can be used to query a potential returned exception, and
     * throw it if it exists.
     */
    public static void throwArrivedException() {
        ExceptionHandler.throwArrivedException();
    }

    /**
     * Get the exceptions that have been caught in the current
     * ProActive.tryWithCatch()/ProActive.removeTryWithCatch()
     * block. This waits for every call in this block to return.
     *
     * @return a collection of these exceptions
     */
    public static Collection<Throwable> getAllExceptions() {
        return ExceptionHandler.getAllExceptions();
    }

    /**
     * This is used to wait for the return of every call, so that we know
     * the execution can continue safely with no pending exception.
     */
    public static void waitForPotentialException() {
        ExceptionHandler.waitForPotentialException();
    }

    /**
     * Find out if the object contains an exception that should be thrown
     * @param future the future object that is examinated
     * @return true iff an exception should be thrown when accessing the object
     */
    public static boolean isException(Object future) {
        // If the object is not reified, it cannot be a future
        if ((MOP.isReifiedObject(future)) == false) {
            return false;
        } else {
            org.objectweb.proactive.core.mop.Proxy theProxy = ((StubObject) future).getProxy();

            // If it is reified but its proxy is not of type future it's not an exception
            if (!(theProxy instanceof Future)) {
                return false;
            } else {
                return ((Future) theProxy).getRaisedException() != null;
            }
        }
    }
}
