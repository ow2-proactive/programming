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
package org.objectweb.proactive.core.remoteobject.rmi;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;


/**
 * RMI implementation of the remote remote object interface
 *
 *
 */

public class RmiRemoteObjectImpl extends UnicastRemoteObject implements RmiRemoteObject {
    protected InternalRemoteRemoteObject internalrrObject;

    public RmiRemoteObjectImpl() throws java.rmi.RemoteException {
    }

    public RmiRemoteObjectImpl(InternalRemoteRemoteObject target) throws java.rmi.RemoteException {
        this.internalrrObject = target;
    }

    public RmiRemoteObjectImpl(InternalRemoteRemoteObject target, RMIServerSocketFactory sf, RMIClientSocketFactory cf)
            throws java.rmi.RemoteException {
        super(0, cf, sf);
        this.internalrrObject = target;
    }

    public Reply receiveMessage(Request message) throws RemoteException, ProActiveException, IOException {
        return this.internalrrObject.receiveMessage(message);
    }

    @Override
    public URI getURI() throws ProActiveException, IOException {
        return internalrrObject.getURI();
    }
}
