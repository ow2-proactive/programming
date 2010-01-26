/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.security;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import functionalTests.security.applicationlifecycle.SecurityTestApplicationLifeCycle;
import functionalTests.security.dynamicsecuritypropagation.SecurityTestContextPropagation;
import functionalTests.security.keygeneration.SecurityTestKeyGen;
import functionalTests.security.policyserver.SecurityTestPolicyServer;
import functionalTests.security.securitymanager.SecurityTestSecurityManager;
import functionalTests.security.sessionkeyexchange.SecurityTestSessionKeyExchange;
import static junit.framework.Assert.assertTrue;


@RunWith(Suite.class)
@SuiteClasses( { SecurityTestKeyGen.class, SecurityTestPolicyServer.class, SecurityTestSecurityManager.class,
        SecurityTestSessionKeyExchange.class, SecurityTestApplicationLifeCycle.class,
        SecurityTestContextPropagation.class })
public class TestUnlimitedCrypto {
    static private final int KSIZE = 4096;

    @BeforeClass
    public static void checkUnlimitedCryptoIsAvailable() throws Exception {
        java.security.Security.addProvider(new BouncyCastleProvider());

        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(KSIZE);
            KeyPair key = keyGen.generateKeyPair();

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");

            // encrypt the plaintext using the public key
            cipher.init(Cipher.ENCRYPT_MODE, key.getPublic());
            cipher.doFinal(new byte[50]);
        } catch (InvalidKeyException e) {
            System.err
                    .println("Strong Juridiction Policy Files detected, please install the Unlimited Juridiction Policy Files to be able to use ProActive' Security Framework");
            assertTrue(false);
        }
    }
}
