package org.objectweb.proactive.extra.rmissl;

import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.rmi.RmiRemoteObjectImpl;


/**
 * @since ProActive 4.2.0
 */
public class RmiSslRemoteObjectImpl extends RmiRemoteObjectImpl {

    public RmiSslRemoteObjectImpl(InternalRemoteRemoteObject target) throws java.rmi.RemoteException {
        super(target, new SslRmiServerSocketFactory(), new SslRmiClientSocketFactory());
    }
}