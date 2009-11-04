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
package functionalTests.component.collectiveitf.unicast;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.group.DispatchBehavior;
import org.objectweb.proactive.core.mop.MethodCall;


public class CustomUnicastDispatch implements DispatchBehavior {

    int index = 1;

    public List<Integer> getTaskIndexes(MethodCall originalMethodCall, List<MethodCall> generatedMethodCall,
            int nbWorkers) {
        List<Integer> l = new ArrayList<Integer>(1);

        if (!(generatedMethodCall.size() == 1)) {
            throw new RuntimeException("invalid number");
        }
        if (RunnerImpl.PARAMETER_1.equals(generatedMethodCall.iterator().next().getEffectiveArguments()[0])) {
            l.add(0);
        } else if (RunnerImpl.PARAMETER_2.equals(generatedMethodCall.iterator().next()
                .getEffectiveArguments()[0])) {
            l.add(1);
        }

        return l;
    }

}
