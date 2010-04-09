/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.webservices;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.dynamic.DynamicClientFactory;
import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of the {@link ProActiveWSCaller} interface using the <a href="http://cxf.apache.org/">CXF</a>
 * API with a dynamic client.
 *
 * @author The ProActive Team
 * @see ProActiveWSCaller
 */
@PublicAPI
public class DynamicCXFWSCaller implements ProActiveWSCaller {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUESTS);

    private Client client;

    public DynamicCXFWSCaller() {
    }

    public void setup(Class<?> serviceClass, String wsUrl) {
        DynamicClientFactory dcf = DynamicClientFactory.newInstance();
        client = dcf.createClient(wsUrl + "?wsdl");
    }

    public Object callWS(String methodName, Object[] args, Class<?> returnType) {
        if (client != null) {
            try {
                Object[] results = client.invoke(methodName, args);
                if (returnType == null) {
                    return null;
                } else {
                    return results[0];
                }
            } catch (Exception e) {
                logger.error("[DynamicCXFWSCaller] Failed to invoke web service: " +
                    client.getEndpoint().getEndpointInfo().getAddress(), e);
                return null;
            }
        } else {
            logger.error("[DynamicCXFWSCaller] Cannot invoke web service since the set up has not been done");
            return null;
        }
    }
}
