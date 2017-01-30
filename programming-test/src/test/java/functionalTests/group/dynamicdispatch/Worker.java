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
package functionalTests.group.dynamicdispatch;

import org.objectweb.proactive.core.group.Dispatch;
import org.objectweb.proactive.core.group.DispatchMode;


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
