/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.ssh;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;


/**
 * A helper class to manager SSH Public keys
 *
 */
public class SSHKeys {
    static final public String[] IDENTITY_FILES = new String[] { "identity", "id_rsa", "id_dsa" };

    /** Default suffix for public keys */
    private final static String KEY_SUFFIX = ".pub";
    private final static int KEY_SUFFIX_LEN = KEY_SUFFIX.length();

    /** A Cache of public keys */
    private final String[] keys;

    public SSHKeys(String dir) throws IOException {
        this.keys = findKeys(dir);
    }

    public String[] getKeys() {
        return keys;
    }

    /**
     * Find all SSH keys inside SshParameters.getSshKeyDirectory()
     *
     * @return all keys found
     * @throws IOException If the base directory does not exist an {@link IOException}
     * is thrown
     */
    private String[] findKeys(String dirStr) throws IOException {
        File dir = new File(dirStr);
        if (!dir.exists()) {
            logger.error("Cannot open SSH connection, " + dir + "does not exist");
            throw new IOException(dir + "does not exist");
        }
        if (!dir.isDirectory()) {
            logger.error("Cannot open SSH connection, " + dir + "is not a directory");
            throw new IOException(dir + "does not exist");
        }
        if (!dir.canRead()) {
            logger.error("Cannot open SSH connection, " + dir + "is not readable");
            throw new IOException(dir + "does not exist");
        }

        String[] tmp = dir.list(new PrivateKeyFilter());

        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = dir.toString() + "/" + tmp[i];
            tmp[i] = tmp[i].substring(0, tmp[i].length() - 4);
        }

        return tmp;
    }

    static private class PrivateKeyFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            if (name.endsWith(".pub")) {
                // Look it this file without ".pub" exist
                File tmp = new File(dir, name.substring(0, name.length() - KEY_SUFFIX_LEN));
                return tmp.exists() && tmp.canRead() && tmp.isFile();
            }

            return false;
        }
    }
}
