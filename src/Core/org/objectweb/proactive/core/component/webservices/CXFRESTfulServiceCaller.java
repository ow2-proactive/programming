/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.core.component.webservices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.webservices.PAWSCaller;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of the {@link PAWSCaller} interface using the <a href="http://cxf.apache.org/">CXF</a>
 * API to call RESTful services. The service class must use annotations of the javax.ws.rs package.
 *
 * @author The ProActive Team
 * @see PAWSCaller
 */
@PublicAPI
public class CXFRESTfulServiceCaller implements PAWSCaller {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUESTS);

    private String restServiceUrl;
    private Object restService;

    public void setup(Class<?> serviceClass, String wsUrl) {
        this.restServiceUrl = wsUrl;
        this.restService = JAXRSClientFactory.create(wsUrl, serviceClass);
    }

    public Object callWS(String methodName, Object[] args, Class<?> returnType) {
        try {
            Class<?>[] parameterTypes = new Class<?>[args.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypes[i] = args[i].getClass();
            }
            Method method = restService.getClass().getDeclaredMethod(methodName, parameterTypes);
            return method.invoke(restService, args);
        } catch (SecurityException se) {
            logger.error("[CXF] Failed to invoke RESTful service: " + restServiceUrl, se);
        } catch (NoSuchMethodException nsme) {
            logger.error("[CXF] Failed to invoke RESTful service: " + restServiceUrl, nsme);
        } catch (IllegalArgumentException iae) {
            logger.error("[CXF] Failed to invoke RESTful service: " + restServiceUrl, iae);
        } catch (IllegalAccessException iae) {
            logger.error("[CXF] Failed to invoke RESTful service: " + restServiceUrl, iae);
        } catch (InvocationTargetException ite) {
            logger.error("[CXF] Failed to invoke RESTful service: " + restServiceUrl, ite);
        }
        return null;
    }
}