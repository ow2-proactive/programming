/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
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
package org.objectweb.proactive.examples.documentation.webservices;

import java.io.File;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.documentation.components.A;
import org.objectweb.proactive.examples.documentation.components.AImpl;
import org.objectweb.proactive.examples.webservices.helloWorld.GoodByeWorldItf;
import org.objectweb.proactive.examples.webservices.helloWorld.HelloWorldComponent;
import org.objectweb.proactive.examples.webservices.helloWorld.HelloWorldItf;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServicesInitActiveFactory;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;
import org.objectweb.proactive.extensions.webservices.component.controller.AbstractPAWebServicesControllerImpl;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Components {

    public static void main(String[] args) throws InstantiationException, NoSuchInterfaceException,
            IllegalLifeCycleException, ProActiveException {
        //@snippet-start webservices_Component_1
        Component boot = Utils.getBootstrapComponent();

        GCMTypeFactory tf = GCM.getGCMTypeFactory(boot);
        GenericFactory cf = GCM.getGenericFactory(boot);

        // type of server component
        ComponentType sType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("hello-world", A.class
                .getName(), false, false, false) });
        // create server component
        Component a = cf.newFcInstance(sType, new ControllerDescription("server", Constants.PRIMITIVE),
                new ContentDescription(AImpl.class.getName()));
        //start the component
        GCM.getGCMLifeCycleController(a).startFc();
        //@snippet-end webservices_Component_1

        // If you want the default WebServicesFactory, you can use
        // AbstractWebServicesFactory.getDefaultWebServicesFactory()
        //@snippet-start webservices_Component_6
        //@snippet-start webservices_Component_7
        //@snippet-start webservices_Component_8
        WebServicesFactory wsf;
        //@snippet-break webservices_Component_7
        //@snippet-break webservices_Component_8
        wsf = AbstractWebServicesFactory.getWebServicesFactory("axis2");
        //@snippet-end webservices_Component_6
        //@snippet-resume webservices_Component_7
        wsf = AbstractWebServicesFactory.getDefaultWebServicesFactory();
        //@snippet-end webservices_Component_7
        //@snippet-resume webservices_Component_8
        wsf = AbstractWebServicesFactory.getWebServicesFactory("cxf");
        //@snippet-end webservices_Component_8

        //@snippet-start webservices_Component_2
        // If you want the default WebServicesFactory, you can use
        // WebServicesFactory.getDefaultWebServicesFactory()
        WebServicesFactory wsFactory = AbstractWebServicesFactory.getWebServicesFactory("cxf");

        // If you want to use the local Jetty server, you can use
        // AbstractWebServicesFactory.getLocalUrl() to get its url with
        // its port number (which is random except if you have set the
        // proactive.http.port variable)
        WebServices webServices = wsFactory.getWebServices("http://localhost:8080/");

        webServices.exposeComponentAsWebService(a, "myComponent");
        //@snippet-end webservices_Component_2
        //@snippet-start webservices_Component_3
        webServices.unExposeComponentAsWebService(a, "myComponent");
        //@snippet-end webservices_Component_3

        //@snippet-start webservices_Component_4
        //@snippet-start webservices_Component_18
        Component componentBoot = Utils.getBootstrapComponent();

        GCMTypeFactory typeFactory = GCM.getGCMTypeFactory(componentBoot);
        GenericFactory genericFactory = GCM.getGenericFactory(componentBoot);

        // type of server component
        ComponentType componentType = typeFactory.createFcType(new InterfaceType[] {
                typeFactory
                        .createFcItfType("hello-world", HelloWorldItf.class.getName(), false, false, false),
                typeFactory.createFcItfType("goodbye-world", GoodByeWorldItf.class.getName(), false, false,
                        false) });

        //@snippet-break webservices_Component_18
        // create server component
        Component helloWorld = genericFactory.newFcInstance(componentType, new ControllerDescription(
            "server", Constants.PRIMITIVE), new ContentDescription(HelloWorldComponent.class.getName()));
        //start the component
        GCM.getGCMLifeCycleController(helloWorld).startFc();

        // If you want the default WebServicesFactory, you can use
        // WebServicesFactory.getDefaultWebServicesFactory()
        WebServicesFactory webServicesFactory = AbstractWebServicesFactory.getWebServicesFactory("cxf");

        // If you want to use the local Jetty server, you can use
        // AbstractWebServicesFactory.getLocalUrl() to get its url with
        // its port number (which is random except if you have set the
        // proactive.http.port variable)
        WebServices webservices = webServicesFactory.getWebServices("http://localhost:8080/");

        // If you want to expose only the goodbye-world interface for example,
        // use the following line instead:
        // webservices.exposeComponentAsWebService(helloWorld, "MyHelloWorldComponentService", new String[] { "goodbye-world" });
        webservices.exposeComponentAsWebService(helloWorld, "MyHelloWorldComponentService");
        //@snippet-end webservices_Component_4

        //@snippet-resume webservices_Component_18
        // Get the web services controller corresponding to the chosen framework (here cxf)
        String controllerPath = AbstractPAWebServicesControllerImpl.getControllerFileUrl("cxf").getPath();

        // Create the component using this controller
        ControllerDescription controllerDesc = new ControllerDescription("server", Constants.PRIMITIVE,
            controllerPath);

        // Create server component
        Component helloWorldComponent = genericFactory.newFcInstance(componentType, controllerDesc,
                new ContentDescription(HelloWorldComponent.class.getName()));

        // Start the component
        GCM.getGCMLifeCycleController(helloWorldComponent).startFc();

        // Get the web services controller
        PAWebServicesController wsController = org.objectweb.proactive.extensions.webservices.component.Utils
                .getPAWebServicesController(helloWorldComponent);

        // Set the Url where the component has to be exposed
        wsController.setUrl("http://localhost:8080/");

        // Expose the component as a web service
        wsController.exposeComponentAsWebService("MyHelloWorldComponentService");
        //@snippet-end webservices_Component_18

        File applicationDescriptor = new File(args[1]);
        GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

        gcmad.startDeployment();

        GCMVirtualNode hello = gcmad.getVirtualNode("Hello");
        Node myNode = hello.getANode();

        //@snippet-start webservices_Component_9
        // Get the Axis2 InitActive in charge of deploying the Axis2 Servlet
        InitActive axis2InitActive = WebServicesInitActiveFactory.getInitActive("axis2");
        Component myComponent = ((PAGenericFactory) genericFactory).newFcInstance(componentType,
                new ControllerDescription("myControllerName", Constants.PRIMITIVE), new ContentDescription(
                    HelloWorldComponent.class.getName(), null, axis2InitActive, null), myNode);
        //@snippet-end webservices_Component_9

        //@snippet-start webservices_Component_10
        WebServicesInitActiveFactory.getInitActive("cxf").initServlet(myNode);
        //@snippet-end webservices_Component_10

        //@snippet-start webservices_Component_11
        // Get the web services controller corresponding to the chosen framework (here axis2)
        String controllersConfigFileLocation = AbstractPAWebServicesControllerImpl.getControllerFileUrl(
                "axis2").getPath();

        // Create the component using this controller
        ControllerDescription cd = new ControllerDescription("server", Constants.PRIMITIVE,
            controllersConfigFileLocation);
        Component myComp = genericFactory.newFcInstance(componentType, cd, new ContentDescription(
            HelloWorldComponent.class.getName()));
        //@snippet-end webservices_Component_11

        //@snippet-start webservices_Component_12
        // Get the web services controller
        PAWebServicesController wsc = org.objectweb.proactive.extensions.webservices.component.Utils
                .getPAWebServicesController(myComp);
        //@snippet-end webservices_Component_12

        //@snippet-start webservices_Component_16
        // Deploy the web service servlet on the jetty server corresponding to the node where the component has been
        // deployed.
        wsc.initServlet();
        //@snippet-end webservices_Component_16

        //@snippet-start webservices_Component_17
        // To expose a component on a remote host where this servlet has not been deployed yet, add
        // the node started on this host as argument. You can put as many nodes as you want.
        wsc.initServlet(myNode);
        //@snippet-end webservices_Component_17

        //@snippet-start webservices_Component_13
        // Set the Url where the component has to be exposed
        wsc.setUrl("http://localhost:8080/");
        //@snippet-end webservices_Component_13

        //@snippet-start webservices_Component_14
        String url = wsc.getLocalUrl();
        //@snippet-end webservices_Component_14

        //@snippet-start webservices_Component_15
        // Use the controller to expose the component as a web service
        wsc.exposeComponentAsWebService("MyComponentName");
        //@snippet-end webservices_Component_15

        //@snippet-start webservices_Component_5
        // Instead of using "cxf", you can also use webServicesFactory.getFrameWorkId()
        // in order to be sure to get the same framework as the service
        // has used to be exposed. However, you can call a cxf service
        // using an axis2 client but there exists some incompatibility
        // between these two frameworks.
        // If you want the default ClientFactory, you can use
        // ClientFactory.getDefaultClientFactory()
        ClientFactory cFactory = AbstractClientFactory.getClientFactory("cxf");

        // Instead of using "http://localhost:8080/", you can use ws.getUrl() to
        // ensure to get to good service address.
        Client serviceClient = cFactory.getClient("http://localhost:8080/",
                "MyHelloWorldComponentService_goodbye-world", GoodByeWorldItf.class);

        // Call which returns a result
        Object[] result = serviceClient.call("goodByeWorld", new Object[] { "ProActive Team" }, String.class);
        System.out.println((String) result[0]);

        // Call which does not return a result
        serviceClient.oneWayCall("setText", new Object[] { "Hi ProActive Team!" });

        // Call with no argument
        result = serviceClient.call("sayText", null, String.class);

        System.out.println((String) result[0]);
        //@snippet-end webservices_Component_5

    }
}
