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
package org.objectweb.proactive.examples.philosophers;

import org.objectweb.proactive.ObjectForSynchronousCall;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


/**
 * This class implements the behaviors of the table and the storage of the forks
 */
@ActiveObject
public class Table implements org.objectweb.proactive.RunActive {

    /**
     * The array containing the forks
     */
    protected boolean[] forks;

    /**
     * The reference to the UI layout
     */
    protected DinnerLayout layout;

    /**
     * The no arg constructor as commanded by PAPDC
     */
    public Table() {
    }

    /**
     * The real constructor
     * @param layout the reference to the Active layout stub
     */
    public Table(DinnerLayout layout) {
        this.layout = layout;
        forks = new boolean[5];
    }

    /**
     * putForks
     * Realeases both forks
     * @param id the id of the philosopher [same as the seat index]
     */
    public void putForks(int id) {
        // Sets the specified fork to be released.
        int nextId = (id + 1) % forks.length;
        forks[id] = forks[nextId] = false;
        layout.updateFork(id, 3);
        layout.updateFork(nextId, 3);
        layout.update(id, 0);
    }

    /**
     * getForks
     * Try to take the forks
     * <b>Note:</b> If both forks aren't free, no forks are taken
     * @param id the id of the philosopher [same as the seat index]
     */
    public ObjectForSynchronousCall getForks(int id) {
        int nextId = (id + 1) % forks.length;
        forks[id] = forks[nextId] = true;
        layout.update(id, 2);
        layout.updateFork(id, 4);
        layout.updateFork(nextId, 4);
        return new ObjectForSynchronousCall();
    }

    /**
     * mayEat
     */
    private boolean mayEat(int id) {
        int nextId = (id + 1) % forks.length;
        return ((forks[id] == false) && (forks[nextId] == false)); // if false both forks aren't free
    }

    /**
     * The live method
     * @param body The active object's body
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        GetForkRequestFilter getForkRequestFilter = new GetForkRequestFilter();
        while (body.isActive()) {
            service.serveAll(getForkRequestFilter);
            service.serveOldest("putForks");
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
            }
        }
    }

    //
    // -- INNER CLASSES -----------------------------------------------
    //
    private class GetForkRequestFilter implements org.objectweb.proactive.core.body.request.RequestFilter {
        public GetForkRequestFilter() {
        }

        public boolean acceptRequest(org.objectweb.proactive.core.body.request.Request request) {
            if (!request.getMethodName().equals("getForks")) {
                return false;
            }
            int place = ((Integer) request.getParameter(0)).intValue();
            if (mayEat(place)) {
                // Notify the user interface
                return true;
            } else {
                return false;
            }
        }
    } // end inner class MyRequestFilter
}
