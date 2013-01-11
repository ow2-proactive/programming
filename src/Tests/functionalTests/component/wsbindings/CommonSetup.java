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
package functionalTests.component.wsbindings;

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.junit.After;
import org.junit.BeforeClass;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.component.Utils;
import org.objectweb.proactive.extensions.webservices.component.controller.AbstractPAWebServicesControllerImpl;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;

import functionalTests.ComponentTest;


public abstract class CommonSetup extends ComponentTest {
    public static int NUMBER_SERVERS = 3;
    public static String SERVER_DEFAULT_NAME = "Server";
    public static String SERVER_SERVICES_NAME = "Services";
    public static String SERVER_SERVICEMULTICAST_NAME = "Service";

    protected Component boot;
    protected GCMTypeFactory tf;
    protected GenericFactory gf;
    protected String url;
    protected Component[] servers;
    protected ComponentType componentType;
    protected WebServicesFactory wsf;
    protected WebServices ws;

    @BeforeClass
    final static public void setJettyPort() {
        CentralPAPropertyRepository.PA_XMLHTTP_PORT.setValue(8888);
    }

    public void setUpAndDeploy() throws Exception {
        boot = Utils.getBootstrapComponent();
        tf = GCM.getGCMTypeFactory(boot);
        gf = GCM.getGenericFactory(boot);

        ComponentType sType = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType(SERVER_SERVICES_NAME, Services.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType(SERVER_SERVICEMULTICAST_NAME, Service.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE) });
        String controllersConfigFileLocation = AbstractPAWebServicesControllerImpl
                .getControllerFileUrl("cxf").getPath();
        url = AbstractWebServicesFactory.getLocalUrl();
        servers = new Component[NUMBER_SERVERS];
        for (int i = 0; i < NUMBER_SERVERS; i++) {
            servers[i] = gf.newFcInstance(sType, new ControllerDescription(SERVER_DEFAULT_NAME + i,
                Constants.PRIMITIVE, controllersConfigFileLocation), new ContentDescription(Server.class
                    .getName()));
            GCM.getGCMLifeCycleController(servers[i]).startFc();
            PAWebServicesController wsController = Utils.getPAWebServicesController(servers[i]);
            wsController.setUrl(url);
            wsController.exposeComponentAsWebService(SERVER_DEFAULT_NAME + i);
        }

        componentType = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("Runner", Runner.class.getName(), TypeFactory.SERVER,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createFcItfType(Client.SERVICES_NAME, Services.class.getName(), TypeFactory.CLIENT,
                        TypeFactory.MANDATORY, TypeFactory.SINGLE),
                tf.createGCMItfType(Client.SERVICEMULTICASTREAL_NAME, ServiceMulticast.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.OPTIONAL, GCMTypeFactory.MULTICAST_CARDINALITY),
                tf.createFcItfType(Client.SERVICEMULTICASTFALSE_NAME, ServiceMulticast.class.getName(),
                        TypeFactory.CLIENT, TypeFactory.OPTIONAL, TypeFactory.SINGLE) });
    }

    @After
    public void undeploy() throws Exception {
        for (int i = 0; i < NUMBER_SERVERS; i++) {
            Utils.getPAWebServicesController(servers[i]).unExposeComponentAsWebService(
                    SERVER_DEFAULT_NAME + i);
        }
    }
}
