/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.examples.hello;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.annotation.MigrationSignal;


/**
 * An example of the use of migration.
 * The SimpleAgent simply says where it is. The main() jsut creates it,
 * and moves it to user-specified node (node must be created manually previously)
 */
@ActiveObject
public class SimpleAgent implements java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    private final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);

    /** ProActive compulsory empty no-args constructor */
    public SimpleAgent() {
    }

    /** Migrate the Active Object to a new host */
    @MigrationSignal
    public void moveTo(String t) {
        try {
            PAMobileAgent.migrateTo(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Returns the machine name on which the Active Object is currently */
    public String whereAreYou() {
        try {
            return URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress());
        } catch (Exception e) {
            return "Localhost lookup failed";
        }
    }

    /** Creates a migratable SimpleAgaent, and migrates it */
    public static void main(String[] args) {
        if (args.length != 1) {
            logger.info("Usage: java org.objectweb.proactive.examples.hello.SimpleAgent hostname/NodeName ");
            System.exit(-1);
        }

        ProActiveConfiguration.load();
        SimpleAgent t = null;
        try {
            // create the SimpleAgent in this JVM
            t = (SimpleAgent) PAActiveObject.newActive("org.objectweb.proactive.examples.hello.SimpleAgent",
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // migrate the SimpleAgent to the location identified by the given node URL
        // we assume here that the node does already exist
        logger.info("Migrating from " + t.whereAreYou());
        t.moveTo(args[0]);
        logger.info("The Active Object is now on host : " + t.whereAreYou());
    }
}
