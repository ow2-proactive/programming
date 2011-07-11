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
package org.objectweb.proactive.extensions.calcium.instructions;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.calcium.muscle.Divide;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


/**
 * This class is an instruction that will perform a divition of one task
 * into several sub-tasks. Each sub-tasks will have the same instruction
 * stack, as specified in the constructor of the class.
 *
 * @author The ProActive Team
 *
 * @param <P> The type of the parameter inputed at division.
 * @param <X> The type of the objects resulting from the division.
 */
public class DivideSIMD<P, X> implements Instruction<P, X> {
    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    static Logger logger = ProActiveLogger.getLogger(Loggers.SKELETONS_STRUCTURE);
    Divide<P, X> div;
    private Stack<Instruction> instruction; //Single instruction stack

    protected DivideSIMD(Divide<P, X> div, Stack<Instruction> instruction) {
        this.div = div;
        this.instruction = instruction;
    }

    public Task<X> compute(SkeletonSystemImpl system, Task<P> parent) throws Exception {
        Timer timer = new Timer();

        X[] childObjects = div.divide(parent.getObject(), system);
        timer.stop();

        for (X o : childObjects) {
            Task<X> child = new Task<X>(o);
            child.setStack(instruction); //Each child task executes the same instruction stack
            parent.family.addReadyChild(child); //parent remebers it's children
        }

        parent.getStats().getWorkout().track(div, timer);
        return (Task<X>) parent;
    }

    public boolean isStateFul() {
        return Stateness.isStateFul(div);
    }

    @SuppressWarnings("unchecked")
    public PrefetchFilesMatching getPrefetchFilesAnnotation() {
        Class cls = div.getClass();

        return (PrefetchFilesMatching) cls.getAnnotation(PrefetchFilesMatching.class);
    }
}
