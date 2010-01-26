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
package org.objectweb.proactive.extensions.calcium.diagnosis.causes;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.objectweb.proactive.extensions.calcium.statistics.Exercise;
import org.objectweb.proactive.extensions.calcium.statistics.Workout;


public abstract class AbstractCause implements Cause {
    public List<Method> blameCode(Workout s) {
        return blamecode(s, DEFAULT_NUMBER_OF_CAUSES);
    }

    public List<Method> blamecode(Workout s, int number) {
        List<Method> blamed = new Vector<Method>();

        List<Exercise> ex = getSortedExcercise(s);

        for (int i = 0; i < Math.min(number, ex.size()); i++) {
            Class<?> c = ex.get(i).getMuscleClass();
            try {
                Method[] mall = c.getMethods();
                for (Method m : mall) {
                    if (!m.isBridge() && (m.getName() == getMethodSearchString())) {
                        blamed.add(m);
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("Unable to find method: " + getMethodSearchString() + " in class: " +
                    c.getName());
            }
        }

        return blamed;
    }

    abstract protected List<Exercise> getSortedExcercise(Workout s);

    abstract protected String getMethodSearchString();
}
