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
package org.objectweb.proactive.core.component.interception;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * Container of request information used by an {@link Interceptor}.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class InterceptedRequest implements Serializable {
    private final String interfaceName;
    private final String methodName;
    private final Class<?>[] parameterTypes;
    private final Class<?> returnType;
    private Object[] parameters;
    private Object result;

    /**
     * Creates an {@link InterceptedRequest}.
     * 
     * @param interfaceName The name of the interface on which the request has been invoked.
     * @param methodCall The {@link MethodCall} instance related to the invocation of the request.
     */
    public InterceptedRequest(String interfaceName, MethodCall methodCall) {
        this(interfaceName, methodCall, null);
    }

    /**
     * Creates an {@link InterceptedRequest}.
     * 
     * @param interfaceName The name of the interface on which the request has been invoked.
     * @param methodCall The {@link MethodCall} instance related to the invocation of the request.
     * @param result The result Object of the invocation which may be null if the request has not
     *              been invoked yet or if the request has no return type.
     */
    public InterceptedRequest(String interfaceName, MethodCall methodCall, Object result) {
        this.interfaceName = interfaceName;
        this.methodName = methodCall.getName();
        this.parameterTypes = methodCall.getReifiedMethod().getParameterTypes();
        this.returnType = methodCall.getReifiedMethod().getReturnType();
        this.parameters = methodCall.getEffectiveArguments();
        this.result = result;
    }

    /**
     * Returns the name of the interface on which the request has been invoked.
     * 
     * @return The name of the interface on which the request has been invoked.
     */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * Returns the name of the method on which the request has been invoked.
     * 
     * @return The name of the method on which the request has been invoked.
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Returns the parameter types of the method on which the request has been invoked.
     * 
     * @return The parameter types of the method on which the request has been invoked.
     */
    public Class<?>[] getParameterTypes() {
        return this.parameterTypes;
    }

    /**
     * Returns the return type of the method on which the request has been invoked.
     * 
     * @return The return type of the method on which the request has been invoked.
     */
    public Class<?> getReturnType() {
        return this.returnType;
    }

    /**
     * Returns the value of the parameter located at the given index to use to execute
     * the method on which the request has been invoked.
     * 
     * @param i The index of the parameter to return.
     * @return The value of the parameter located at the given index to use to execute
     *          the method on which the request has been invoked.
     * @throws InterceptedRequestException If the given index is invalid.
     */
    public Object getParameter(int i) throws InterceptedRequestException {
        if ((i > -1) && (i < this.parameters.length)) {
            return this.parameters[i];
        } else {
            throw new InterceptedRequestException("Invalid index of parameter: size " +
                this.parameterTypes.length + ", index received " + i);
        }
    }

    /**
     * Returns a clone of the parameter values to use to execute the method on which the request
     * has been invoked.
     * 
     * @return A clone of the parameter values to use to execute the method on which the request
     *          has been invoked.
     */
    public Object[] getParameters() {
        return this.parameters.clone();
    }

    /**
     * Sets the value of the parameter located at the given index to use to execute the method on
     * which the request has been invoked.
     * 
     * @param parameter The new value of the parameter located at the given index to use to execute
     *          the method on which the request has been invoked.
     * @param i The index of the parameter to set.
     * @throws InterceptedRequestException If the given index is invalid or if the type of the given
     *          parameter is not correct.
     */
    public void setParameter(Object parameter, int i) throws InterceptedRequestException {
        if ((i > -1) && (i < this.parameters.length)) {
            if (!this.parameterTypes[i].isAssignableFrom(parameter.getClass())) {
                throw new InterceptedRequestException("Wrong parameter type for parameter number " + i +
                    ": expected " + this.parameterTypes[i].getName() + ", received " + parameter.getClass());
            }

            this.parameters[i] = parameter;
        } else {
            throw new InterceptedRequestException("Invalid index of parameter: size " +
                this.parameterTypes.length + ", index received " + i);
        }
    }

    /**
     * Sets the parameter values to use to execute the method on which the request has been
     * invoked.
     * 
     * @param parameters The new parameter values to use to execute the method on which the
     *          request has been invoked.
     * @throws InterceptedRequestException If the number of the given parameters is not correct
     *          or if one of their type is not correct.
     */
    public void setParameters(Object[] parameters) throws InterceptedRequestException {
        if (parameters.length != this.parameterTypes.length) {
            throw new InterceptedRequestException("Wrong number of parameters: expected " +
                this.parameterTypes.length + " parameters, received " + parameters.length + " parameters");
        }

        for (int i = 0; i < this.parameterTypes.length; i++) {
            setParameter(parameters[i], i);
        }
    }

    /**
     * Returns the result Object of the execution of the method on which the request has
     * been invoked. The result may be null if the method has not been executed yet or if
     * the method has no return type.
     * 
     * @return The result Object of the execution of the method on which the request has
     *          been invoked.
     */
    public Object getResult() {
        return this.result;
    }

    /**
     * Sets the result Object of the execution of the method on which the request has
     * been invoked.
     * 
     * @param result The new result Object of the execution of the method on which the
     *          request has been invoked.
     * @throws InterceptedRequestException If the type of the given result Object is not
     *          correct.
     */
    public void setResult(Object result) throws InterceptedRequestException {
        if (!this.returnType.isAssignableFrom(result.getClass())) {
            throw new InterceptedRequestException("Wrong return type: expected " + this.returnType.getName() +
                ", received " + result.getClass());
        }

        this.result = result;
    }
}
