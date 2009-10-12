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
package org.objectweb.proactive.extensions.webservices.client.cxf;

import org.apache.cxf.frontend.ClientFactoryBean;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.client.AbstractClient;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public class CXFClient extends AbstractClient implements Client {

    private org.apache.cxf.endpoint.Client client;

    protected CXFClient(String url, String serviceName, Class<?> serviceClass) {
        super(url, serviceName, serviceClass);
        client = getCxfClient();
    }

    /**
     * get a cxf client
     *
     * @param url Url of the service
     * @param serviceClass Class of the service (needed for cxf contrary to Axis2)
     * @param serviceName Name of the service
     * @return a cxf Client
     */
    private org.apache.cxf.endpoint.Client getCxfClient() {

        ClientFactoryBean factory = new ClientFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(url + WSConstants.SERVICES_PATH + serviceName);
        factory.getServiceFactory().setQualifyWrapperSchema(false);
        return factory.create();

    }

    /**
     * CXF one way call (for void methods)
     *
     * @param method Method to call
     * @param args Arguments to give to the method
     * @param serviceClass Class of the service
     * @throws WebServicesException
     * @throws Exception
     */
    public void oneWayCall(String method, Object[] args) throws WebServicesException {
        if (args == null)
            args = new Object[] {};

        try {
            client.invoke(method, args);
        } catch (Exception e) {
            throw new WebServicesException("An exception occured during the call to " + "the method '" +
                method + "' of the service '" + this.serviceName + "' located at " + this.url, e);
        }
    }

    /**
     * Call of a method whose return type is different to void using CXF.
     * In the case of a CXF call, we do not need to specify the class of
     * the return type
     *
     * @param method Method to call
     * @param args Arguments to give to the method
     * @param serviceClass Class of the service
     * @return Result of the call
     * @throws WebServicesException
     * @throws Exception
     */
    public Object[] call(String method, Object[] args, Class<?>... serviceClass) throws WebServicesException {
        if (args == null)
            args = new Object[] {};

        Object[] result;
        try {
            result = client.invoke(method, args);
        } catch (Exception e) {
            throw new WebServicesException("An exception occured during the call to " + "the method '" +
                method + "' of the service '" + this.serviceName + "' located at " + this.url, e);
        }

        return result;
    }

}
