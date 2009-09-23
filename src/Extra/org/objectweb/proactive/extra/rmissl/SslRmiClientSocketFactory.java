package org.objectweb.proactive.extra.rmissl;

import java.io.IOException;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.HostsInfos;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @since ProActive 4.2.0
 */
public class SslRmiClientSocketFactory implements RMIClientSocketFactory, java.io.Serializable {

    static final public Logger logger = ProActiveLogger.getLogger(Loggers.SSH);

    public Socket createSocket(String host, int port) throws IOException {
        String realName = HostsInfos.getSecondaryName(host);
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(realName, port);

        socket.setEnabledCipherSuites(SSLCONSTANTS.enabled_ciphers);
        return socket;
    }

    @Override
    public boolean equals(Object obj) {
        // the equals method is class based, since all instances are functionally equivalent.
        // We could if needed compare on an instance basic for instance with the host and port
        // Same for hashCode
        return this.getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
