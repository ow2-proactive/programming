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
package org.objectweb.proactive.core.remoteobject.rmi;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;

import java.io.IOException;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;


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

    public RmiRemoteObjectImpl(InternalRemoteRemoteObject target, RMIServerSocketFactory sf,
            RMIClientSocketFactory cf) throws java.rmi.RemoteException {
        super(0, cf, sf);
        this.internalrrObject = target;
    }

    public Reply receiveMessage(Request message) throws RemoteException, RenegotiateSessionException,
            ProActiveException, IOException {
        return this.internalrrObject.receiveMessage(message);
    }

    @Override
    public URI getURI() throws ProActiveException, IOException {
        return internalrrObject.getURI();
    }
}
