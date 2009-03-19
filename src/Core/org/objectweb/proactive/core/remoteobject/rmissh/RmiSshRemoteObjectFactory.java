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
package org.objectweb.proactive.core.remoteobject.rmissh;

import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.httpserver.ClassServerServlet;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.http.HTTPTransportServlet;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObject;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectFactory;
import org.objectweb.proactive.core.ssh.rmissh.SshRMIClientSocketFactory;
import org.objectweb.proactive.core.util.URIBuilder;


public class RmiSshRemoteObjectFactory extends RmiRemoteObjectFactory {
    protected static String protocolIdentifier = Constants.RMISSH_PROTOCOL_IDENTIFIER;

    static {
        createClassServer();
    }

    protected static synchronized void createClassServer() {
        HTTPTransportServlet.get();
        ClassServerServlet.get();

        String codebase = ClassServerServlet.get().getCodeBase();
        addCodebase(codebase);
        // FIXME: add HTTP SSH ?
    }

    @Override
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        try {
            return new RmiSshRemoteObjectImpl(target);
        } catch (RemoteException e) {
            throw new ProActiveException(e);
        }
    }

    @Override
    public URI[] list(URI url) throws ProActiveException {
        try {
            Registry registry = LocateRegistry.getRegistry(url.getHost(), url.getPort(),
                    new SshRMIClientSocketFactory());

            String[] names = registry.list();
            if (names != null) {
                URI[] uris = new URI[names.length];
                for (int i = 0; i < names.length; i++) {
                    uris[i] = URIBuilder.buildURI(url.getHost(), names[i],
                            Constants.RMISSH_PROTOCOL_IDENTIFIER, url.getPort());
                }
                return uris;
            }
        } catch (Exception e) {
            throw new ProActiveException(e);
        }
        return null;
    }

    @Override
    public RemoteObject lookup(URI url1) throws ProActiveException {
        String host;
        host = URIBuilder.getHostNameFromUrl(url1);

        int port = URIBuilder.getPortNumber(url1);
        try {
            Registry registry = LocateRegistry.getRegistry(host, port, new SshRMIClientSocketFactory());
            RmiRemoteObject objectStub = (RmiRemoteObject) registry.lookup(URIBuilder.getNameFromURI(url1));
            return new RemoteObjectAdapter(objectStub);
        } catch (java.rmi.NotBoundException e) {
            throw new ProActiveException("The url " + url1 + " is not bound to any known object");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getProtocolId() {
        return protocolIdentifier;
    }
}
