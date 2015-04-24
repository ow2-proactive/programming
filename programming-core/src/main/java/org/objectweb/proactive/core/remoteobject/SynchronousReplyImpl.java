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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.tags.MessageTags;


public class SynchronousReplyImpl implements Reply, Serializable {

    private static final long serialVersionUID = 62L;
    protected MethodCallResult reply;

    public SynchronousReplyImpl() {
    }

    public SynchronousReplyImpl(MethodCallResult reply) {
        this.reply = reply;
    }

    public MethodCallResult getResult() {
        return reply;
    }

    public long getSessionId() {
        return 0;
    }

    public boolean isAutomaticContinuation() {
        return false;
    }

    public boolean isCiphered() {
        return false;
    }

    public void send(UniversalBody destinationBody) throws IOException {
    }

    public String getMethodName() {
        return null;
    }

    public long getSequenceNumber() {
        return 0;
    }

    public UniqueID getSourceBodyID() {
        return null;
    }

    public long getTimeStamp() {
        return 0;
    }

    public boolean isOneWay() {
        return false;
    }

    public MessageTags getTags() {
        return null;
    }
}
