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
package org.objectweb.proactive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.PAProperty;
import org.objectweb.proactive.core.util.ProActiveInet;


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
            System.out.println("\t" + prop.getType() + "\t" + prop.getName() + " [" + prop.getValueAsString() + "]");
        }
    }
}
