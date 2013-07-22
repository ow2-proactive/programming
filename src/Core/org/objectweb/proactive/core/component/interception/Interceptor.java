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

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.control.PAInterceptorController;
import org.objectweb.proactive.core.mop.MethodCall;


/**
 * This interface must be implemented by controllers that need to intercept
 * incoming or outgoing functional invocations of a specific client or server
 * functional interface. <br>
 * Before executing (In the case of a primitive component) or transferring (In
 * the case of a composite component) an incoming or outgoing functional
 * request, the <code>beforeMethodInvocation</code> method is called, and the
 * <code>afterMethodInvocation</code> is called after the execution or
 * transfer of the invocation.<br>
 * These methods are executed on the controllers of the current component that
 * implement this interface.<br>
 * The <code>beforeMethodInvocation</code> method is called sequentially
 * for each controller in the order they have been set in the
 * {@link PAInterceptorController}.<br>
 * The <code>afterMethodInvocation</code> method is called sequentially
 * for each controller in the <b>reverse order</b> they have been set in the
 * {@link PAInterceptorController}.<br>
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface Interceptor {
    /**
     * This method is executed when an invocation is intercepted, before
     * executing the invocation.
     * 
     * @param interfaceName The name of the functional interface on which the
     *          invocation has been intercepted
     * @param methodCall The method that will be executed (MethodCall objects
     *          include method parameters and other ProActive-specific infos)
     */
    public void beforeMethodInvocation(String interfaceName, MethodCall methodCall);

    /**
     * This method is executed when an invocation has been intercepted, after
     * the execution of the invocation.
     * 
     * @param interfaceName The name of the functional interface on which the
     *          invocation has been intercepted
     * @param methodCall The method that has been executed (MethodCall objects
     *          include method parameters and other ProActive-specific infos)
     * @param result The result of the method that has been executed
     */
    public void afterMethodInvocation(String interfaceName, MethodCall methodCall, Object result);
}
