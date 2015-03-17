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
package org.objectweb.proactive.core.body.reply;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.mop.Utils;


public class ReplyImpl extends MessageImpl implements Reply, Serializable {

    private static final long serialVersionUID = 61L;

    /** The hypothetic result */
    protected MethodCallResult result;

    /** true if this reply is sent by automatic continuation */
    private boolean isAC;

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result,
            MessageTags tags) {
        this(senderID, sequenceNumber, methodName, result, false, tags);
    }

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result) {
        this(senderID, sequenceNumber, methodName, result, false, null);
    }

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result,
            boolean isAutomaticContinuation) {
        this(senderID, sequenceNumber, methodName, result, isAutomaticContinuation, null);
    }

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result,
            boolean isAutomaticContinuation, MessageTags tags) {
        super(senderID, sequenceNumber, true, methodName, tags);
        this.result = result;
        this.isAC = isAutomaticContinuation;
    }

    public MethodCallResult getResult() {
        return result;
    }

    public void send(UniversalBody destinationBody) throws IOException {
        // if destination body is on the same VM that the sender, we must
        // perform
        // a deep copy of result in order to preserve ProActive model.
        UniqueID destinationID = destinationBody.getID();

        // The following code ensures that if the destination body is located
        // on the same VM, the result will only be serialized once.
        // This also solves the bug PROACTIVE-81.
        UniversalBody localRef = LocalBodyStore.getInstance().getLocalBody(destinationID);
        if (localRef == null) {
            // halfBody ?
            localRef = LocalBodyStore.getInstance().getLocalHalfBody(destinationID);
        }

        if (localRef != null) {
            destinationBody = localRef;
            result = (MethodCallResult) Utils.makeDeepCopy(result);
        }

        destinationBody.receiveReply(this);
    }

    /**
     * @see org.objectweb.proactive.core.body.reply.Reply#isAutomaticContinuation()
     */
    public boolean isAutomaticContinuation() {
        return this.isAC;
    }
}
