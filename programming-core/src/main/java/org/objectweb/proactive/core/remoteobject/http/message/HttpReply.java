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
package org.objectweb.proactive.core.remoteobject.http.message;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;
import org.objectweb.proactive.core.remoteobject.http.util.HttpUtils;


/**
 * This kind of HTTP message isusefull to receive and send replies.
 * @author The ProActive Team
 * @see HttpMessage
 */
public class HttpReply extends HttpMessage implements Serializable {

    private static final long serialVersionUID = 62L;
    private Reply reply;
    private UniqueID idBody;

    /**
     *  Constructs an HTTP Message containing a ProActive Reply
     * @param reply The ProActive Reply to encapsulate
     * @param idBody The unique id of the targeted active object
     */
    public HttpReply(Reply reply, UniqueID idBody, String url) {
        super(url);
        this.reply = reply;
        this.idBody = idBody;
    }

    public int getReturnedObject() {
        if (this.returnedObject != null) {
            return ((Integer) this.returnedObject).intValue();
        }
        return 0; // or throws an exception ...
    }

    @Override
    public Object processMessage() {
        try {
            Body body = HttpUtils.getBody(idBody);
            if (this.reply != null) {
                body.receiveReply(this.reply);
                return "";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
