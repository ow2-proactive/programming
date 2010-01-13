/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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

    /** URL of the remote object */
    final private String url;

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
        url = roe.getURL();
    }

    public NamingService getLocalNamingService() {
        return this.namingService;
    }

    public NamingService getRemoteNamingService() throws ProActiveException {
        return (NamingService) RemoteObjectHelper.generatedObjectStub(this.roe.getRemoteObject());
    }

    public String getNamingServiceURL() {
        return this.url;
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
