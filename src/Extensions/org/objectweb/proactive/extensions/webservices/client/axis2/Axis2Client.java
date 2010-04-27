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
package org.objectweb.proactive.extensions.webservices.client.axis2;

import javax.xml.namespace.QName;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.rpc.client.RPCServiceClient;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.client.AbstractClient;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public class Axis2Client extends AbstractClient implements Client {

    private RPCServiceClient client;

    protected Axis2Client(String url, String serviceName, Class<?> serviceClass) throws WebServicesException {
        super(url, serviceName, serviceClass);
        try {
            client = getAxis2Client();
        } catch (AxisFault e) {
            throw new WebServicesException("An AxisFault exception occured when creating "
                + "the axis2 client", e);
        }
    }

    /**
     * Get an axis2 client
     *
     * @param url Url of the service
     * @param serviceName Name of the service
     * @return an RPCServiceClient
     * @throws AxisFault
     */
    private RPCServiceClient getAxis2Client() throws AxisFault {
        RPCServiceClient serviceClient = new RPCServiceClient();
        Options options = serviceClient.getOptions();

        EndpointReference targetEPR = new EndpointReference(url + WSConstants.SERVICES_PATH + serviceName);

        serviceClient.getAxisService().setElementFormDefault(
                CentralPAPropertyRepository.PA_WEBSERVICES_ELEMENTFORMDEFAULT.isTrue());
        options.setTo(targetEPR);

        return serviceClient;
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.client.Client#call(java.lang.String, java.lang.Object[], java.lang.Class<?>[])
     */
    public Object[] call(String method, Object[] args, Class<?>... returnTypes) throws WebServicesException {

        if (args == null)
            args = new Object[] {};

        try {

            client.getOptions().setAction(method);
            QName op = new QName(method);
            Object[] response;
            response = client.invokeBlocking(op, args, returnTypes);
            return response;
        } catch (AxisFault e) {
            throw new WebServicesException("An AxisFault exception occured during the call " +
                "to the method '" + method + "' of the service '" + this.serviceName + "' located at " +
                this.url, e);
        }
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.client.Client#oneWayCall(java.lang.String, java.lang.Object[])
     */
    public void oneWayCall(String method, Object[] args) throws WebServicesException {

        if (args == null)
            args = new Object[] {};

        try {
            client.getOptions().setAction(method);
            QName op = new QName(method);
            client.invokeRobust(op, args);
        } catch (AxisFault e) {
            throw new WebServicesException("An AxisFault exception occured during the call " +
                "to the method '" + method + "' of the service '" + this.serviceName + "' located at " +
                this.url, e);
        }
    }

}
