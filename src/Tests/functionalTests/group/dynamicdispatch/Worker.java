/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package functionalTests.group.dynamicdispatch;

import org.objectweb.proactive.core.group.DispatchMode;
import org.objectweb.proactive.core.group.Dispatch;


public class Worker {

    int workerIndex;

    public Worker() {
    }

    public Worker(int index) {
        this.workerIndex = index;
    }

    public Task executeTask(Task t) {
        System.out.println("running worker " + workerIndex);
        t.execute(workerIndex);
        return t;
    }

    public void killWorker(int i) {
        if (workerIndex == i)
            System.exit(0);

    }

    @Dispatch(mode = DispatchMode.DYNAMIC)
    public Task executeDynamically(Task t) {
        return executeTask(t);
    }

}
