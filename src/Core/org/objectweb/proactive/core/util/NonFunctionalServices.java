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
package org.objectweb.proactive.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;


/**
 *  <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 *  <p>
 *  This class is a way to add non functionnal services to managed active objects.
 *
 * The methods are reifed in the active object's Stub, so that their implementation is transparent for the programmer.
 *
 */
public class NonFunctionalServices {
    public static Class<?> nonFunctionalServicesClass = null;
    public static Method terminateAOMethod = null;
    public static Method terminateAOImmediatelyMethod = null;
    public static Class<?>[] paramTypes;

    static {
        try {
            nonFunctionalServicesClass = java.lang.Class
                    .forName("org.objectweb.proactive.core.util.NonFunctionalServices");
            paramTypes = new Class<?>[1];
            paramTypes[0] = java.lang.Class.forName("org.objectweb.proactive.core.mop.Proxy");
            terminateAOMethod = nonFunctionalServicesClass.getMethod("_terminateAO", paramTypes);
            terminateAOImmediatelyMethod = nonFunctionalServicesClass.getMethod("_terminateAOImmediately",
                    paramTypes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException es) {
            es.printStackTrace();
        } catch (NoSuchMethodException en) {
            en.printStackTrace();
        }
    }

    /**
     * Reify the "_terminateAO" method.
     * @param proxy
     * @throws Throwable
     */
    public static void terminateAO(Proxy proxy) throws Throwable {
        proxy.reify(MethodCall.getMethodCall(terminateAOMethod, paramTypes,
                (Map<TypeVariable<?>, Class<?>>) null));
    }

    /**
     * Reify the "_terminateAOImmediately" method.
     * A call on this method is an immediateService.
     * @param proxy
     * @throws Throwable
     */
    public static void terminateAOImmediately(Proxy proxy) throws Throwable {
        proxy.reify(MethodCall.getMethodCall(terminateAOImmediatelyMethod, paramTypes,
                (Map<TypeVariable<?>, Class<?>>) null));
    }

    /**
     * This method is reified by terminateAO(Proxy proxy).
     * The _terminateAO request is then intercepted by BodyImpl.serve() which calls AbstractBody.terminate().
     * @param proxy
     */
    public void _terminateAO(Proxy proxy) {
    }

    /**
     * This method is reified by terminateAOImmediately(Proxy proxy).
     * The _terminateAOImmediately request is turn into an immediate service,
     * then intercepted by BodyImpl.serve() which calls AbstractBody.terminate().
     * @param proxy
     */
    public void _terminateAOImmediately(Proxy proxy) {
    }
}
