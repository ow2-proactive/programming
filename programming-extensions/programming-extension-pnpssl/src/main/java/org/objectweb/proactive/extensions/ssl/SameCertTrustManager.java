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

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * A custom trust manager authorizing only a given key
 *
 * The master certificate is a self signed certificate,
 * used only for a given application.
 *
 * A certificate is allowed if and only if:
 *   - Its pubkey is the same than the master certificate's pubkey
 *   - It  has been signed by the master certificate's pubkey
 *
 * @since ProActive 5.0.0
 */
public class SameCertTrustManager implements X509TrustManager {
    /** Authorized certificates  */
    final private X509Certificate[] authCerts;

    public SameCertTrustManager(X509Certificate[] authCerts) {
        this.authCerts = authCerts;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        // We don't care about the trust chain. We just want the same certificate
        checkTrusted(chain[chain.length - 1]);
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        checkTrusted(chain[chain.length - 1]);
    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    private void checkTrusted(X509Certificate cert) throws CertificateException {
        for (X509Certificate authCert : this.authCerts) {
            byte[] pk1 = cert.getPublicKey().getEncoded();
            byte[] pk2 = authCert.getPublicKey().getEncoded();
            if (Arrays.equals(pk1, pk2)) {
                try {
                    cert.verify(authCert.getPublicKey(), BouncyCastleProvider.PROVIDER_NAME);
                    return;
                } catch (GeneralSecurityException e) {
                    // Ok
                }
            }
        }

        throw new CertificateException(cert.getSubjectDN() +
                                       " public key does not match the master certificate public key");
    }
}
