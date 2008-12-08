package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class DebugStepByStepDisableAction extends AbstractStepByStepAction {

    public static final String DEBUGSTEPBYSTEPDISABLE = "DEBUG_STEPBYSTEP09_Disable Step by Step mode";

    public DebugStepByStepDisableAction() {
        super.setId(DEBUGSTEPBYSTEPDISABLE);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/resume.gif"), null)));
        super.setText("Disable Step by Step mode");
        super.setToolTipText("Disable Step by Step mode");
        super.setEnabled(false);
    }

    /**
     * Set the action's target and set the correct display Text of the contextual menu action.
     * Enable or Disable for an active object, or only Enable for a container ( node, jvm, host,
     * world ).
     *
     * @param object - the action's target
     */
    public void setAbstractDataObject(AbstractData<?, ?> object) {
        if (object instanceof WorldObject || object instanceof ActiveObject) {
            super.setEnabled(false);
            return;
        }
        this.target = object;
        super.setText("Disable Step by Step in " + object.getName());
        super.setEnabled(true);
    }

    @Override
    protected void executeAction(ActiveObject obj) {
        obj.disableStepByStep();
    }

}
