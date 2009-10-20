/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.webservices;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of the {@link ProActiveWSCaller} interface using the {@link http://ws.apache.org/axis2/ Axis2} API.
 *
 * @author The ProActive Team
 * @see ProActiveWSCaller
 */
@PublicAPI
public class Axis2WSCaller implements ProActiveWSCaller {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_REQUESTS);

    private RPCServiceClient rsc;

    public Axis2WSCaller() {
    }

    public void setup(Class<?> serviceClass, String wsUrl) {
        try {
            rsc = new RPCServiceClient();
            rsc.getOptions().setTo(new EndpointReference(wsUrl));
        } catch (AxisFault e) {
            logger.error("[Axis2] Cannot create RPCServiceClient with the given address: " + wsUrl);
        }
    }

    public Object callWS(String methodName, Object[] args, Class<?> returnType) {
        if (rsc != null) {
            try {
                rsc.getOptions().setAction(methodName);
                QName qName = new QName(methodName);
                if (returnType == null) {
                    rsc.invokeRobust(qName, args);
                    return null;
                } else {
                    return rsc.invokeBlocking(qName, args, new Class<?>[] { returnType })[0];
                }
            } catch (AxisFault af) {
                logger.error(
                        "[Axis2] Failed to invoke web service: " + rsc.getOptions().getTo().getAddress(), af);
                return null;
            }
        } else {
            logger
                    .error("[Axis2] Cannot invoke web service since the set up has not been done or has failed");
            return null;
        }
    }
}
