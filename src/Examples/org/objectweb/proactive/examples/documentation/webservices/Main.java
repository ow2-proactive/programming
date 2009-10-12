package org.objectweb.proactive.examples.documentation.webservices;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.examples.documentation.classes.B;
import org.objectweb.proactive.examples.documentation.components.A;
import org.objectweb.proactive.examples.documentation.components.AImpl;
import org.objectweb.proactive.examples.webservices.helloWorld.HelloWorld;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;


public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            //@snippet-start webservices_AO_1
            B b = (B) PAActiveObject.newActive(B.class.getName(), new Object[] {});
            //@snippet-end webservices_AO_1

            //@snippet-start webservices_Component_1
            Component boot = org.objectweb.fractal.api.Fractal.getBootstrapComponent();

            TypeFactory tf = Fractal.getTypeFactory(boot);
            GenericFactory cf = Fractal.getGenericFactory(boot);

            // type of server component
            ComponentType sType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("hello-world",
                    A.class.getName(), false, false, false) });
            // create server component
            Component a = cf.newFcInstance(sType, new ControllerDescription("server", Constants.PRIMITIVE),
                    new ContentDescription(AImpl.class.getName()));
            //start the component
            Fractal.getLifeCycleController(a).startFc();
            //@snippet-end webservices_Component_1

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

            //@snippet-start webservices_AO_2
            HelloWorld hw = (HelloWorld) PAActiveObject
                    .newActive(HelloWorld.class.getName(), new Object[] {});

            // If you want the default WebServicesFactory, you can use
            // WebServicesFactory.getDefaultWebServicesFactory()
            WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory("cxf");

            // If you want to use the local Jetty server, you can use
            // AbstractWebServicesFactory.getLocalUrl() to get its url with
            // its port number (which is random except if you have set the
            // proactive.http.port variable)
            WebServices ws = wsf.getWebServices("http://localhost:8080/");

            ws.exposeAsWebService(hw, "MyHelloWorldService", new String[] { "putTextToSayAndConfirm",
                    "putTextToSay", "sayText" });
            //@snippet-end webservices_AO_2

            //@snippet-start webservices_AO_3
            // Instead of using "cxf", you can also use wsf.getFrameWorkId()
            // in order to be sure to get the same framework as the service
            // has used to be exposed. However, you can call a cxf service
            // using an axis2 client but there exists some incompatibility
            // between these two framework.
            // If you want the default ClientFactory, you can use
            // ClientFactory.getDefaultClientFactory()
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
            //@snippet-end webservices_AO_3
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalLifeCycleException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
