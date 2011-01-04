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
package org.objectweb.proactive.ext.locationserver;

import java.io.IOException;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestReceiver;
import org.objectweb.proactive.core.event.MessageEventListener;


public class BouncingRequestReceiver implements RequestReceiver {
    private static ObjectHasMigratedException REUSABLE_EXCEPTION = new ObjectHasMigratedException(
        "Object has migrated");

    public int receiveRequest(Request r, Body bodyReceiver) throws java.io.IOException {
        //System.out.println("BouncingRequestReceiver: receiveRequest()");
        //        throw REUSABLE_EXCEPTION;
        throw new ObjectHasMigratedException("Object has migrated");
    }

    public void addMessageEventListener(MessageEventListener listener) {
    }

    public void removeMessageEventListener(MessageEventListener listener) {
    }

    public void setImmediateService(String methodName) {
    }

    public void removeImmediateService(String methodName, Class<?>[] parametersTypes) throws IOException {
    }

    public void setImmediateService(String methodName, Class<?>[] parametersTypes) throws IOException {
    }

    public boolean isInImmediateService() throws IOException {
        return false;
    }

    public boolean hasThreadsForImmediateService() {
        return false;
    }

    public void terminate() {
    }

}
