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
