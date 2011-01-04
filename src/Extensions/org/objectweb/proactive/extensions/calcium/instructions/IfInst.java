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
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.muscle.Condition;
import org.objectweb.proactive.extensions.calcium.stateness.Stateness;
import org.objectweb.proactive.extensions.calcium.statistics.Timer;
import org.objectweb.proactive.extensions.calcium.system.PrefetchFilesMatching;
import org.objectweb.proactive.extensions.calcium.system.SkeletonSystemImpl;
import org.objectweb.proactive.extensions.calcium.task.Task;


public class IfInst<P> implements Instruction<P, P> {
    Condition<P> cond;
    Stack<Instruction> trueChildStack;
    Stack<Instruction> falseChildStack;

    public IfInst(Condition<P> cond, Stack<Instruction> trueChildStack, Stack<Instruction> falseChildStack) {
        this.cond = cond;
        this.trueChildStack = trueChildStack;
        this.falseChildStack = falseChildStack;
    }

    public Task<P> compute(SkeletonSystemImpl system, Task<P> t) throws Exception {
        Stack<Instruction> childStack;
        Timer timer = new Timer();
        boolean evalCondition = cond.condition(t.getObject(), system);
        timer.stop();

        if (evalCondition) {
            childStack = trueChildStack;
        } else {
            childStack = falseChildStack;
        }

        Vector<Instruction> taskStack = t.getStack();
        taskStack.addAll(childStack);
        t.setStack(taskStack);
        t.getStats().getWorkout().track(cond, timer);

        return t;
    }

    public boolean isStateFul() {
        return Stateness.isStateFul(cond);
    }

    @SuppressWarnings("unchecked")
    public PrefetchFilesMatching getPrefetchFilesAnnotation() {
        Class cls = cond.getClass();

        return (PrefetchFilesMatching) cls.getAnnotation(PrefetchFilesMatching.class);
    }
}
