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
