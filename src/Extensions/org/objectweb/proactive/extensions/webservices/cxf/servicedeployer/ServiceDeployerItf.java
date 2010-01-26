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
package org.objectweb.proactive.extensions.webservices.cxf.servicedeployer;

import org.objectweb.fractal.api.NoSuchInterfaceException;


/**
 * Interface of the service which will be deployed on the server at the
 * same time as the proactive web application. This service is used to deploy and undeploy
 * Active Object and components on the server side.
 *
 * @author The ProActive Team
 */
public interface ServiceDeployerItf {

    /**
     * Expose the marshalled active object as a web service
     *
     * @param marshalledObject marshalled object
     * @param serviceName Name of the service
     * @param marshalledSerializedMethods byte array representing the methods (of type Method)
     *        to be exposed
     * @param isComponent Specify whether the object we want to expose is a component
     * @throws NoSuchInterfaceException
     * @throws ClassNotFoundException
     */
    public void deploy(byte[] marshalledObject, String serviceName, byte[] marshalledSerializedMethods,
            boolean isComponent) throws NoSuchInterfaceException, ClassNotFoundException;

    /**
     * Undeploy the service whose name is serviceName
     *
     * @param serviceName name of the service
     */
    public void undeploy(String serviceName);
}
