/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive;

import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;


public class Main {

    /**
     * Returns the version number
     *
     * @return String
     */
    public static String getProActiveVersion() {
        return "4.0.3";
    }

    public static void main(String[] args) {
        System.out.println("\t\t--------------------");
        System.out.println("\t\tProActive " + PAVersion.getProActiveVersion());
        System.out.println("\t\t--------------------");
        System.out.println();
        System.out.println();

        String localAddress = null;
        localAddress = URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress());
        System.out.println("Local IP Address: " + localAddress);
        System.out.println("Available properties:");
        for (PAProperties p : PAProperties.values()) {
            String type = p.isBoolean() ? "Boolean" : "String";
            System.out.println("\t" + type + "\t" + p.getKey() + " [" + p.getValue() + "]");
        }
    }
}
