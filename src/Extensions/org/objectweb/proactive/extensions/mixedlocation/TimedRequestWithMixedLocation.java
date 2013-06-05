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
package org.objectweb.proactive.extensions.mixedlocation;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.ext.locationserver.LocationServer;
import org.objectweb.proactive.ext.locationserver.TimedRequestWithLocationServer;


public class TimedRequestWithMixedLocation extends TimedRequestWithLocationServer {
    private static final int MAX_TRIES = 30;
    private static int counter = 0;
    private int tries;
    transient protected LocationServer server;
    protected long startTime;

    public TimedRequestWithMixedLocation(MethodCall methodCall, UniversalBody sender, boolean isOneWay,
            long nextSequenceID, LocationServer server) {
        super(methodCall, sender, isOneWay, nextSequenceID, server);
    }

    @Override
    protected void sendRequest(UniversalBody destinationBody) throws java.io.IOException {
        System.out.println("RequestWithMixedLocation: sending to universal " + counter);
        try {
            destinationBody.receiveRequest(this);
        } catch (Exception e) {
            //  e.printStackTrace();
            this.backupSolution(destinationBody);
        }
    }

    /**
     * Implements the backup solution
     */
    @Override
    protected void backupSolution(UniversalBody destinationBody) throws java.io.IOException {
        boolean ok = false;
        tries = 0;
        System.out.println("RequestWithMixedLocationr: backupSolution() contacting server " + server);
        System.out.println("RequestWithMixedLocation.backupSolution() : looking for " + destinationBody);
        //get the new location from the server
        while (!ok && (tries < MAX_TRIES)) {
            UniversalBody mobile = server.searchObject(destinationBody.getID());
            System.out.println("RequestWithMixedLocation: backupSolution() server has sent an answer");

            //we want to bypass the stub/proxy
            UniversalBody newDestinationBody = (UniversalBody) ((FutureProxy) ((StubObject) mobile)
                    .getProxy()).getResult();

            // !!!!
            // !!!! should put a counter here to stop calling if continuously failing
            // !!!!
            try {
                // sendRequest(newDestinationBody);
                newDestinationBody.receiveRequest(this);
                //everything went fine, we have to update the current location of the object
                //so that next requests don't go through the server
                System.out.println("RequestWithMixedLocation: backupSolution() updating location");
                if (sender != null) {
                    sender.updateLocation(newDestinationBody.getID(), newDestinationBody.getRemoteAdapter());
                }
                ok = true;
            } catch (Exception e) {
                System.out.println("RequestWithMixedLocation: backupSolution() failed");
                tries++;
                try {
                    Thread.sleep(500);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }
}
