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
        master.solve(adapterTasks);
        return master.waitAllResults();
    }
}
