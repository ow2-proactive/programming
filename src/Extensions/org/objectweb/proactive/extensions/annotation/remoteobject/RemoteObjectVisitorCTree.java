/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.annotation.remoteobject;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import org.objectweb.proactive.extensions.annotation.activeobject.ActiveObjectVisitorCTree;

import com.sun.source.tree.ReturnTree;
import com.sun.source.util.Trees;


/**
 * <p>This class implements a visitor for the ProActiveProcessor, according to the Pluggable Annotation Processing API(jsr269) specification</p>
 */
public class RemoteObjectVisitorCTree extends ActiveObjectVisitorCTree {

    public RemoteObjectVisitorCTree(ProcessingEnvironment procEnv) {
        super(procEnv);
    }

    @Override
    public Void visitReturn(ReturnTree returnNode, Trees trees) {
        return null;
    }

    protected void reportError(String msg, Element element) {
        super.reportError(replaceActiveToRemote(msg), element);
    }

    protected void reportWarning(String msg, Element element) {
        super.reportWarning(replaceActiveToRemote(msg), element);
    }

    private String replaceActiveToRemote(String msg) {
        String newMsg = msg.replaceAll("an\\sactive", "a remote");
        msg.replaceAll("active", "remote");
        newMsg = newMsg.replaceAll("Active", "Remote");
        return newMsg;
    }
}
