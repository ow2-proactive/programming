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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.mop.MethodCall;

import com.google.common.collect.Lists;


/**
 * An implementation of java.util.concurrent.Future wrapping a MethodCallResult.
 * Passed as parameter to the user defined callback on future update.
 */
class ProActiveFuture implements java.util.concurrent.Future<Object> {
    private MethodCallResult result;

    public ProActiveFuture(MethodCallResult result) {
        this.result = result;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new IllegalStateException("Cannot cancel an already arrived ProActive future");
    }

    public Object get() throws ExecutionException {
        try {
            return this.result.getResult();
        } catch (Throwable t) {
            throw new ExecutionException(t);
        }
    }

    public Object get(long timeout, TimeUnit unit) throws ExecutionException {
        return get();
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return true;
    }
}

/**
 * A callback is method declared as 'void myCallback(java.util.concurrent.Future fr, ...)'
 * It is added using addFutureCallback(myFuture, "myCallback", ...), and will be
 * queued when the future is updated on ProActive.getBodyOnThis()
 * Callbacks are local, so are not copied when a future is serialized.
 */
public class LocalFutureUpdateCallbacks {
    private BodyImpl body;

    private Collection<MethodAndArguments> methods;

    private FutureProxy future;

    LocalFutureUpdateCallbacks(FutureProxy future) {
        try {
            this.body = (BodyImpl) PAActiveObject.getBodyOnThis();
        } catch (ClassCastException e) {
            throw new IllegalStateException("Can only be called in a body");
        }
        this.methods = new LinkedList<MethodAndArguments>();
        this.future = future;
    }

    void add(String methodName, Object... arguments) throws NoSuchMethodException {
        if (PAActiveObject.getBodyOnThis() != this.body) {
            throw new IllegalStateException("Callbacks added by different " +
                                            "bodies on the same future, this cannot be possible" +
                                            "without breaking the no-sharing property");
        }
        Object target = this.body.getReifiedObject();
        Class<?> c = target.getClass();

        Class[] argumentTypes = new Class[1 + arguments.length];
        argumentTypes[0] = java.util.concurrent.Future.class;
        for (int i = 0; i < arguments.length; i++) {
            argumentTypes[i + 1] = arguments[i].getClass();
        }
        Method m = c.getMethod(methodName, argumentTypes);
        this.methods.add(new MethodAndArguments(m, arguments));
    }

    void run() {

        for (MethodAndArguments m : this.methods) {
            List<Object> updatedArguments = m.getArguments();

            updatedArguments.add(0, new ProActiveFuture(this.future.getMethodCallResult()));
            MethodCall mc = MethodCall.getMethodCall(m.getMethod(), updatedArguments.toArray(), null);

            try {
                this.body.sendRequest(mc, null, this.body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class MethodAndArguments {

        final private Method method;

        final private Object[] arguments;

        public MethodAndArguments(Method method, Object[] arguments) {
            this.method = method;
            this.arguments = arguments;
        }

        public Method getMethod() {
            return method;
        }

        public List<Object> getArguments() {
            return Lists.newArrayList(arguments);
        }
    }
}
