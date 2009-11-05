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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.webservices.client.axis2;

import org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory;
import org.objectweb.proactive.extensions.webservices.client.Client;
import org.objectweb.proactive.extensions.webservices.client.ClientFactory;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * @author The ProActive Team
 *
 */
public class Axis2ClientFactory extends AbstractClientFactory implements ClientFactory {

    /** (non-Javadoc)
     * @see org.objectweb.proactive.extensions.webservices.client.ClientFactory#getFrameWorkId()
     */
    public String getFrameWorkId() {
        return "axis2";
    }

    /**
     * @see org.objectweb.proactive.extensions.webservices.client.AbstractClientFactory#newClient(java.lang.String, java.lang.String, java.lang.Class<?>)
     */
    protected Client newClient(String url, String serviceName, Class<?> serviceClass)
            throws WebServicesException {
        return new Axis2Client(url, serviceName, serviceClass);
    }

}
