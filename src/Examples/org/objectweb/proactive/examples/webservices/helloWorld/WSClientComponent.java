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
//@snippet-start wsclientcomponent
public class WSClientComponent {
    public static void main(String[] args) {
        try {
            String url = "";
            if (args.length == 0) {
                url = "http://localhost:8080/";
            } else if (args.length == 1) {
                url = args[0];
            } else {
                System.out.println("Wrong number of arguments:");
                System.out.println("Usage: java HelloWorld [url]");
                return;
            }

            if (!url.startsWith("http://")) {
                url = "http://" + url;
            }

            RPCServiceClient serviceClient = new RPCServiceClient();

            Options options = serviceClient.getOptions();

            EndpointReference targetEPR;

            if (args.length == 0) {
                targetEPR = new EndpointReference(url + WSConstants.AXIS_SERVICES_PATH + "server_hello-world");
            } else {
                targetEPR = new EndpointReference(url + WSConstants.AXIS_SERVICES_PATH + "server_hello-world");
            }

            options.setTo(targetEPR);

            // Call sayText
            QName op = new QName("helloWorld");

            Object[] opArgs = new Object[] { "ProActive Team" };
            Class<?>[] returnTypes = new Class[] { String.class };

            Object[] response = serviceClient.invokeBlocking(op, opArgs, returnTypes);

            String result = (String) response[0];

            System.out.println("Client returned " + result);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }
}
//@snippet-end wsclientcomponent
