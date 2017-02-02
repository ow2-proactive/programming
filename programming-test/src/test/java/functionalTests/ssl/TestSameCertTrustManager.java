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

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.ssl.SameCertTrustManager;


/**
 *
 *
 * @since ProActive 5.0.0
 */
public class TestSameCertTrustManager extends AbstractSSL {

    // Generating certificate is an expensive process, so precompute some
    static KeyPairAndCert[] chain;

    @BeforeClass
    static public void localBeforeClass() throws Exception {
        chain = generateCertificates(3);
    }

    @Test
    public void testSimpleOK() throws Exception {
        SameCertTrustManager tm = new SameCertTrustManager(new X509Certificate[] { chain[0].cert });
        tm.checkClientTrusted(new X509Certificate[] { chain[0].cert }, "");
        tm.checkServerTrusted(new X509Certificate[] { chain[0].cert }, "");
    }

    @Test
    public void testSeveralOK() throws Exception {
        SameCertTrustManager tm = new SameCertTrustManager(new X509Certificate[] { chain[0].cert, chain[1].cert });

        tm.checkClientTrusted(new X509Certificate[] { chain[0].cert }, "");
        tm.checkServerTrusted(new X509Certificate[] { chain[0].cert }, "");

        tm.checkClientTrusted(new X509Certificate[] { chain[1].cert }, "");
        tm.checkServerTrusted(new X509Certificate[] { chain[1].cert }, "");
    }

    @Test(expected = CertificateException.class)
    public void testNOK() throws Exception {
        SameCertTrustManager tm = new SameCertTrustManager(new X509Certificate[] { chain[0].cert, chain[1].cert });

        tm.checkClientTrusted(new X509Certificate[] { chain[2].cert }, "");
        tm.checkServerTrusted(new X509Certificate[] { chain[2].cert }, "");
    }
}
