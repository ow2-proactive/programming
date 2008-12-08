package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class DebugStepByStepEnableAction extends AbstractStepByStepAction {

    public static final String DEBUGSTEPBYSTEPENABLE = "DEBUG_STEPBYSTEP01_Enable Step by Step mode";

    public DebugStepByStepEnableAction() {
        super.setId(DEBUGSTEPBYSTEPENABLE);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                    "icons/pause.gif"), null)));
        super.setText("Enable Step by Step mode");
        super.setToolTipText("Enable Step by Step mode");
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
        if (object instanceof WorldObject) {
            super.setEnabled(false);
            return;
        }
        this.target = object;
        if (target instanceof ActiveObject) {
            if (((ActiveObject) target).getDebugInfo().isStepByStepMode()) {
                super.setText("Disable Step by Step in " + object.getName());
                super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                        org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                            "icons/resume.gif"), null)));
            } else {
                super.setText("Enable Step by Step in " + object.getName());
                super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                        org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                            "icons/pause.gif"), null)));
            }
        } else {
            super.setText("Enable Step by Step in " + object.getName());
            super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                    org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                        "icons/pause.gif"), null)));
        }
        super.setEnabled(true);
    }

    @Override
    protected void executeAction(ActiveObject obj) {
        if (obj.getDebugInfo().isStepByStepMode()) {
            obj.disableStepByStep();
        } else {
            obj.enableStepByStep();
        }
    }

}
