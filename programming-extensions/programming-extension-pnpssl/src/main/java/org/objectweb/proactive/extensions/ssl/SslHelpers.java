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

import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;


/**
 * A set of utily method to work with SSL and Bouncy Castle
 *
 * @since ProActive 5.0.0
 */
public class SslHelpers {
    /** The name of the bouncy castle security provider
     *
     * Shorter than {@link BouncyCastleProvider#PROVIDER_NAME}
     */
    static public String BC_NAME = BouncyCastleProvider.PROVIDER_NAME;

    /**
     * The default subject domain name of used/issued certificate
     */
    static public String DEFAULT_SUBJET_DN = "O=ProActive Parallel Suite, OU=Automatic certificate generator, CN=Certificate for ProActive";

    static public String DEFAULT_ALIAS_PATTERN = ".*";

    static public String DEFAULT_PROTOCOL = "TLSv1.2";

    /**
     * Insert Bouncy castle as a security provider if needed
     */
    static public void insertBouncyCastle() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

}
