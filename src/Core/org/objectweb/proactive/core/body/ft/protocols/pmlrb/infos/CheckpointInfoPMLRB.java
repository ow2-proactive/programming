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
package org.objectweb.proactive.core.body.ft.protocols.pmlrb.infos;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.body.ft.checkpointing.CheckpointInfo;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;


/**
 * Checkpoint additional informations for the PMLRB protocol. Contains message logs
 * used to recover the activity state
 * @author The ProActive Team
 * @since ProActive 3.0
 */
public class CheckpointInfoPMLRB implements CheckpointInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 430L;
    /**
     *
     */

    // message logs
    private List<Request> requestLog;
    private List<Reply> replyLog;

    // pending request
    private Request pending;

    /**
     * Checkpoint infos constructor.
     * @param pendingRequest the request that must be served after the recovery.
     */
    public CheckpointInfoPMLRB(Request pendingRequest) {
        this.pending = pendingRequest;
        this.replyLog = new ArrayList<Reply>();
        this.requestLog = new ArrayList<Request>();
    }

    // GETTERs - SETTERs
    public Request getPendingRequest() {
        return this.pending;
    }

    public void setRequestLog(List<Request> requests) {
        this.requestLog = requests;
    }

    public void setReplyLog(List<Reply> replies) {
        this.replyLog = replies;
    }

    public List<Request> getRequestLog() {
        return this.requestLog;
    }

    public List<Reply> getReplyLog() {
        return this.replyLog;
    }

    public void addRequest(Request r) {
        this.requestLog.add(r);
    }

    public void addReply(Reply r) {
        this.replyLog.add(r);
    }
}
