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

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Vector;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;


/**
 *
 *
 * @since ProActive 5.0.0
 */
public class CertificateGenerator {

    /**
     * Create a random, self signed, one time certificate
     *
     * A such certificate can be used to take advantage of the SSL/TLS encryption
     * feature without requiring any action from the user.
     *
     * A self signed certificate, valid for the next 10 year is issued.
     *
     * @return
     */
    public X509Certificate generateCertificate(String subjectDN, KeyPair pair) throws SslException {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

        // Auto-generated certificate, use a default principal
        X500Principal defaultPrincipal;
        defaultPrincipal = new X500Principal(subjectDN);
        certGen.setIssuerDN(defaultPrincipal);
        certGen.setSubjectDN(defaultPrincipal);

        // Valid for the next few years
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 10000));
        certGen.setNotAfter(new Date(System.currentTimeMillis() + (10 * 365 * 24 * 60)));

        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));

        certGen.setPublicKey(pair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

        // Not certified by a CA
        certGen.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));

        // SSL requires signiture & encipherment
        KeyUsage keyUsage = new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment);
        certGen.addExtension(X509Extensions.KeyUsage, true, keyUsage);

        // Allow client and server authentication
        Vector<ASN1Object> extendedKeyUsageV = new Vector<>();
        extendedKeyUsageV.add(KeyPurposeId.id_kp_serverAuth);
        extendedKeyUsageV.add(KeyPurposeId.id_kp_clientAuth);
        certGen.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(extendedKeyUsageV));

        try {
            X509Certificate cert = certGen.generate(pair.getPrivate(), BouncyCastleProvider.PROVIDER_NAME);
            try {
                cert.checkValidity();
                cert.verify(pair.getPublic());
            } catch (GeneralSecurityException e) {
                throw new SslException("Generated certificate is not valid", e);
            }

            return cert;
        } catch (GeneralSecurityException e) {
            throw new SslException("Failed to generate certificate", e);
        }
    }

    public KeyPair generateRSAKeyPair() throws SslException {
        final String ALGORITHM = "RSA";
        try {
            // Don't use NativePRNG since it will drain the system entropy pool
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            KeyPairGenerator kpGen = KeyPairGenerator.getInstance(ALGORITHM, SslHelpers.BC_NAME);
            kpGen.initialize(2048, sr);
            KeyPair kp = kpGen.generateKeyPair();
            return kp;
        } catch (NoSuchAlgorithmException e) {
            throw new SslException("Failed to generate an RSA key pair. Unknow algorithm " + ALGORITHM, e);
        } catch (NoSuchProviderException e) {
            throw new SslException("Failed to generate an RSA key pair. Bad provider: " + SslHelpers.BC_NAME, e);
        }
    }
}
