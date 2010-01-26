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
package org.objectweb.proactive.extensions.calcium.environment.proactive;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class AOInterpreter {
    protected AOStageIn stageIn;
    protected AOStageOut stageOut;
    protected AOStageCompute stageCompute;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOInterpreter() {
    }

    public AOInterpreter(AOTaskPool taskpool, FileServerClientImpl fserver) throws NodeException,
            ActiveObjectCreationException {
        Node localnode = NodeFactory.getDefaultNode();

        this.stageOut = (AOStageOut) PAActiveObject.newActive(AOStageOut.class.getName(), new Object[] {
                taskpool, fserver }, localnode);

        this.stageCompute = (AOStageCompute) PAActiveObject.newActive(AOStageCompute.class.getName(),
                new Object[] { taskpool, stageOut }, localnode);

        this.stageIn = (AOStageIn) PAActiveObject.newActive(AOStageIn.class.getName(), new Object[] {
                taskpool, fserver, stageCompute }, localnode);
    }

    public AOStageIn getStageIn(AOInterpreterPool interpool) {

        stageOut.setStageInAndInterPool(stageIn, interpool);

        return stageIn;
    }
}
