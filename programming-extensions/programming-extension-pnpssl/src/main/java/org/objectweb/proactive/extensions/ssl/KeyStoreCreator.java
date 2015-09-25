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
package org.objectweb.proactive.extensions.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.objectweb.proactive.utils.PasswordField;


/**
 * Create a keystore to be used by SSL enabled communication protocols
 *
 * @since ProActive 5.0.0
 */
public class KeyStoreCreator {
    private final String OPT_HELP[] = new String[] { "h", "help", "Show help" };
    private final String OPT_KEYSTORE[] = new String[] { "k", "keystore", "The keystore file" };
    private final String OPT_CREATE[] = new String[] { "c", "create",
            "Create a keystore with a self signed certificate" };
    private final String OPT_UPDATE[] = new String[] { "u", "update",
            "Update the certificate inside the keystore" };
    private final String OPT_VERIFY[] = new String[] { "v", "verify",
            "Verify the certificate with right subject dn can be found" };

    public static void main(String[] args) throws Exception {
        SslHelpers.insertBouncyCastle();

        KeyStoreCreator ksc = new KeyStoreCreator();
        ksc.parseOptions(args);
    }

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("KeyStoreCreator", options);
        System.exit(0);
    }

    public void parseOptions(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(OPT_HELP[0], OPT_HELP[1], false, OPT_HELP[2]);
        options.addOption(OPT_KEYSTORE[0], OPT_KEYSTORE[1], true, OPT_KEYSTORE[2]);

        OptionGroup group = new OptionGroup();
        group.addOption(new Option(OPT_CREATE[0], OPT_CREATE[1], false, OPT_CREATE[2]));
        group.addOption(new Option(OPT_UPDATE[0], OPT_UPDATE[1], false, OPT_UPDATE[2]));
        group.addOption(new Option(OPT_VERIFY[0], OPT_VERIFY[1], false, OPT_VERIFY[2]));
        options.addOptionGroup(group);

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(OPT_HELP[0])) {
                printHelp(options);
            }

            String keyStore = null;
            if (cmd.hasOption(OPT_KEYSTORE[0])) {
                keyStore = cmd.getOptionValue(OPT_KEYSTORE[0]);
            } else {
                System.err.println("The " + OPT_KEYSTORE[1] + " option is mandatory");
                return;
            }

            boolean hasAction = false;
            if (cmd.hasOption(OPT_CREATE[0])) {
                hasAction = true;
                if (!this.create(keyStore)) {
                    System.exit(1);
                }
            }

            if (cmd.hasOption(OPT_UPDATE[0])) {
                hasAction = true;
                if (!this.update(keyStore)) {
                    System.exit(1);
                }
            }

            if (cmd.hasOption(OPT_VERIFY[0])) {
                hasAction = true;
                if (!this.verify(keyStore)) {
                    System.exit(1);
                }
            }

            if (!hasAction) {
                System.err.println("One of " + OPT_CREATE[1] + ", " + OPT_UPDATE[1] + ", " + OPT_VERIFY[1] +
                    " has is needed\n");
                printHelp(options);
            }
        } catch (ParseException e) {
            System.err.println(e);
        }
    }

    private boolean verify(String keyStore) {
        // Load the keystore
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(keyStore);
        } catch (FileNotFoundException e) {
            System.err.println("Failed to open the key store: " + e);
            return false;
        }

        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("PKCS12", SslHelpers.BC_NAME);
            ks.load(fis, SslHelpers.DEFAULT_KS_PASSWD.toCharArray());
        } catch (Exception e) {
            System.err.println("Failed to open the key store: " + e);
            return false;
        }

        try {

            Enumeration<String> aliases = ks.aliases();

            List<Certificate> matchingCerts = new LinkedList<Certificate>();
            List<Certificate> otherCerts = new LinkedList<Certificate>();

            while (aliases.hasMoreElements()) {
                String alias = (String) aliases.nextElement();
                //                if (ks.isCertificateEntry(alias)) {
                if (alias.matches(SslHelpers.DEFAULT_ALIAS_PATTERN)) {
                    matchingCerts.add(ks.getCertificate(alias));
                } else {
                    otherCerts.add(ks.getCertificate(alias));
                }
                //                }

                if (matchingCerts.size() > 0) {
                    System.out.println(matchingCerts.size() + " matching certificate found");
                    for (Certificate cert : matchingCerts) {
                        System.out.println(cert);
                    }
                } else {
                    System.err.println("No matching certificate foud. " + otherCerts.size() +
                        " non matching certificate found.");
                    return false;
                }
            }
        } catch (KeyStoreException e) {
            // Should not happen. Only throwed if the keystore is not initialized
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private boolean update(String keyStore) {
        // Load the keystore
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(keyStore);
        } catch (FileNotFoundException e) {
            System.err.println("Failed to open the key store: " + e);
            return false;
        }

        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance("PKCS12", SslHelpers.BC_NAME);
            ks.load(fis, SslHelpers.DEFAULT_KS_PASSWD.toCharArray());
        } catch (Exception e) {
            System.err.println("Failed to open the key store: " + e);
            return false;
        }

        try {
            // Create a certificate
            CertificateGenerator gen = new CertificateGenerator();
            KeyPair pair = gen.generateRSAKeyPair();
            X509Certificate cert = gen.generateCertificate(SslHelpers.DEFAULT_SUBJET_DN, pair);

            // Remove the old certificate if needed
            try {
                ks.deleteEntry(SslHelpers.DEFAULT_SUBJET_DN);
            } catch (KeyStoreException e) {
                // OK
            }

            // Add the certificate
            ks.setCertificateEntry(SslHelpers.DEFAULT_SUBJET_DN, cert);
            // Write the keystore
            FileOutputStream fos = new FileOutputStream(new File(keyStore));
            ks.store(fos, SslHelpers.DEFAULT_KS_PASSWD.toCharArray());
            fos.close();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to update the keystore " + keyStore + ": " + e);
            return false;
        }
    }

    private void clearPassword(char[] pass) {
        if (pass != null) {
            Arrays.fill(pass, ' ');
        }
    }

    private char[] askPassword(String msg) throws IOException {
        while (true) {
            char[] pass1 = PasswordField.getPassword(System.in, msg + ":");
            char[] pass2 = PasswordField.getPassword(System.in, msg + "(confirm):");
            if (!Arrays.equals(pass1, pass2)) {
                clearPassword(pass1);
                clearPassword(pass2);
                System.out.println("The two password does not match. Please try again");
            } else {
                clearPassword(pass2);
                return pass1;
            }
        }
    }

    /**
     * Create a keystore with a certificate
     */
    private boolean create(String keyStore) {
        try {
            // Create a certificate
            CertificateGenerator gen = new CertificateGenerator();
            KeyPair pair = gen.generateRSAKeyPair();
            X509Certificate cert = gen.generateCertificate(SslHelpers.DEFAULT_SUBJET_DN, pair);

            // Create the keystore
            KeyStore ks = KeyStore.getInstance("PKCS12", SslHelpers.BC_NAME);
            ks.load(null, null);

            ks.setKeyEntry(SslHelpers.DEFAULT_SUBJET_DN, pair.getPrivate(),
                    SslHelpers.DEFAULT_KS_PASSWD.toCharArray(), new X509Certificate[] { cert });

            // Write the keystore
            FileOutputStream fos = new FileOutputStream(new File(keyStore));
            ks.store(fos, SslHelpers.DEFAULT_KS_PASSWD.toCharArray());
            fos.close();
            return true;
        } catch (Exception e) {
            System.err.println("Failed to create the keystore " + keyStore + ": " + e);
            return false;
        }
    }
}