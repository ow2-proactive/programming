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
package org.objectweb.proactive;

import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.PAProperty;
import org.objectweb.proactive.core.util.ProActiveInet;

import java.util.*;


public class Main {
    static final private String PA_VERSION = "$Id$";

    /**
     * Returns the version number
     *
     * @return String
     */
    public static String getProActiveVersion() {
        return PA_VERSION;
    }

    public static void main(String[] args) {
        ProActiveInet pInet = ProActiveInet.getInstance();

        System.out.println("\t\t--------------------");
        System.out.println("\t\tProActive " + PAVersion.getProActiveVersion());
        System.out.println("\t\t--------------------");
        System.out.println();
        System.out.println();

        String localAddress = null;
        localAddress = pInet.getInetAddress().getHostAddress();
        System.out.println("Local IP Address: " + localAddress);

        System.out.println("Config dir: " + Constants.USER_CONFIG_DIR);
        System.out.println();

        System.out.println("Network setup:");
        for (String s : pInet.listAllInetAddress()) {
            System.out.println("\t" + s);
        }
        System.out.println();

        System.out.println("Available properties:");
        Map<Class<?>, List<PAProperty>> allProperties = PAProperties.getAllProperties();
        for (Class<?> cl : allProperties.keySet()) {
            System.out.println("From class " + cl.getCanonicalName());
            printProperties(allProperties.get(cl));
        }
    }

    private static void printProperties(List<PAProperty> props) {
        ArrayList<PAProperty> reals = new ArrayList<PAProperty>(props.size());
        for (PAProperty prop : props) {
            if (!prop.isAlias()) {
                reals.add(prop);
            }
        }

        Collections.sort(reals, new Comparator<PAProperty>() {
            @Override
            public int compare(PAProperty o1, PAProperty o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (PAProperty prop : reals) {
            System.out.println("\t" + prop.getType() + "\t" + prop.getName() + " [" +
                prop.getValueAsString() + "]");
        }
    }
}
