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

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public interface PAWebServicesController {

    public final static String WEBSERVICES_CONTROLLER = "webservices-controller";

    /**
     * Sets the url where the component will be deployed.
     *
     * @param The url where the component will be deployed.
     */
    public void setUrl(String url);

    /**
     * Gets the url where the component will be deployed.
     *
     * @return The url where the component will be deployed.
     */
    public String getUrl();

    /**
     * Initializes the web service servlet on a remote node
     *
     * @param node Node where to initialize the servlet
     * @throws ActiveObjectCreationException
     * @throws NodeException
     * @throws UnknownFrameWorkException
     */
    public void initServlet(Node... nodes) throws ActiveObjectCreationException, NodeException,
            UnknownFrameWorkException;

    /**
     * Gets the default local url where the component will be deployed.
     *
     * @return The default local url where the component will be deployed
     */
    public String getLocalUrl();

    //@snippet-start PAWebServicesController_expose_methods
    /**
     * Expose a component as a web service. Each server interface of the component
     * will be accessible by  the urn [componentName]_[interfaceName].
     * Only the interfaces public methods of the specified interfaces in
     * <code>interfaceNames</code> will be exposed.
     *
     * @param componentName Name of the component
     * @param interfaceNames Names of the interfaces we want to deploy.
      *                           If null, then all the interfaces will be deployed
     * @throws WebServicesException
     */
    public void exposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException;

    /**
     * Expose a component as web service. Each server interface of the component
     * will be accessible by  the urn [componentName]_[interfaceName].
     * All the interfaces public methods of all interfaces will be exposed.
     *
     * @param componentName Name of the component
     * @throws WebServicesException
     */
    public void exposeComponentAsWebService(String componentName) throws WebServicesException;

    /**
     * Undeploy all the client interfaces of a component deployed on a web server.
     *
     * @param componentName The name of the component
     * @throws WebServicesException
     */
    public void unExposeComponentAsWebService(String componentName) throws WebServicesException;

    /**
     * Undeploy specified interfaces of a component deployed on a web server
     *
     * @param componentName The name of the component
     * @param interfaceNames Interfaces to be undeployed
     * @throws WebServicesException
     */
    public void unExposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException;
    //@snippet-end PAWebServicesController_expose_methods
}
