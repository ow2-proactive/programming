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
package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.EngineTask;
import org.objectweb.proactive.extra.montecarlo.Executor;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;


/**
 * ExecutorImpl
 *
 * @author The ProActive Team
 */
public class ExecutorImpl implements Executor {

    SubMaster<EngineTaskAdapter, Serializable> master;

    public ExecutorImpl(SubMaster master) {
        this.master = master;
    }

    public List<Serializable> solve(List<EngineTask> engineTasks) throws TaskException {
        ArrayList<EngineTaskAdapter> adapterTasks = new ArrayList<EngineTaskAdapter>(engineTasks.size());
        for (EngineTask etask : engineTasks) {
            adapterTasks.add(new EngineTaskAdapter(etask));
        }
        master.setResultReceptionOrder(SubMaster.SUBMISSION_ORDER);
        master.solve(adapterTasks);
        return master.waitAllResults();
    }
}
