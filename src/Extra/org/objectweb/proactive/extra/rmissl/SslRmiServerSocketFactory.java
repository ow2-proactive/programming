package org.objectweb.proactive.extra.rmissl;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


/**
 * @since ProActive 4.2.0
 */
public class SslRmiServerSocketFactory implements RMIServerSocketFactory, java.io.Serializable {

    private static SSLServerSocketFactory sslServerSocketFactory = null;

    static {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, null, new java.security.SecureRandom());
            sslServerSocketFactory = sc.getServerSocketFactory();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public ServerSocket createServerSocket(int port) throws IOException {
        SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(port);
        serverSocket.setEnabledCipherSuites(SSLCONSTANTS.enabled_ciphers);
        return serverSocket;
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
