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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.ft.message.MessageInfo;
import org.objectweb.proactive.core.body.ft.protocols.FTManager;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;


public class SynchronousReplyImpl implements Reply, Serializable {
    protected MethodCallResult reply;

    public SynchronousReplyImpl() {
    }

    public SynchronousReplyImpl(MethodCallResult reply) {
        this.reply = reply;
    }

    public boolean decrypt(ProActiveSecurityManager psm) throws RenegotiateSessionException {
        // TODO Auto-generated method stub
        return false;
    }

    public MethodCallResult getResult() {
        return reply;
    }

    public long getSessionId() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isAutomaticContinuation() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isCiphered() {
        // TODO Auto-generated method stub
        return false;
    }

    public int send(UniversalBody destinationBody) throws IOException {
        return 0;
    }

    public FTManager getFTManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public MessageInfo getMessageInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMethodName() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getSequenceNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    public UniqueID getSourceBodyID() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getTimeStamp() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean ignoreIt() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isOneWay() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setFTManager(FTManager ft) {
        // TODO Auto-generated method stub
    }

    public void setIgnoreIt(boolean ignore) {
        // TODO Auto-generated method stub
    }

    public void setMessageInfo(MessageInfo mi) {
        // TODO Auto-generated method stub
    }

    public MessageTags getTags() {
        // TODO Auto-generated method stub
        return null;
    }
}
