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
package org.objectweb.proactive.core.descriptor.services;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;


/**
 * This class represents a service to acquire with an RMI lookup a ProActiveRuntime(JVM) previously registered
 * in a RMIRegistry on a local or remote host.
 * This service can be defined and used transparently when using XML Deployment descriptor
 * @author The ProActive Team
 * @version 1.0,  2004/09/20
 * @since   ProActive 2.0.1
 */
public class RMIRegistryLookupService implements UniversalService {

    private static final long serialVersionUID = 60L;

    /** lookup url */
    protected String url;
    protected static String serviceName = "RMIRegistryLookup";
    protected ProActiveRuntime[] runtimeArray;

    public RMIRegistryLookupService(String url) {
        this.url = url;
        this.runtimeArray = new ProActiveRuntime[1];
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     */
    public ProActiveRuntime[] startService() throws ProActiveException {
        ProActiveRuntime part = RuntimeFactory.getRuntime(url);
        runtimeArray[0] = part;
        return runtimeArray;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#getServiceName()
     */
    public String getServiceName() {
        return serviceName;
    }
}
