/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Arrays;


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
            if (Arrays.areEqual(pk1, pk2)) {
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