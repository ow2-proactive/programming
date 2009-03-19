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
package org.objectweb.proactive.ext.util;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.BodyMap;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.ext.locationserver.LocationServer;


/**
 * An implementation of a Location Server
 */
public class SimpleLocationServer implements org.objectweb.proactive.RunActive, LocationServer {
    static Logger logger = ProActiveLogger.getLogger(Loggers.MIGRATION);
    final private BodyMap table = new BodyMap();

    public SimpleLocationServer() {
    }

    /**
     * Update the location for the mobile object s
     * with id
     */
    public void updateLocation(UniqueID i, UniversalBody s) {
        //       System.out.println("Server: updateLocation() " + i + " object = " + s);
        this.table.updateBody(i, s);
    }

    /**
     * Return a reference to the remote body if available.
     * Return null otherwise
     */
    public UniversalBody searchObject(UniqueID id) {
        return this.table.getBody(id);
    }

    /**
     * First register with the specified url
     * Then wait for request
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        service.fifoServing();
    }

    public void updateLocation(UniqueID i, UniversalBody s, int version) {
        // Commented an obviously broken piece of code
        // this.updateLocation(i, s, 0);
    }
}
