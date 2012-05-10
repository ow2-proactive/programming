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
//@snippet-start wsclientcomponent
package org.objectweb.proactive.examples.webservices.helloWorld;

import java.net.MalformedURLException;
import java.net.URL;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public class HelloWorldComponentClient {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) {

        String url = "";
        String wsFrameWork = "";
        if (args.length == 0) {
            url = "http://localhost:8080/";
            wsFrameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
        } else if (args.length == 1) {
            try {
                new URL(args[0]);
                url = args[0];
                wsFrameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
            } catch (MalformedURLException me) {
                // Given argument is not the URL to use to expose the web service, it should be the web service framework to use
                url = "http://localhost:8080/";
                wsFrameWork = args[0];
            }
        } else if (args.length == 2) {
            url = args[0];
            wsFrameWork = args[1];
        } else {
            System.out.println("Wrong number of arguments:");
            System.out.println("Usage: java WSClientCXF [url] [wsFrameWork]");
            System.out.println("where wsFrameWork should be 'cxf'");
            return;
        }

        ClientFactory cf = null;
        try {
            cf = AbstractClientFactory.getClientFactory(wsFrameWork);
        } catch (UnknownFrameWorkException e1) {
            e1.printStackTrace();
            System.exit(-1);
        }

        Client client = null;
        try {
            client = cf.getClient(url, "server_hello-world", HelloWorldItf.class);
        } catch (WebServicesException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.exit(-1);
        }

        Object[] res;
        try {

            res = client.call("helloWorld", new Object[] { "ProActive Team" }, String.class);
            String str = (String) res[0];
            System.out.println(str);

            client.oneWayCall("setText", new Object[] { "A text has been inserted" });

            res = client.call("sayText", null, String.class);
            str = (String) res[0];
            System.out.println(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            client = cf.getClient(url, "server_goodbye-world", GoodByeWorldItf.class);
        } catch (WebServicesException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            System.exit(-1);
        }
        try {
            res = client.call("goodByeWorld", new Object[] { "ProActive Team" }, String.class);
            String str = (String) res[0];
            System.out.println(str);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//@snippet-end wsclientcomponent