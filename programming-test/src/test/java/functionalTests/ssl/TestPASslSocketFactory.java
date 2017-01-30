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
package functionalTests.ssl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLHandshakeException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.pnpssl.PNPSslConfig;
import org.objectweb.proactive.extensions.ssl.PASslServerSocketFactory;
import org.objectweb.proactive.extensions.ssl.PASslSocketFactory;
import org.objectweb.proactive.extensions.ssl.SecureMode;


/**
 * @since ProActive 5.0.0
 */
public class TestPASslSocketFactory extends AbstractSSL {
    static String PAYLOAD = "Chuck Norris can touch MC Hammer";

    // Generating certificate is an expensive process, so precompute some
    static KeyPairAndCert[] kpAndCerts;

    @BeforeClass
    static public void localBeforeClass() throws Exception {
        kpAndCerts = generateCertificates(3);
    }

    public void testSslSocket() throws Exception {
    }

    /**
     * Check that client and server are able to communicate:
     * <ul>
     * <li>Chipering only, no authentication</li>
     * <li>Client and Server uses the same keystore  and certificate</li>
     * </ul>
     */
    @Test
    public void testChiperedOnlyOkSameKS() throws Exception {
        KeyStore clientKs = KeyStore.getInstance("PKCS12");
        clientKs.load(null, null);
        clientKs.setKeyEntry(kpAndCerts[0].cert.getSubjectDN().toString(),
                             kpAndCerts[0].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[0].getCertAsCertArray());
        X509Certificate[] clientTrustedCerts = kpAndCerts[0].getCertAsCertArray();

        KeyStore serverKs = clientKs;
        X509Certificate[] serverTrustedCerts = clientTrustedCerts;

        connectAndExchange(SecureMode.CIPHERED_ONLY, clientKs, clientTrustedCerts, serverKs, serverTrustedCerts);
    }

    /**
     * Check that client and server are able to communicate:
     * <ul>
     * <li>Chipering only, no authentication</li>
     * <li>Client and Server uses a different keystore  and certificate</li>
     * <li>Client and Server does not know each other.</li>
     * </ul>
     */
    @Test
    public void testChiperedOnlyOkDifferentKS() throws Exception {
        KeyStore clientKs = KeyStore.getInstance("PKCS12");
        clientKs.load(null, null);
        clientKs.setKeyEntry(kpAndCerts[0].cert.getSubjectDN().toString(),
                             kpAndCerts[0].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[0].getCertAsCertArray());
        X509Certificate[] clientTrustedCerts = kpAndCerts[0].getCertAsCertArray();

        KeyStore serverKs = KeyStore.getInstance("PKCS12");
        serverKs.load(null, null);
        serverKs.setKeyEntry(kpAndCerts[1].cert.getSubjectDN().toString(),
                             kpAndCerts[1].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[1].getCertAsCertArray());
        X509Certificate[] serverTrustedCerts = kpAndCerts[1].getCertAsCertArray();

        connectAndExchange(SecureMode.CIPHERED_ONLY, clientKs, clientTrustedCerts, serverKs, serverTrustedCerts);

    }

    /**
     * Check that client and server are able to communicate:
     * <ul>
     * <li>Chipering and authentication</li>
     * <li>Client and Server uses the same keystore  and truststore</li>
     * </ul>
     */
    @Test
    public void testAuthAndChiperedOkSameKsAndCert() throws Exception {
        KeyStore clientKs = KeyStore.getInstance("PKCS12");
        clientKs.load(null, null);
        clientKs.setKeyEntry(kpAndCerts[0].cert.getSubjectDN().toString(),
                             kpAndCerts[0].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[0].getCertAsCertArray());
        X509Certificate[] clientTrustedCerts = kpAndCerts[0].getCertAsCertArray();

        KeyStore serverKs = clientKs;
        X509Certificate[] serverTrustedCerts = clientTrustedCerts;

        connectAndExchange(SecureMode.AUTH_AND_CIPHERED, clientKs, clientTrustedCerts, serverKs, serverTrustedCerts);
    }

    /**
     * Check that client and server are able to communicate:
     * <ul>
     * <li>Chipering and authentication</li>
     * <li>Client and Server uses cross credential. server has client cert in it's truststore and vice versa</li>
     * </ul>
     */
    @Test
    public void testAuthAndChiperedOkCrossCredential() throws Exception {
        KeyStore clientKs = KeyStore.getInstance("PKCS12");
        clientKs.load(null, null);
        clientKs.setKeyEntry(kpAndCerts[0].cert.getSubjectDN().toString(),
                             kpAndCerts[0].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[0].getCertAsCertArray());
        X509Certificate[] clientTrustedCerts = kpAndCerts[1].getCertAsCertArray();

        KeyStore serverKs = KeyStore.getInstance("PKCS12");
        serverKs.load(null, null);
        serverKs.setKeyEntry(kpAndCerts[1].cert.getSubjectDN().toString(),
                             kpAndCerts[1].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[1].getCertAsCertArray());
        X509Certificate[] serverTrustedCerts = kpAndCerts[0].getCertAsCertArray();

        connectAndExchange(SecureMode.AUTH_AND_CIPHERED, clientKs, clientTrustedCerts, serverKs, serverTrustedCerts);
    }

    /**
     * Client does not allows server and server does not allows client
     */
    @Test(expected = SSLHandshakeException.class)
    public void testAuthAndChiperedNOK() throws Exception {
        KeyStore clientKs = KeyStore.getInstance("PKCS12");
        clientKs.load(null, null);
        clientKs.setKeyEntry(kpAndCerts[0].cert.getSubjectDN().toString(),
                             kpAndCerts[0].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[0].getCertAsCertArray());
        X509Certificate[] clientTrustedCerts = kpAndCerts[0].getCertAsCertArray();

        KeyStore serverKs = KeyStore.getInstance("PKCS12");
        serverKs.load(null, null);
        serverKs.setKeyEntry(kpAndCerts[1].cert.getSubjectDN().toString(),
                             kpAndCerts[1].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[1].getCertAsCertArray());
        X509Certificate[] serverTrustedCerts = kpAndCerts[1].getCertAsCertArray();

        connectAndExchange(SecureMode.AUTH_AND_CIPHERED, clientKs, clientTrustedCerts, serverKs, serverTrustedCerts);

    }

    /**
     * Client does not allows server
     */
    @Test(expected = SSLHandshakeException.class)
    public void testAuthAndChiperedNOKClientRejectServer() throws Exception {
        KeyStore clientKs = KeyStore.getInstance("PKCS12");
        clientKs.load(null, null);
        clientKs.setKeyEntry(kpAndCerts[0].cert.getSubjectDN().toString(),
                             kpAndCerts[0].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[0].getCertAsCertArray());
        X509Certificate[] clientTrustedCerts = kpAndCerts[2].getCertAsCertArray();

        KeyStore serverKs = KeyStore.getInstance("PKCS12");
        serverKs.load(null, null);
        serverKs.setKeyEntry(kpAndCerts[1].cert.getSubjectDN().toString(),
                             kpAndCerts[1].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[1].getCertAsCertArray());
        X509Certificate[] serverTrustedCerts = kpAndCerts[0].getCertAsCertArray();

        connectAndExchange(SecureMode.AUTH_AND_CIPHERED, clientKs, clientTrustedCerts, serverKs, serverTrustedCerts);

    }

    /**
     * Server does not allows client
     */
    @Test(expected = SSLHandshakeException.class)
    public void testAuthAndChiperedNOKServerRejectClient() throws Exception {
        KeyStore clientKs = KeyStore.getInstance("PKCS12");
        clientKs.load(null, null);
        clientKs.setKeyEntry(kpAndCerts[0].cert.getSubjectDN().toString(),
                             kpAndCerts[0].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[0].getCertAsCertArray());
        X509Certificate[] clientTrustedCerts = kpAndCerts[1].getCertAsCertArray();

        KeyStore serverKs = KeyStore.getInstance("PKCS12");
        serverKs.load(null, null);
        serverKs.setKeyEntry(kpAndCerts[1].cert.getSubjectDN().toString(),
                             kpAndCerts[1].getPrivateKey(),
                             PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                             kpAndCerts[1].getCertAsCertArray());
        X509Certificate[] serverTrustedCerts = kpAndCerts[2].getCertAsCertArray();

        connectAndExchange(SecureMode.AUTH_AND_CIPHERED, clientKs, clientTrustedCerts, serverKs, serverTrustedCerts);

    }

    private void connectAndExchange(final SecureMode sm, final KeyStore clientKs,
            final X509Certificate[] clientTrustedCerts, final KeyStore serverKs,
            final X509Certificate[] serverTrustedCerts) throws Exception {

        PASslServerSocketFactory ssf;
        ssf = new PASslServerSocketFactory(sm, serverKs, serverTrustedCerts);
        final ServerSocket serverSocket = ssf.createServerSocket();
        serverSocket.bind(null);

        final AtomicReference<Throwable> clientError = new AtomicReference<Throwable>(null);
        Thread clientThread = new Thread() {

            public void run() {
                try {
                    PASslSocketFactory sf = new PASslSocketFactory(sm, clientKs, clientTrustedCerts);
                    Socket client = sf.createSocket("localhost", serverSocket.getLocalPort());

                    client.getOutputStream().write(PAYLOAD.getBytes());
                    client.close();
                } catch (Throwable t) {
                    clientError.set(t);
                }
            }
        };
        clientThread.start();

        Socket server = serverSocket.accept();
        BufferedReader br = new BufferedReader(new InputStreamReader(server.getInputStream()));
        String rPayload = br.readLine();

        br.close();
        serverSocket.close();

        Assert.assertNull(clientError.get());
        Assert.assertEquals(PAYLOAD, rPayload);
    }
}
