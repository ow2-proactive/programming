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
package org.objectweb.proactive.extensions.pnpssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.TrustManager;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pnp.PNPConfig;
import org.objectweb.proactive.extensions.pnp.PNPExtraHandlers;
import org.objectweb.proactive.extensions.pnp.PNPRemoteObjectFactoryAbstract;
import org.objectweb.proactive.extensions.pnp.PNPRemoteObjectFactoryBackend;
import org.objectweb.proactive.extensions.ssl.CertificateGenerator;
import org.objectweb.proactive.extensions.ssl.PermissiveTrustManager;
import org.objectweb.proactive.extensions.ssl.SameCertTrustManager;
import org.objectweb.proactive.extensions.ssl.SecureMode;
import org.objectweb.proactive.extensions.ssl.SslException;
import org.objectweb.proactive.extensions.ssl.SslHelpers;


/**
 * The PNP over SSL remote object factory
 *
 * @since ProActive 5.0.0
 */
public class PNPSslRemoteObjectFactory extends PNPRemoteObjectFactoryAbstract {
    static final Logger logger = ProActiveLogger.getLogger(PNPSslConfig.Loggers.PNPSSL);

    static final public String PROTO_ID = "pnps";

    public PNPSslRemoteObjectFactory() throws PNPSslException {
        SslHelpers.insertBouncyCastle();

        // Handle standard pnp options
        PNPConfig config = new PNPConfig();
        config.setPort(PNPSslConfig.PA_PNPSSL_PORT.getValue());
        config.setIdleTimeout(PNPSslConfig.PA_PNPSSL_IDLE_TIMEOUT.getValue());
        config.setDefaultHeartbeat(PNPSslConfig.PA_PNPSSL_DEFAULT_HEARTBEAT.getValue());
        config.setHeartbeatFactor(PNPSslConfig.PA_PNPSSL_HEARTBEAT_FACTOR.getValue());
        config.setHeartbeatWindow(PNPSslConfig.PA_PNPSSL_HEARTBEAT_WINDOW.getValue());

        if (PNPSslConfig.PA_PNPSSL_AUTHENTICATE.isTrue() && !PNPSslConfig.PA_PNPSSL_KEYSTORE.isSet()) {
            throw new PNPSslConfigurationException(PNPSslConfig.PA_PNPSSL_KEYSTORE.getName() +
                                                   " property must be set when " +
                                                   PNPSslConfig.PA_PNPSSL_AUTHENTICATE.getName() + " is true");
        }

        KeyStore ks = getKeystore();
        TrustManager tm = getTrustManager(ks);
        SecureMode sm = getSecureMode();

        PNPExtraHandlers extraHandlers = new PNPSslExtraHandlers(sm, ks, tm);

        final PNPRemoteObjectFactoryBackend rof;
        rof = new PNPRemoteObjectFactoryBackend(PROTO_ID, config, extraHandlers);
        this.setBackendRemoteObjectFactory(rof);
    }

    private SecureMode getSecureMode() {
        if (PNPSslConfig.PA_PNPSSL_AUTHENTICATE.isTrue()) {
            return SecureMode.AUTH_AND_CIPHERED;
        } else {
            return SecureMode.CIPHERED_ONLY;
        }
    }

    private TrustManager getTrustManager(final KeyStore ks) throws PNPSslException {
        if (PNPSslConfig.PA_PNPSSL_AUTHENTICATE.isTrue()) {
            try {
                Enumeration<String> aliases = ks.aliases();
                List<Certificate> certs = new LinkedList<Certificate>();
                while (aliases.hasMoreElements()) {
                    String alias = (String) aliases.nextElement();
                    if (alias.matches(SslHelpers.DEFAULT_ALIAS_PATTERN)) {
                        certs.add(ks.getCertificate(alias));
                    }
                }

                if (certs.size() == 0) {
                    throw new PNPSslException("No certificate matching \"" + SslHelpers.DEFAULT_ALIAS_PATTERN +
                                              "\" found in the keystore " + ks + ". Cannot enable authenticate mode");
                }

                return new SameCertTrustManager(certs.toArray(new X509Certificate[certs.size()]));
            } catch (KeyStoreException e) {
                throw new PNPSslException("Failed to list certificates in the keystore " + ks, e);
            }
        } else {
            return new PermissiveTrustManager();
        }
    }

    private KeyStore getKeystore() throws PNPSslException {
        if (PNPSslConfig.PA_PNPSSL_KEYSTORE.isSet()) {
            return readKeystoreFromDisk(PNPSslConfig.PA_PNPSSL_KEYSTORE.getValue());
        } else {
            if (PNPSslConfig.PA_PNPSSL_AUTHENTICATE.isTrue()) {
                logger.error(PROTO_ID + " configured to authenticate remote runtimes but keystore is not set. " +
                             PROTO_ID + "will not work");
            }

            return createKeystore();
        }
    }

    private KeyStore readKeystoreFromDisk(String url) throws PNPSslConfigurationException {
        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            File f = new File(PNPSslConfig.PA_PNPSSL_KEYSTORE.getValue());
            FileInputStream fis = new FileInputStream(f);
            ks.load(fis, PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray());
            return ks;
        } catch (KeyStoreException e) {
            throw new PNPSslConfigurationException("Failed to create keystore", e);
        } catch (FileNotFoundException e) {
            throw new PNPSslConfigurationException("Failed to read user specifed keystore for " + PROTO_ID, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PNPSslConfigurationException("Failed to load user specified keystore for " + PROTO_ID, e);
        } catch (CertificateException e) {
            throw new PNPSslConfigurationException("Failed to load a certificate in the user specified keystore for " +
                                                   PROTO_ID, e);
        } catch (IOException e) {
            throw new PNPSslConfigurationException("Failed to load user specified keystore for " + PROTO_ID, e);
        }

    }

    private KeyStore createKeystore() throws PNPSslException {
        try {
            CertificateGenerator gen = new CertificateGenerator();
            KeyPair pair = gen.generateRSAKeyPair();
            X509Certificate cert = gen.generateCertificate(SslHelpers.DEFAULT_SUBJET_DN, pair);

            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.setKeyEntry(SslHelpers.DEFAULT_SUBJET_DN,
                           pair.getPrivate(),
                           PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray(),
                           new X509Certificate[] { cert });
            return ks;
        } catch (KeyStoreException e) {
            throw new PNPSslConfigurationException("Failed to create or fill the keystore for " + PROTO_ID, e);
        } catch (NoSuchAlgorithmException e) {
            throw new PNPSslConfigurationException("Failed to create the keystore for " + PROTO_ID, e);
        } catch (CertificateException e) {
            throw new PNPSslException("Failed to load a certificate in the user specified keystore for " + PROTO_ID, e);
        } catch (IOException e) {
            throw new PNPSslConfigurationException("Failed to load user specified keystore for " + PROTO_ID, e);
        } catch (SslException e) {
            throw new PNPSslConfigurationException("Failed to create a certificate for " + PROTO_ID, e);
        }
    }
}
