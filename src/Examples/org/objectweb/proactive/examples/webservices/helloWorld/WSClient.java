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
package org.objectweb.proactive.examples.webservices.helloWorld;

import javax.xml.namespace.QName;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.objectweb.proactive.extensions.webservices.WSConstants;


/**
 * An example to call the hello world web service.
 *
 * @author The ProActive Team
 */
public class WSClient {
    public static void main(String[] args) {

        try {

			RPCServiceClient serviceClient = new RPCServiceClient();

			Options options = serviceClient.getOptions();

			EndpointReference targetEPR = new EndpointReference(
					"http://localhost:8080/" + WSConstants.AXIS_SERVICES_PATH + "HelloWorld");
			options.setTo(targetEPR);
			options.setAction("helloWorld");

			QName op = new QName("helloWorld");

			Object[] opArgs = new Object[] {};
			Class<?>[] returnTypes = new Class[] { String.class };

			Object[] response = serviceClient.invokeBlocking(op,
				opArgs, returnTypes);

			Object result = response[0];

			System.out.println("Client returned " + (String) result);

        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }
}
