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
package org.objectweb.proactive.examples.documentation.webservices;

import java.io.File;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.documentation.classes.B;
import org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServicesInitActiveFactory;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class ActiveObjects {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            //@snippet-start webservices_AO_1
            B b = (B) PAActiveObject.newActive(B.class.getName(), new Object[] {});
            //@snippet-end webservices_AO_1

            //@snippet-start webservices_AO_2
            HelloWorld hw = (HelloWorld) PAActiveObject
                    .newActive(HelloWorld.class.getName(), new Object[] {});

            // As only one framework is supported for the moment,
            // you can use either one of the two following methods
            //@snippet-start webservices_AO_4
            //@snippet-start webservices_AO_5
            WebServicesFactory wsf;
            //@snippet-break webservices_AO_5
            wsf = AbstractWebServicesFactory.getDefaultWebServicesFactory();
            //@snippet-end webservices_AO_4
            //@snippet-resume webservices_AO_5
            wsf = AbstractWebServicesFactory.getWebServicesFactory("cxf");
            //@snippet-end webservices_AO_5

            // If you want to use the local Jetty server, you can use
            // AbstractWebServicesFactory.getLocalUrl() to get its url with
            // its port number (which is random except if you have set the
            // proactive.http.port variable)
            WebServices ws = wsf.getWebServices("http://localhost:8080/");

            ws.exposeAsWebService(hw, "MyHelloWorldService", new String[] { "putTextToSayAndConfirm",
                    "putTextToSay", "sayText" });
            //@snippet-end webservices_AO_2

            File applicationDescriptor = new File(args[1]);
            GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

            gcmad.startDeployment();

            GCMVirtualNode hello = gcmad.getVirtualNode("Hello");
            Node myNode = hello.getANode();

            //@snippet-start webservices_AO_7
            // Get the CXF InitActive in charge of deploying the CXF Servlet
            InitActive cxfInitActive = WebServicesInitActiveFactory.getInitActive("cxf");
            HelloWorld helloWorld = (HelloWorld) PAActiveObject.newActive(HelloWorld.class.getName(), null,
                    new Object[] {}, myNode, cxfInitActive, null);
            //@snippet-end webservices_AO_7

            //@snippet-start webservices_AO_8
            WebServicesInitActiveFactory.getInitActive("cxf").initServlet(myNode);
            //@snippet-end webservices_AO_8

            //@snippet-start webservices_AO_3
            // Instead of using "cxf", you can also use webServicesFactory.getFrameWorkId().
            // As only one framework is supported for the moment, use the following method
            // is equivalent to ClientFactory.getDefaultClientFactory()
            ClientFactory clientFactory = AbstractClientFactory.getClientFactory("cxf");

            // Instead of using "http://localhost:8080/", you can use ws.getUrl() to
            // ensure to get to good service address.
            Client client = clientFactory.getClient("http://localhost:8080/", "MyHelloWorldService",
                    HelloWorld.class);

            // Call which returns a result
            Object[] res = client.call("putTextToSayAndConfirm", new Object[] { "Hello World!" },
                    String.class);
            System.out.println((String) res[0]);

            // Call which does not return a result
            client.oneWayCall("putTextToSay", new Object[] { "Hi ProActive Team!" });

            // Call with no argument
            res = client.call("sayText", null, String.class);

            System.out.println((String) res[0]);

            // Call with no argument
            res = client.call("sayText", null, String.class);

            System.out.println((String) res[0]);
            //@snippet-end webservices_AO_3

        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
