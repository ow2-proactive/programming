/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.examples.readers;

import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class Reader implements org.objectweb.proactive.RunActive {
    private ReaderDisplay display;
    private ReaderWriter rw;
    private boolean done;
    private boolean autopilot;
    private boolean reading;
    private int id;

    /**
     * The no args constructor as commanded by PaPdc
     */
    public Reader() {
    }

    /**
     * The real constructor
     */
    public Reader(ReaderDisplay display, ReaderWriter rw, int id) {
        this.display = display;
        this.rw = rw;
        this.id = id;
        done = false;
        autopilot = true;
        reading = false;
    }

    public void stopIt() {
        done = true;
    }

    private void startRead() {
        reading = true;
        display.setWait(id, true);
        rw.startRead();
        display.setRead(id, true);
    }

    private void endRead() {
        reading = false;
        rw.endRead();
        display.setRead(id, false);
    }

    /**
     * The live method.
     * @param body the body of the Active object
     */
    public void runActivity(org.objectweb.proactive.Body body) {
        org.objectweb.proactive.Service service = new org.objectweb.proactive.Service(body);
        while (!done) {
            service.serveOldest();
            // Autopilot mode
            if (reading) {
                endRead();
            } else {
                startRead();
            }
            try {
                Thread.sleep(700 + (long) (Math.random() * 1500));
            } catch (InterruptedException e) {
            }
        }
    }
}
