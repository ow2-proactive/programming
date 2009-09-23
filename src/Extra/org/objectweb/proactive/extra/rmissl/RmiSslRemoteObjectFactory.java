package org.objectweb.proactive.extra.rmissl;

import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.objectweb.proactive.core.remoteobject.rmi.AbstractRmiRemoteObjectFactory;


/**
 *
 * @since ProActive 4.2.0
 */
public class RmiSslRemoteObjectFactory extends AbstractRmiRemoteObjectFactory {

    public RmiSslRemoteObjectFactory() {
        super("rmissl", RmiSslRemoteObjectImpl.class);
    }

    protected Registry getRegistry(URI url) throws RemoteException {
        return LocateRegistry.getRegistry(url.getHost(), url.getPort());
    }
}