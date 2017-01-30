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

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.pnpssl.PNPSslConfig;


/**
 * This class is implemented as a child of SSLEngine instead of a factory because ProActive
 * is not designed to support class injection. It is easier to replace this PASslEngine by
 * another SSLEngine by using inheritance than using an hard coded factory class.
 *
 * @since ProActive 5.0.0
 */
public class PASslEngine extends SSLEngine {
    static final private String[] STRONG_CIPHERS = { "SSL_RSA_WITH_RC4_128_MD5", "SSL_RSA_WITH_RC4_128_SHA",
                                                     "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                                                     "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA",
                                                     "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA",
                                                     "TLS_DHE_RSA_WITH_AES_128_CBC_SHA",
                                                     "TLS_DHE_DSS_WITH_AES_128_CBC_SHA", "TLS_KRB5_WITH_RC4_128_MD5",
                                                     "TLS_KRB5_WITH_RC4_128_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA",
                                                     "TLS_KRB5_WITH_3DES_EDE_CBC_MD5", "TLS_KRB5_WITH_3DES_EDE_CBC_SHA",
                                                     "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
                                                     "TLS_DHE_DSS_WITH_AES_256_CBC_SHA",
                                                     "TLS_RSA_WITH_AES_256_CBC_SHA" };

    final private SSLEngine sslEngine;

    public PASslEngine(boolean client, SecureMode secureMode, KeyStore keystore, TrustManager trustManager) {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore, PNPSslConfig.PA_PNPSSL_KEYSTORE_PASSWORD.getValue().toCharArray());

            // Initialize the SSLContext to work with our key managers.
            SSLContext ctxt = SSLContext.getInstance(SslHelpers.DEFAULT_PROTOCOL);
            ctxt.init(kmf.getKeyManagers(), new TrustManager[] { trustManager }, null);

            this.sslEngine = ctxt.createSSLEngine();
            this.sslEngine.setEnabledProtocols(new String[] { SslHelpers.DEFAULT_PROTOCOL });
            this.sslEngine.setEnableSessionCreation(true);
            String[] supportedCiphers = this.sslEngine.getSupportedCipherSuites();
            this.sslEngine.setEnabledCipherSuites(this.getEnabledCiphers(supportedCiphers, STRONG_CIPHERS));
            if (client) {
                this.sslEngine.setUseClientMode(true);
            } else {
                this.sslEngine.setUseClientMode(false);
            }
            switch (secureMode) {
                case CIPHERED_ONLY:
                    this.sslEngine.setNeedClientAuth(false);
                    break;
                case AUTH_AND_CIPHERED:
                    this.sslEngine.setNeedClientAuth(true);
                    break;
                default:
                    throw new ProActiveRuntimeException("Unsupported secure mode: " + secureMode);
            }
        } catch (GeneralSecurityException e) {
            throw new ProActiveRuntimeException("failed to initialize " + this.getClass().getName(), e);
        }
    }

    /**
     * Filter out the list of supported cipher to retain only the strong ones.
     *
     * @param supportedCiphers List of ciphers supported by the ssl engine
     * @param wantedCiphers    List of cipher considered as strong enough to be used
     * @return List of supported and strong enough ciphers
     */
    private String[] getEnabledCiphers(String[] supportedCiphers, String[] wantedCiphers) {
        Set<String> enabled = new HashSet<String>(wantedCiphers.length);

        for (String wanted : wantedCiphers) {
            for (String supported : supportedCiphers) {
                if (wanted.equals(supported)) {
                    enabled.add(wanted);
                }
            }
        }

        return enabled.toArray(new String[enabled.size()]);
    }

    @Override
    public SSLEngineResult wrap(ByteBuffer[] srcs, int offset, int length, ByteBuffer dst) throws SSLException {
        return this.sslEngine.wrap(srcs, offset, length, dst);
    }

    @Override
    public SSLEngineResult unwrap(ByteBuffer src, ByteBuffer[] dsts, int offset, int length) throws SSLException {
        return this.sslEngine.unwrap(src, dsts, offset, length);
    }

    @Override
    public Runnable getDelegatedTask() {
        return this.sslEngine.getDelegatedTask();
    }

    @Override
    public void closeInbound() throws SSLException {
        this.sslEngine.closeInbound();
    }

    @Override
    public boolean isInboundDone() {
        return this.sslEngine.isInboundDone();
    }

    @Override
    public void closeOutbound() {
        this.sslEngine.closeOutbound();
    }

    @Override
    public boolean isOutboundDone() {
        return this.sslEngine.isOutboundDone();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return this.sslEngine.getSupportedCipherSuites();
    }

    @Override
    public String[] getEnabledCipherSuites() {
        return this.sslEngine.getEnabledCipherSuites();
    }

    @Override
    public void setEnabledCipherSuites(String[] suites) {
        this.sslEngine.setEnabledCipherSuites(suites);
    }

    @Override
    public String[] getSupportedProtocols() {
        return this.sslEngine.getSupportedProtocols();
    }

    @Override
    public String[] getEnabledProtocols() {
        return this.sslEngine.getEnabledProtocols();
    }

    @Override
    public void setEnabledProtocols(String[] protocols) {
        this.sslEngine.setEnabledProtocols(protocols);
    }

    @Override
    public SSLSession getSession() {
        return this.sslEngine.getSession();
    }

    @Override
    public void beginHandshake() throws SSLException {
        this.sslEngine.beginHandshake();
    }

    @Override
    public HandshakeStatus getHandshakeStatus() {
        return this.sslEngine.getHandshakeStatus();
    }

    @Override
    public void setUseClientMode(boolean mode) {
        this.sslEngine.setUseClientMode(mode);
    }

    @Override
    public boolean getUseClientMode() {
        return this.sslEngine.getUseClientMode();
    }

    @Override
    public void setNeedClientAuth(boolean need) {
        this.sslEngine.setNeedClientAuth(need);
    }

    @Override
    public boolean getNeedClientAuth() {
        return this.sslEngine.getNeedClientAuth();
    }

    @Override
    public void setWantClientAuth(boolean want) {
        this.sslEngine.setWantClientAuth(want);
    }

    @Override
    public boolean getWantClientAuth() {
        return this.sslEngine.getWantClientAuth();
    }

    @Override
    public void setEnableSessionCreation(boolean flag) {
        this.sslEngine.setEnableSessionCreation(flag);
    }

    @Override
    public boolean getEnableSessionCreation() {
        return this.sslEngine.getEnableSessionCreation();
    }

}
