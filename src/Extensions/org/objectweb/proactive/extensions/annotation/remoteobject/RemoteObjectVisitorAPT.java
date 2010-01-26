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
package org.objectweb.proactive.extensions.annotation.remoteobject;

import org.objectweb.proactive.extensions.annotation.activeobject.ActiveObjectVisitorAPT;

import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.Declaration;


public class RemoteObjectVisitorAPT extends ActiveObjectVisitorAPT {

    public RemoteObjectVisitorAPT(final Messager messager) {
        super(messager);
    }

    protected void reportError(Declaration declaration, String msg) {
        super.reportError(declaration, replaceActiveToRemote(msg));
    }

    protected void reportWarning(Declaration declaration, String msg) {
        super.reportWarning(declaration, replaceActiveToRemote(msg));
    }

    private String replaceActiveToRemote(String msg) {
        String newMsg = msg.replaceAll("an\\sactive", "a remote");
        msg.replaceAll("active", "remote");
        newMsg = newMsg.replaceAll("Active", "Remote");
        return newMsg;
    }

}
