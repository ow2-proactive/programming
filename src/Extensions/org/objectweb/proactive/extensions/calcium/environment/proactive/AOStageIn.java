/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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

import org.objectweb.proactive.extensions.annotation.ActiveObject;
import org.objectweb.proactive.extensions.calcium.environment.Interpreter;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.system.files.FileStaging;
import org.objectweb.proactive.extensions.calcium.task.Task;


@ActiveObject
public class AOStageIn {
    protected FileServerClientImpl fserver;
    protected AOStageCompute stageCompute;
    protected AOTaskPool taskpool;
    protected Interpreter interpreter;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOStageIn() {
    }

    /**
     * @param fserver
     * @param next
     * @param taskpool
     */
    public AOStageIn(AOTaskPool taskpool, FileServerClientImpl fserver, AOStageCompute stageCompute) {
        super();
        this.fserver = fserver;
        this.stageCompute = stageCompute;
        this.taskpool = taskpool;

        interpreter = new Interpreter();
    }

    public void stageIn(Task task) {
        //task = (Task) PAFuture.getFutureValue(task);
        try {
            SkeletonSystemImpl system = new SkeletonSystemImpl();
            FileStaging files = interpreter.stageIn(task, system, fserver);
            stageCompute.computeTheLoop(new InterStageParam(task, files, system));

            //TODO put my self in the AOI pool
        } catch (Exception e) {
            task.setException(e);
            taskpool.putProcessedTask(task);
        }
    }
}
