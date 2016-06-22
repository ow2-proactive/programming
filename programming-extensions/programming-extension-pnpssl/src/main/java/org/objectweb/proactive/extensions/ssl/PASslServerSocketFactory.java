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
package org.objectweb.proactive.extensions.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;

import org.objectweb.proactive.extensions.pnpssl.PNPSslConfig;


/**
 * @since ProActive 5.0.0
 */
public class PASslServerSocketFactory extends SSLServerSocketFactory {

    private final SSLContext sslContext;

    private final SSLServerSocketFactory ssf;

    public PASslServerSocketFactory(SecureMode secureMode, KeyStore clientKs, X509Certificate[] acceptCert)
            throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException,
            CertificateException, IOException, KeyManagementException, UnrecoverableKeyException {

        //        // Create a dedicated in-memory keystore
        //        // the keys in the keystore are send to remote peer for ciphering & authentication
        //        KeyStore ks = KeyStore.getInstance("PKCS12", BouncyCastleProvider.PROVIDER_NAME);
        //        ks.load(null, null);
        //        for (X509Certificate cert : clientCerts) {
        //            ks.setKeyEntry(cert.getSubjectDN().toString(), cert.getPublicKey().getEncoded(), new X509Certificate[] {cert});
        //        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientKs, PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray());

        // Our custom trust managers are used to authenticate remote peers
        TrustManager[] tms;
        switch (secureMode) {
            case CIPHERED_ONLY:
                tms = new TrustManager[] { new PermissiveTrustManager() };
                break;
            case AUTH_AND_CIPHERED:
                tms = new TrustManager[] { new SameCertTrustManager(acceptCert) };
                break;
            default:
                throw new SecurityException("Unsupported secure mode");
        }

        this.sslContext = SSLContext.getInstance(SslHelpers.DEFAULT_PROTOCOL);
        this.sslContext.init(kmf.getKeyManagers(), tms, null);
        this.ssf = this.sslContext.getServerSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.ssf.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.ssf.getSupportedCipherSuites();
    }

    static private ServerSocket configureSocket(SSLServerSocket ss) {
        ss.setNeedClientAuth(true);
        ss.setUseClientMode(false);
        return ss;
    }

    @Override
    public ServerSocket createServerSocket() throws IOException {
        return configureSocket((SSLServerSocket) this.ssf.createServerSocket());
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return configureSocket((SSLServerSocket) this.ssf.createServerSocket(port));
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog) throws IOException {
        return configureSocket((SSLServerSocket) this.ssf.createServerSocket(port, backlog));
    }

    @Override
    public ServerSocket createServerSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        return configureSocket((SSLServerSocket) this.ssf.createServerSocket(port, backlog, ifAddress));
    }
}
