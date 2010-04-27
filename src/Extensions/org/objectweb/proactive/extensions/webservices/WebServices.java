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
package org.objectweb.proactive.extensions.webservices;

import java.lang.reflect.Method;

import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public interface WebServices {

    /**
     * url getter
     *
     * @return the WebServices url
     */
    public String getUrl();

    //@snippet-start WebServices_Methods_AO
    /**
     * Expose an active object as a web service with the methods specified in <code>methods</code>
     *
     * @param o The object to expose as a web service
     * @param urn The name of the service
     * @param methods The methods that will be exposed as web services functionalities
     *                   If null, then all methods will be exposed
     * @throws WebServicesException
     */
    public void exposeAsWebService(Object o, String urn, String[] methods) throws WebServicesException;

    /**
     * Expose an active object as a web service with the methods specified in <code>methods</code>
     *
     * @param o The object to expose as a web service
     * @param urn The name of the service
     * @param methods The methods that will be exposed as web services functionalities
     *                   If null, then all methods will be exposed
     * @throws WebServicesException
     */
    public void exposeAsWebService(Object o, String urn, Method[] methods) throws WebServicesException;

    /**
     * Expose an active object with all its methods as a web service
     *
     * @param o The object to expose as a web service
     * @param urn The name of the service
     * @throws WebServicesException
     */
    public void exposeAsWebService(Object o, String urn) throws WebServicesException;

    /**
     * Undeploy a service
     *
     * @param urn The name of the service
     * @throws WebServicesException
     */
    public void unExposeAsWebService(String urn) throws WebServicesException;

    //@snippet-end WebServices_Methods_AO

    //@snippet-start WebServices_Methods_Components
    /**
     * Expose a component as a web service. Each server interface of the component
     * will be accessible by  the urn [componentName]_[interfaceName].
     * Only the interfaces public methods of the specified interfaces in
     * <code>interfaceNames</code> will be exposed.
     *
     * @param component The component owning the interfaces that will be deployed as web services.
     * @param componentName Name of the component
     * @param interfaceNames Names of the interfaces we want to deploy.
      *                           If null, then all the interfaces will be deployed
     * @throws WebServicesException
     */
    public void exposeComponentAsWebService(Component component, String componentName, String[] interfaceNames)
            throws WebServicesException;

    /**
     * Expose a component as web service. Each server interface of the component
     * will be accessible by  the urn [componentName]_[interfaceName].
     * All the interfaces public methods of all interfaces will be exposed.
     *
     * @param component The component owning the interfaces that will be deployed as web services.
     * @param componentName Name of the component
     * @throws WebServicesException
     */
    public void exposeComponentAsWebService(Component component, String componentName)
            throws WebServicesException;

    /**
     * Undeploy all the client interfaces of a component deployed on a web server.
     *
     * @param component  The component owning the services interfaces
     * @param componentName The name of the component
     * @throws WebServicesException
     */
    public void unExposeComponentAsWebService(Component component, String componentName)
            throws WebServicesException;

    /**
     * Undeploy the given client interfaces of a component deployed on a web server.
     * If the array of interface names is null, then undeploy all the interfaces of
     * the component.
     *
     * @param component  The component owning the services interfaces
     * @param componentName The name of the component
     * @param interfaceNames Interfaces tp be undeployed
     * @throws WebServicesException
     */
    public void unExposeComponentAsWebService(Component component, String componentName,
            String[] interfaceNames) throws WebServicesException;

    /**
     * Undeploy specified interfaces of a component deployed on a web server
     *
     * @param componentName The name of the component
     * @param interfaceNames Interfaces to be undeployed
     * @throws WebServicesException
     */
    public void unExposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException;
    //@snippet-end WebServices_Methods_Components
}
