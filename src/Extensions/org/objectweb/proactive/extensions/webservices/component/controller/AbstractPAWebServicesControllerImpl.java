/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.extensions.webservices.component.controller;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.control.AbstractPAController;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServicesInitActiveFactory;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public abstract class AbstractPAWebServicesControllerImpl extends AbstractPAController implements
        PAWebServicesController {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    private final String framework;
    private String url;

    protected static final Map<String, URL> controllerFileUrls;

    static {
        controllerFileUrls = new HashMap<String, URL>();
        controllerFileUrls
                .put(
                        "axis2",
                        AbstractPAWebServicesControllerImpl.class
                                .getResource("/org/objectweb/proactive/extensions/webservices/axis2/component/controller/axis2-component-config.xml"));
        controllerFileUrls
                .put(
                        "cxf",
                        AbstractPAWebServicesControllerImpl.class
                                .getResource("/org/objectweb/proactive/extensions/webservices/cxf/component/controller/cxf-component-config.xml"));
    }

    public AbstractPAWebServicesControllerImpl(Component owner) {
        super(owner);
        this.framework = getFramework();
        this.url = getLocalUrl();
    }

    /**
     * Gets the framework
     */
    protected abstract String getFramework();

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(
                    PAWebServicesController.WEBSERVICES_CONTROLLER, PAWebServicesController.class.getName(),
                    TypeFactory.SERVER, TypeFactory.MANDATORY, TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    public void initServlet(Node... nodes) throws ActiveObjectCreationException, NodeException,
            UnknownFrameWorkException {
        if (nodes == null || nodes.length == 0) {
            WebServicesInitActiveFactory.getInitActive(this.framework).initServlet(PAActiveObject.getNode());
        } else {
            for (Node node : nodes)
                WebServicesInitActiveFactory.getInitActive(this.framework).initServlet(node);
        }
    }

    public String getLocalPort() {
        return AbstractWebServicesFactory.getLocalPort();
    }

    public String getLocalUrl() {
        return AbstractWebServicesFactory.getLocalUrl();
    }

    public void exposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException {
        WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory(this.framework);
        WebServices ws = wsf.getWebServices(url);
        ws.exposeComponentAsWebService(Fractive.getComponentRepresentativeOnThis(), componentName,
                interfaceNames);
    }

    public void exposeComponentAsWebService(String componentName) throws WebServicesException {
        WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory(this.framework);
        WebServices ws = wsf.getWebServices(url);
        ws.exposeComponentAsWebService(Fractive.getComponentRepresentativeOnThis(), componentName);
    }

    public void unExposeComponentAsWebService(String componentName) throws WebServicesException {
        WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory(this.framework);
        WebServices ws = wsf.getWebServices(url);
        ws.unExposeComponentAsWebService(Fractive.getComponentRepresentativeOnThis(), componentName);
    }

    public void unExposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException {
        WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory(this.framework);
        WebServices ws = wsf.getWebServices(url);
        ws.unExposeComponentAsWebService(Fractive.getComponentRepresentativeOnThis(), componentName,
                interfaceNames);
    }

    static public URL getControllerFileUrl(String wsFramework) throws UnknownFrameWorkException {
        URL controllersUrl = AbstractPAWebServicesControllerImpl.controllerFileUrls.get(wsFramework);

        if (controllersUrl == null)
            throw new UnknownFrameWorkException("Framework '" + wsFramework + "' is not known");

        return controllersUrl;
    }
}
