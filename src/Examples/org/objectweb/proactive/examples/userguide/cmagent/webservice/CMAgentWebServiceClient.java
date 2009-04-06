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
package org.objectweb.proactive.examples.userguide.cmagent.webservice;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.objectweb.proactive.extensions.webservices.WSConstants;


/**
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 * An example to call the a java web service
 */
//@snippet-start webservice_cma_client_full
public class CMAgentWebServiceClient {
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

            EndpointReference targetEPR = new EndpointReference(url + WSConstants.AXIS_SERVICES_PATH +
                "cmAgentService");

            options.setTo(targetEPR);
            options.setAction("getLastRequestServeTime");

            // Call sayText
            QName op = new QName("getLastRequestServeTime");

            Object[] opArgs = new Object[] {};
            Class<?>[] returnTypes = new Class[] { String.class };
            // Call sayText
            Object[] response = serviceClient.invokeBlocking(op, opArgs, returnTypes);

            String name = (String) response[0];

            System.out.println(name);
        } catch (AxisFault e) {
            e.printStackTrace();
        }
    }
}
//@snippet-end webservice_cma_client_full