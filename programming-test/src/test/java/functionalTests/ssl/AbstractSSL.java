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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.junit.BeforeClass;
import org.objectweb.proactive.extensions.ssl.CertificateGenerator;
import org.objectweb.proactive.extensions.ssl.SslHelpers;

import functionalTests.FunctionalTest;


/**
 *
 *
 * @since ProActive 5.0.0
 */
public abstract class AbstractSSL extends FunctionalTest {

    @BeforeClass
    static public void beforeClass() {
        SslHelpers.insertBouncyCastle();
    }

    static public KeyPairAndCert[] generateCertificates(int nb) throws Exception {
        KeyPairAndCert[] ar = new KeyPairAndCert[nb];
        CertificateGenerator gen = new CertificateGenerator();

        for (int i = 0; i < nb; i++) {
            KeyPair kp = gen.generateRSAKeyPair();
            X509Certificate cert = gen.generateCertificate(SslHelpers.DEFAULT_SUBJET_DN + "i", kp);
            ar[i] = new KeyPairAndCert(kp, cert);
        }

        return ar;
    }

    public static class KeyPairAndCert {
        final KeyPair keypair;

        final X509Certificate cert;

        public KeyPairAndCert(KeyPair keyPair, X509Certificate cert) {
            this.keypair = keyPair;
            this.cert = cert;
        }

        public X509Certificate[] getCertAsCertArray() {
            return new X509Certificate[] { this.cert };
        }

        public PrivateKey getPrivateKey() {
            return keypair.getPrivate();
        }
    }
}
