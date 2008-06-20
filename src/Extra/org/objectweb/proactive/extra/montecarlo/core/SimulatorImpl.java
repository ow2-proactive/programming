package org.objectweb.proactive.extra.montecarlo.core;

import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.SubMaster;
import org.objectweb.proactive.extra.montecarlo.ExperienceSet;
import org.objectweb.proactive.extra.montecarlo.Simulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.LinkedList;


/**
 * SimulatorImpl
 *
 * @author The ProActive Team
 */
public class SimulatorImpl implements Simulator {

    SubMaster<ExperienceTask, double[]> master;

    public SimulatorImpl(SubMaster master) {
        this.master = master;
    }

    public Enumeration<double[]> solve(List<ExperienceSet> experienceSets) throws TaskException {
        ArrayList<ExperienceTask> adapterTasks = new ArrayList<ExperienceTask>(experienceSets.size());
        for (ExperienceSet eset : experienceSets) {
            adapterTasks.add(new ExperienceTask(eset));
        }
        master.setResultReceptionOrder(SubMaster.COMPLETION_ORDER);
        master.solve(adapterTasks);
        return new OutputEnumeration();
    }

    public class OutputEnumeration implements Enumeration<double[]> {

        private LinkedList<double[]> buffer = new LinkedList<double[]>();

        public OutputEnumeration() {

        }

        public boolean hasMoreElements() {
            return buffer.size() > 0 || !master.isEmpty();
        }

        public double[] nextElement() {
            if (buffer.isEmpty()) {
                try {
                    buffer.addAll(master.waitSomeResults());
                } catch (TaskException e) {
                    throw new RuntimeException(e);
                }
            }
            return buffer.poll();
        }
    }
}
