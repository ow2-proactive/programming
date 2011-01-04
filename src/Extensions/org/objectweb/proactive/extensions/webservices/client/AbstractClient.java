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
package org.objectweb.proactive.extensions.webservices.client;

/**
 * @author The ProAcive Team
 *
 */
public abstract class AbstractClient implements Client {

    protected String url;
    protected String serviceName;
    protected Class<?> serviceClass;

    protected AbstractClient(String url, String serviceName, Class<?> serviceClass) {
        this.url = url;
        this.serviceName = serviceName;
        this.serviceClass = serviceClass;
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.client.Client#getServiceName()
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.client.Client#getUrl()
     */
    public String getUrl() {
        return this.url;
    }
}
