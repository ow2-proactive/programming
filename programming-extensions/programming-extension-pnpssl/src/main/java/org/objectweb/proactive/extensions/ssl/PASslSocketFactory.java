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
package org.objectweb.proactive.extensions.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnpssl.PNPSslConfig;


/**
 * @since ProActive 5.0.0
 */
public class PASslSocketFactory extends SSLSocketFactory {

    private final SSLContext sslContext;

    private final SSLSocketFactory sf;

    public PASslSocketFactory(SecureMode secureMode, KeyStore clientKs, X509Certificate[] acceptCert)
            throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException,
            IOException, KeyManagementException, UnrecoverableKeyException {

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

        this.sslContext = SSLContext.getInstance(PNPSslConfig.PA_PNPSSL_PROTOCOL.getValue());
        this.sslContext.init(kmf.getKeyManagers(), tms, null);

        this.sf = this.sslContext.getSocketFactory();
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return this.sf.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.sf.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return this.sf.createSocket(s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return this.sf.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException, UnknownHostException {
        return this.sf.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.sf.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return this.sf.createSocket(address, port, localAddress, localPort);
    }

}
