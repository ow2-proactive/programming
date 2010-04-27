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
package functionalTests.component.webservices.cxf;

import static org.junit.Assert.assertTrue;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;
import org.objectweb.proactive.extensions.webservices.component.controller.AbstractPAWebServicesControllerImpl;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;

import functionalTests.FunctionalTest;
import functionalTests.component.webservices.common.ChooseNameComponent;
import functionalTests.component.webservices.common.ChooseNameItf;
import functionalTests.component.webservices.common.HelloNameComponent;
import functionalTests.component.webservices.common.HelloNameItf;


/**
 * A simple example to expose an active object as a web service.
 *
 * @author The ProActive Team
 */
public class TestCompositeComponent extends FunctionalTest {

    private String url;
    private WebServices ws;
    private PAWebServicesController wsc;

    @org.junit.Before
    public void deployComposite() {
        try {
            url = AbstractWebServicesFactory.getLocalUrl();

            Component boot = null;
            Component comp = null;
            Component hello = null;
            Component chooseName = null;

            boot = Utils.getBootstrapComponent();

            GCMTypeFactory tf = GCM.getGCMTypeFactory(boot);
            GenericFactory cf = GCM.getGenericFactory(boot);

            // type of server component
            ComponentType typeComp = tf.createFcType(new InterfaceType[] { tf.createFcItfType("hello-name",
                    HelloNameItf.class.getName(), false, false, false) });

            ComponentType typeHello = tf.createFcType(new InterfaceType[] {
                    tf.createFcItfType("hello-name", HelloNameItf.class.getName(), false, false, false),
                    tf.createFcItfType("choose-name", ChooseNameItf.class.getName(), true, false, false) });

            ComponentType typeChoose = tf.createFcType(new InterfaceType[] { tf.createFcItfType(
                    "choose-name", ChooseNameItf.class.getName(), false, false, false) });

            // create server component
            String controllersConfigFileLocation = AbstractPAWebServicesControllerImpl.getControllerFileUrl(
                    "cxf").getPath();
            ControllerDescription cd = new ControllerDescription("composite", Constants.COMPOSITE,
                controllersConfigFileLocation);
            comp = cf.newFcInstance(typeComp, cd, null);
            hello = cf.newFcInstance(typeHello, new ControllerDescription("hello", Constants.PRIMITIVE),
                    new ContentDescription(HelloNameComponent.class.getName(), null));
            chooseName = cf.newFcInstance(typeChoose, new ControllerDescription("choosename",
                Constants.PRIMITIVE), new ContentDescription(ChooseNameComponent.class.getName(), null));

            // start the component
            ContentController cc = GCM.getContentController(comp);
            cc.addFcSubComponent(hello);
            cc.addFcSubComponent(chooseName);
            BindingController bc = GCM.getBindingController(comp);
            bc.bindFc("hello-name", hello.getFcInterface("hello-name"));
            bc = GCM.getBindingController(hello);
            bc.bindFc("choose-name", chooseName.getFcInterface("choose-name"));
            GCM.getGCMLifeCycleController(comp).startFc();

            // Deploying the service in the Active Object way
            WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory("cxf");
            ws = wsf.getWebServices(url);
            ws.exposeComponentAsWebService(comp, "composite", new String[] { "hello-name" });

            // Deploying the service using the web service controller
            wsc = org.objectweb.proactive.extensions.webservices.component.Utils
                    .getPAWebServicesController(comp);
            wsc.initServlet();
            wsc.setUrl(url);
            wsc.exposeComponentAsWebService("composite2", new String[] { "hello-name" });
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @org.junit.Test
    public void testComposite() {

        try {
            ClientFactory cf = AbstractClientFactory.getClientFactory("cxf");
            Client client = cf.getClient(url, "composite_hello-name", HelloNameItf.class);
            int index = 0;
            String result = (String) client.call("helloName", new Object[] { index }, String.class)[0];
            assertTrue(result.equals("Hello ProActive Team!"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }

    }

    @org.junit.Test
    public void testComposite2() {

        try {
            ClientFactory cf = AbstractClientFactory.getClientFactory("cxf");
            Client client = cf.getClient(url, "composite2_hello-name", HelloNameItf.class);
            int index = 0;
            String result = (String) client.call("helloName", new Object[] { index }, String.class)[0];
            assertTrue(result.equals("Hello ProActive Team!"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @org.junit.After
    public void undeployComposite() {
        try {
            ws.unExposeComponentAsWebService("composite", new String[] { "hello-name" });
            wsc.unExposeComponentAsWebService("composite2", new String[] { "hello-name" });
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
