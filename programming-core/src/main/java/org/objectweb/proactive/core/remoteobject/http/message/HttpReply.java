/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
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
