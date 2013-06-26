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
package org.objectweb.proactive.ext.hpc;

import java.io.IOException;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;


public class DummySender implements UniversalBody {

    //
    // ---- DUMMY SENDER ---
    //

    private static DummySender instance = new DummySender();

    public static DummySender getDummySender() {
        return instance;
    }

    public String getNodeURL() {
        return "Unknown";
    }

    //
    // ---- DUMMY SENDER ---
    //

    public void createShortcut(Shortcut shortcut) throws IOException {
    }

    public void disableAC() throws IOException {
    }

    public void enableAC() throws IOException {
    }

    public UniqueID getID() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    public String getReifiedClassName() {
        return null;
    }

    public UniversalBody getRemoteAdapter() {

        return null;
    }

    public Object receiveHeartbeat() throws IOException {
        return null;
    }

    public void receiveReply(Reply r) throws IOException {
    }

    public void receiveRequest(Request request) throws IOException {
    }

    @Deprecated
    public void register(String url) throws UnknownProtocolException {
    }

    public void setRegistered(boolean registered) throws IOException {
    }

    public void updateLocation(UniqueID id, UniversalBody body) throws IOException {
    }

    public String registerByName(String name, boolean rebind) throws IOException {
        return null;
    }

    public String getUrl() {

        return null;
    }

    public String registerByName(String name, boolean rebind, String protocol) throws IOException {
        return null;
    }
}
