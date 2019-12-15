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
package org.objectweb.proactive.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *  <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 *  <p>
 *  This class is a way to add non functionnal services to managed active objects.
 *
 * The methods are reifed in the active object's Stub, so that their implementation is transparent for the programmer.
 *
 */
public class NonFunctionalServices {
    static Logger logger = ProActiveLogger.getLogger(Loggers.UTIL);

    static Class<?> nonFunctionalServicesClass = null;

    static Method terminateAOMethod = null;

    static Method terminateAOImmediatelyMethod = null;

    static Class<?>[] paramTypes;

    static {
        try {
            nonFunctionalServicesClass = java.lang.Class.forName(NonFunctionalServices.class.getName());
            paramTypes = new Class<?>[1];
            paramTypes[0] = java.lang.Class.forName(Proxy.class.getName());
            terminateAOMethod = nonFunctionalServicesClass.getMethod("_terminateAO", paramTypes);
            terminateAOImmediatelyMethod = nonFunctionalServicesClass.getMethod("_terminateAOImmediately", paramTypes);
        } catch (ClassNotFoundException | SecurityException | NoSuchMethodException e) {
            logger.error("", e);
        }
    }

    /**
     * Reify the "_terminateAO" method.
     * @param proxy
     * @throws Throwable
     */
    public static void terminateAO(Proxy proxy) throws Throwable {
        proxy.reify(MethodCall.getMethodCall(terminateAOMethod, paramTypes, (Map<TypeVariable<?>, Class<?>>) null));
    }

    /**
     * Reify the "_terminateAOImmediately" method.
     * A call on this method is an immediateService.
     * @param proxy
     * @throws Throwable
     */
    public static void terminateAOImmediately(Proxy proxy) throws Throwable {
        proxy.reify(MethodCall.getMethodCall(terminateAOImmediatelyMethod,
                                             paramTypes,
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
