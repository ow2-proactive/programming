/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
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
 * @since ProActive 4.4.0
 */
public class TestSameCertTrustManager extends SSLTest {

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
        SameCertTrustManager tm = new SameCertTrustManager(new X509Certificate[] { chain[0].cert,
                chain[1].cert });

        tm.checkClientTrusted(new X509Certificate[] { chain[0].cert }, "");
        tm.checkServerTrusted(new X509Certificate[] { chain[0].cert }, "");

        tm.checkClientTrusted(new X509Certificate[] { chain[1].cert }, "");
        tm.checkServerTrusted(new X509Certificate[] { chain[1].cert }, "");
    }

    @Test(expected = CertificateException.class)
    public void testNOK() throws Exception {
        SameCertTrustManager tm = new SameCertTrustManager(new X509Certificate[] { chain[0].cert,
                chain[1].cert });

        tm.checkClientTrusted(new X509Certificate[] { chain[2].cert }, "");
        tm.checkServerTrusted(new X509Certificate[] { chain[2].cert }, "");
    }
}
