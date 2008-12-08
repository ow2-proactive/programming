package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.debug.dialogs.StepByStepDelayDialog;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IWorkbenchWindowsActionExtPoint;


public class SetSlowMotionAction extends Action implements IWorkbenchWindowActionDelegate,
        IWorkbenchWindowsActionExtPoint {

    public static final boolean DEFAULT_DELAYING = false;
    public static final String SET_STEPBYSTEPDELAY = "DEBUG_STEPBYSTEP05_Set Step By Step Delay";

    private boolean stepdelaying = DEFAULT_DELAYING;
    private String enableMessage = "The step by step motion is Enabled";
    private String disableMessage = "The step by step motion is Disabled";

    /*  The world */
    private WorldObject world;

    public SetSlowMotionAction() {
        super("Enable Slow Motion", AS_CHECK_BOX);
        this.setId(SET_STEPBYSTEPDELAY);
        this.setText("Set Step By Step Delay");
        setToolTipText("Set Step By Step Delay");
        this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                    "icons/clock.png"), null)));
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void dispose() { /* Do nothing */
    }

    public void init(IWorkbenchWindow window) { /* Do nothing */
    }

    public void run(IAction action) {
        this.run();
    }

    public void selectionChanged(IAction action, ISelection selection) { /* Do nothing */
    }

    @Override
    public void run() {
        updateStepDelayState();
        stepdelaying = !stepdelaying;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void updateStepDelayState() {
        if (stepdelaying) {
            this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                    org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                        "icons/clock.png"), null)));
            Console.getInstance(Activator.CONSOLE_NAME).debug(disableMessage);
            this.setText("Set Step By Step Delay");
            setToolTipText("Set Step By Step Delay");
            for (ActiveObject a : world.getAOChildren()) {
                a.slowMotion(0);
            }
        } else {
            this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                    org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                        "icons/clock_on.png"), null)));
            Console.getInstance(Activator.CONSOLE_NAME).debug(enableMessage);
            this.setText("Step By Step Slow Motion in progress...");
            setToolTipText("Step By Step Slow Motion in progress...");
            new StepByStepDelayDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), world);
        }
    }

    public void setWorldObject(WorldObject object) {
        this.world = object;
    }

}
