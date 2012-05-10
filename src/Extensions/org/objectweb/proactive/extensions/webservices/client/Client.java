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
package org.objectweb.proactive.extensions.webservices.client;

import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public interface Client {

    /**
     * Performs a call to a web service method whose return type is void
     *
     * @param method Name of the method
     * @param args Arguments of the methods
     * @throws WebServicesException
     */
    public void oneWayCall(String method, Object[] args) throws WebServicesException;

    /**
     * Performs a call to a web service method whose return type is not void
     *
     * @param method Name of the method
     * @param args Arguments of the method
     * @param returnTypes Return type of the method
     * @return the response of the call
     * @throws WebServicesException
     */
    public Object[] call(String method, Object[] args, Class<?>... returnTypes) throws WebServicesException;

    /**
     * @return The URL of the service
     */
    public String getUrl();

    /**
     * @return The name of the service
     */
    public String getServiceName();
}
