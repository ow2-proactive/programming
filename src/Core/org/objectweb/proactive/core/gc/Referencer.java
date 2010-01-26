/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.core.gc;

public class Referencer {

    /**
     * The last activity we gave it in a GC response
     */
    private Activity givenActivity;

    /**
     * Whether the referenced agreed on the last activity it gave us.
     * Note that this last activity can be different than this.givenActivity
     * in which case, it will obviously disagree with the consensus.
     */
    private boolean consensus;

    /**
     * When did we receive the latest GC message from this referencer?
     */
    private long lastMessageTimestamp;

    /**
     * Did we notify this referencer that we were in a dead cycle?
     */
    private boolean notifiedCycle;

    Referencer() {
        this.notifiedCycle = false;
    }

    long getLastMessageTimestamp() {
        return this.lastMessageTimestamp;
    }

    void setLastGCMessage(GCSimpleMessage mesg) {
        this.consensus = mesg.getConsensus();
        this.lastMessageTimestamp = System.currentTimeMillis();
    }

    void setGivenActivity(Activity activity) {
        if (!activity.equals(this.givenActivity)) {
            this.givenActivity = activity;
            this.consensus = false;
        }
    }

    boolean getConsensus(Activity activity) {
        if (activity.equals(this.givenActivity)) {
            return this.consensus;
        }

        return false;
    }

    boolean isNotifiedCycle() {
        return this.notifiedCycle;
    }

    void setNotifiedCycle() {
        this.notifiedCycle = true;
    }
}
