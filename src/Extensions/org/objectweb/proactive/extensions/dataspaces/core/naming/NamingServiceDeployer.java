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
package org.objectweb.proactive.extensions.dataspaces.core.naming;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;


/**
 * Deploys {@link NamingService} instance on the local runtime.
 */
public class NamingServiceDeployer {

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
     * Deploys locally a NamingService instance as a RemoteObject with specified name.
     *
     * @param name
     *            of deployed RemoteObject
     */
    public NamingServiceDeployer(String name) throws ProActiveException {
        namingService = new NamingService();

        roe = PARemoteObject.newRemoteObject(NamingService.class.getName(), this.namingService);
        roe.createRemoteObject(name, false);
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
