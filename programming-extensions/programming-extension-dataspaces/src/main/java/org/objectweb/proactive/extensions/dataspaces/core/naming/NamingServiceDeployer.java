/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.dataspaces.core.naming;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;


/**
 * Deploys {@link NamingService} instance on the local runtime.
 */
public final class NamingServiceDeployer {

    private static final String NAMING_SERVICE_DEFAULT_NAME = "defaultNamingService";

    /** URLs of the remote object, one url for each protocol in use*/
    final private String[] urls;

    final private NamingService namingService;

    RemoteObjectExposer<NamingService> roe;

    /**
     * Deploys locally a NamingService instance as a RemoteObject with default name.
     */
    public NamingServiceDeployer() throws ProActiveException {
        this(NAMING_SERVICE_DEFAULT_NAME);
    }

    /**
     * Deploys locally a NamingService instance as a RemoteObject with default name.
     * 
     * @param rebind true if the service must rebind an existing one, false if not.
     * 			If false, throws an exception if the service is already bound under the given name.
     */
    public NamingServiceDeployer(boolean rebind) throws ProActiveException {
        this(NAMING_SERVICE_DEFAULT_NAME, rebind);
    }

    /**
     * Deploys locally a NamingService instance as a RemoteObject with specified name.
     * This method throws an exception if the service is already bound under the given name.
     *
     * @param name
     *            of deployed RemoteObject
     */
    public NamingServiceDeployer(String name) throws ProActiveException {
        this(name, false);
    }

    /**
     * Deploys locally a NamingService instance as a RemoteObject with specified name.
     * Also specify if the naming service must rebind an existing one or not.
     *
     * @param name
     *            of deployed RemoteObject
     * @param rebind true if the service must rebind an existing one, false if not.
     * 			If false, throws an exception if the service is already bound under the given name.
     */
    public NamingServiceDeployer(String name, boolean rebind) throws ProActiveException {
        namingService = new NamingService();

        roe = PARemoteObject.newRemoteObject(NamingService.class.getName(), this.namingService);
        roe.createRemoteObject(name, rebind);
        urls = roe.getURLs();
    }

    public NamingService getLocalNamingService() {
        return this.namingService;
    }

    public NamingService getRemoteNamingService() throws ProActiveException {
        return (NamingService) RemoteObjectHelper.generatedObjectStub(this.roe.getRemoteObject());
    }

    /**
     * Return the main url of this NamingService, in case of multi-protocol, this is the url of the default protocol
     * @return default NamingService url
     */
    public String getNamingServiceURL() {
        // the main url of the naming service is stored at index 0, it is the url corresponding to the default protocol
        return this.urls[0];
    }

    /**
     * Return all the urls of this NamingService, one for each protocol used
     * @return all NamingService urls
     */
    public String[] getNamingServiceURLs() {
        return urls;
    }

    /**
     * Unexport the remote object.
     *
     * @throws ProActiveException
     */
    public void terminate() throws ProActiveException {
        if (roe != null) {
            roe.unexportAll();
            roe.unregisterAll();
            roe = null;
        }
    }
}
