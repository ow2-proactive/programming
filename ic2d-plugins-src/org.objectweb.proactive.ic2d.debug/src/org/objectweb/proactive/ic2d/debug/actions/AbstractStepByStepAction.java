package org.objectweb.proactive.ic2d.debug.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


public abstract class AbstractStepByStepAction extends Action implements IActionExtPoint {

    /** The target data */
    protected AbstractData<?, ?> target;

    public AbstractStepByStepAction(String str, int opt) {
        super(str, opt);
    }

    public AbstractStepByStepAction() {
        super();
    }

    public void setActiveSelect(AbstractData<?, ?> ref) {
        // Nothing to do for the moment when the target get the focus
    }

    /**
     * Action to do when action invoked
     */
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        if (target instanceof ActiveObject) {
            executeAction((ActiveObject) target);
        } else {
            explore((List<AbstractData>) target.getMonitoredChildrenAsList());
        }
    }

    /**
     * If target is a container, explore the child and enable the step by step mode for the active
     * object contained.
     */
    @SuppressWarnings("unchecked")
    protected void explore(List<AbstractData> exploreList) {
        if (exploreList != null && exploreList.size() > 0) {
            for (AbstractData obj : exploreList) {
                if (obj instanceof ActiveObject) {
                    executeAction((ActiveObject) obj);
                } else {
                    explore(obj.getMonitoredChildrenAsList());
                }
            }
        }
    }

    abstract protected void executeAction(ActiveObject obj);

}
