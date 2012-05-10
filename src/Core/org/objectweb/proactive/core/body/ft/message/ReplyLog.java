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
package org.objectweb.proactive.core.body.ft.message;

import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;


/**
 * This class is used for logging a reply.
 * It contains its original destination.
 * @author The ProActive Team
 * @since ProActive 2.2
 */
public class ReplyLog implements MessageLog {

    /**
     *
     */

    // Logged message and its destination
    private UniversalBody destination;
    private Reply reply;

    /**
     * Create a reply log.
     * @param r The reply to log
     * @param d The destination body
     */
    public ReplyLog(Reply r, UniversalBody d) {
        this.destination = d;
        this.reply = r;
    }

    /**
     * Return the logged reply.
     * @return the logged reply.
     */
    public Reply getReply() {
        return reply;
    }

    /**
     * Return the destination of this reply
     * @return the destination of this reply
     */
    public UniversalBody getDestination() {
        return destination;
    }
}
