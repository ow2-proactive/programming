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

import java.lang.reflect.Method;
import java.util.Collection;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.MethodCallExceptionContext;


public class ExceptionHandler {

    /* Called by the user */
    public static void tryWithCatch(Class<?>[] exceptions) {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForIntersection(exceptions);
            stack.push(exceptions);
        }
    }

    public static void throwArrivedException() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.throwArrivedException();
        }
    }

    public static void waitForPotentialException() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForPotentialException(true);
        }
    }

    public static void endTryWithCatch() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.waitForPotentialException(false);
        }
    }

    public static void removeTryWithCatch() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            stack.pop();
        }
    }

    public static Collection<Throwable> getAllExceptions() {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            return stack.getAllExceptions();
        }
    }

    /* Called by ProActive on the client side */
    public static void addRequest(MethodCall methodCall, FutureProxy future) {
        MethodCallExceptionContext context = methodCall.getExceptionContext();
        if (context.isExceptionAsynchronously() || context.isRuntimeExceptionHandled()) {
            ExceptionMaskStack stack = ExceptionMaskStack.get();
            synchronized (stack) {
                Method m = methodCall.getReifiedMethod();
                ExceptionMaskLevel level = stack.findBestLevel(m.getExceptionTypes());
                level.addFuture(future);
            }
        }
    }

    public static void addResult(FutureProxy future) {
        ExceptionMaskLevel level = future.getExceptionLevel();
        if (level != null) {
            level.removeFuture(future);
        }
    }

    public static MethodCallExceptionContext getContextForCall(Method m) {
        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            boolean runtime = stack.isRuntimeExceptionHandled();
            boolean async = stack.areExceptionTypesCaught(m.getExceptionTypes());
            MethodCallExceptionContext res = new MethodCallExceptionContext(runtime, async);

            //            System.out.println(m + " => " + res);
            return res;
        }
    }

    public static void throwException(Throwable exception) {
        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        }

        if (exception instanceof Error) {
            throw (Error) exception;
        }

        ExceptionMaskStack stack = ExceptionMaskStack.get();
        synchronized (stack) {
            if (!stack.isExceptionTypeCaught(exception.getClass())) {
                RuntimeException re = new ProActiveRuntimeException();
                re.initCause(exception);
                throw re;
            }

            ExceptionThrower.throwException(exception);
        }
    }
}
