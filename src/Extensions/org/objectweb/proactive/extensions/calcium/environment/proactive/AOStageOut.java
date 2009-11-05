/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
public class AOStageOut {
    protected AOTaskPool taskpool;
    protected AOInterpreterPool interpool;
    protected FileServerClientImpl fserver;
    protected AOStageIn stageIn;
    protected Interpreter interpreter;

    /**
     * Empty constructor for ProActive  MOP
     * Do not use directly!!!
     */
    @Deprecated
    public AOStageOut() {
    }

    /**
     * @param taskpool
     * @param fserver
     */
    public AOStageOut(AOTaskPool taskpool, FileServerClientImpl fserver) {
        super();
        this.taskpool = taskpool;
        this.fserver = fserver;
        this.stageIn = null;
        this.interpool = null;

        interpreter = new Interpreter();
    }

    public void setStageInAndInterPool(AOStageIn stageIn, AOInterpreterPool interpool) {
        this.stageIn = stageIn;
        this.interpool = interpool;
    }

    public void stageOut(InterStageParam param) {
        Task<?> task = param.task;
        SkeletonSystemImpl system = param.system;
        FileStaging fstaging = param.fstaging;

        try {
            task = interpreter.stageOut(task, fstaging, system, fserver);
        } catch (Exception e) {
            task.setException(e);
        }

        taskpool.putProcessedTask(task);
        interpool.put(stageIn);
    }
}
