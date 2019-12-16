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
package org.objectweb.proactive.core.exceptions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ExceptionMaskLevel {

    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);

    /* Exception types in the catch blocks */
    private Collection<Class<?>> caughtExceptionTypes;

    /* Actual caught exceptions in this level */
    private Collection<Throwable> caughtExceptions;

    /* Pending futures */
    private int nbFutures;

    /* The stack this level belongs to */
    private ExceptionMaskStack parent;

    /* Do we catch a subtype of RuntimeException */
    private boolean catchRuntimeException;

    /* Do we catch a Non Functional Exception */
    ExceptionMaskLevel(ExceptionMaskStack parent, Class<?>[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            if (!Throwable.class.isAssignableFrom(exceptions[i])) {
                throw new IllegalArgumentException("Only exceptions can be catched");
            }

            catchRuntimeException = catchRuntimeException || RuntimeException.class.isAssignableFrom(exceptions[i]) ||
                                    exceptions[i].isAssignableFrom(RuntimeException.class);
        }

        if (exceptions.length < 1) {
            throw new IllegalArgumentException("At least one exception must be catched");
        }

        caughtExceptionTypes = Arrays.asList(exceptions);
        caughtExceptions = new LinkedList<Throwable>();
        nbFutures = 0;
        this.parent = parent;
    }

    /* Empty constructor for ExceptionHandler */
    ExceptionMaskLevel() {
        caughtExceptionTypes = new LinkedList<Class<?>>();
    }

    boolean isExceptionTypeCaught(Class<?> c) {
        Iterator<Class<?>> iter = caughtExceptionTypes.iterator();
        while (iter.hasNext()) {
            Class<?> cc = iter.next();
            if (cc.isAssignableFrom(c) || c.isAssignableFrom(cc)) {
                return true;
            }
        }

        return false;
    }

    /* We do an OR */
    boolean areExceptionTypesCaught(Class<?>[] exceptions) {
        if (caughtExceptionTypes.isEmpty()) {
            return false;
        }

        for (int i = 0; i < exceptions.length; i++) {
            if (isExceptionTypeCaught(exceptions[i])) {
                return true;
            }
        }

        return false;
    }

    void addExceptionTypes(ExceptionMaskLevel level) {
        Iterator<Class<?>> iter = level.caughtExceptionTypes.iterator();
        while (iter.hasNext()) {
            Class<?> c = iter.next();
            if (!isExceptionTypeCaught(c)) {
                caughtExceptionTypes.add(c);
            }
        }

        catchRuntimeException = catchRuntimeException || level.catchRuntimeException();
    }

    synchronized void waitForPotentialException() {
        parent.throwArrivedException();
        while (nbFutures != 0) {
            try {
                wait();
            } catch (InterruptedException ie) {
                logger.error("", ie);
                break;
            }
            parent.throwArrivedException();
        }
    }

    boolean catchRuntimeException() {
        return catchRuntimeException;
    }

    /* A call is launched */
    synchronized void addFuture(FutureProxy f) {
        if (f != null) {
            f.setExceptionLevel(this);
            nbFutures++;
        }
    }

    /* A future has returned */
    synchronized void removeFuture(FutureProxy f) {
        nbFutures--;
        MethodCallResult res = f.getMethodCallResult();

        if (res != null) {
            Throwable exception = f.getMethodCallResult().getException();
            if (exception != null) {
                synchronized (caughtExceptions) {
                    caughtExceptions.add(exception);
                }
            }
        }

        notifyAll();
    }

    Collection<Throwable> getCaughtExceptions() {
        return caughtExceptions;
    }

    synchronized Collection<Throwable> getAllExceptions() {
        while (nbFutures != 0) {
            try {
                wait();
            } catch (InterruptedException ie) {
                logger.error("", ie);
                break;
            }
        }
        return caughtExceptions;
    }
}
