/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.body.ft.servers.recovery;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.ft.checkpointing.Checkpoint;
import org.objectweb.proactive.core.body.ft.servers.util.ActiveQueueJob;
import org.objectweb.proactive.core.node.Node;


/**
 * Job for recovering an activity.
 * @author The ProActive Team
 * @since 2.2
 */
public class RecoveryJob implements ActiveQueueJob {
    private Checkpoint toSend;
    private int incarnation;
    private Node receiver;

    /**
     * @param toSend the checkpoint used for the recovery
     * @param incarnation the incarnation number of this recovery
     * @param receiver the node on which the activity is recovered
     */
    public RecoveryJob(Checkpoint toSend, int incarnation, Node receiver) {
        super();
        this.toSend = toSend;
        this.incarnation = incarnation;
        this.receiver = receiver;
    }

    public void doTheJob() {
        try {
            receiver.getProActiveRuntime().receiveCheckpoint(receiver.getNodeInformation().getURL(), toSend,
                    incarnation);
            RecoveryProcessImpl.logger.info("[RECOVERY] " + this.toSend.getBodyID() + " recovered.");
        } catch (ProActiveException e) {
            RecoveryProcessImpl.logger.error("[RECOVERY] **ERROR** Unable to recover " +
                this.toSend.getBodyID());
            e.printStackTrace();
        }
    }
}
