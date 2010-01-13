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
package org.objectweb.proactive.extensions.calcium.diagnosis.causes;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.extensions.calcium.muscle.Condition;
import org.objectweb.proactive.extensions.calcium.statistics.Exercise;
import org.objectweb.proactive.extensions.calcium.statistics.Workout;


public class LastTaskPenaltyCause extends AbstractCause {
    public String getDescription() {
        return "Average task size is too big with respect to wallclock time.";
    }

    @Override
    protected String getMethodSearchString() {
        Method[] m = Condition.class.getMethods();
        return m[0].getName();
    }

    @Override
    protected List<Exercise> getSortedExcercise(Workout s) {
        List<Exercise> ex = s.getConditionExercises();
        Collections.sort(ex, Exercise.compareByInvokedTimes);
        return ex;
    }

    public boolean canBlameCode(Workout s) {
        return s.getConditionExercises().size() > 0;
    }

    public String suggestAction() {
        return "Method should return true more often.";
    }
}
