package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ProActiveNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


public class DebugConnectAction extends Action implements IActionExtPoint {

    public static final String DEBUGCONNECT = "DEBUG_10_Connect a debugger";

    private DebugSocketConnection dsc;

    public DebugConnectAction() {
        super.setId(DEBUGCONNECT);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/bug_link.png"), null)));
        super.setText("Connect a debugger");
        super.setToolTipText("Connect a debugger");
        super.setEnabled(false);
    }

    @Override
    public void run() {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                if (dsc.hasDebuggerConnected()) {
                    dsc.removeDebugger();
                    Console.getInstance(Activator.CONSOLE_NAME).log("Debugger connection tunnel closed.");
                } else {
                    dsc.connectSocketDebugger();
                }
            }
        });
    }

    public void setAbstractDataObject(AbstractData<?, ?> object) {
        RuntimeObject runtime = null;
        if (object.getClass().getName().equals(RuntimeObject.class.getName())) {
            runtime = (RuntimeObject) object;
        }
        if (object.getClass().getName().equals(ProActiveNodeObject.class.getName())) {
            runtime = (RuntimeObject) object.getParent();
        }
        if (object.getClass().getName().equals(ActiveObject.class.getName())) {
            runtime = (RuntimeObject) object.getParent().getParent();
        }
        if (runtime != null && runtime.canBeDebugged()) {
            dsc = new DebugSocketConnection(runtime);
            // Display the runtime name of this object where we will be connected
            // with the debugger
            if (dsc.hasDebuggerConnected()) {
                super.setText("Disconnect a debugger on " + runtime.getName());
            } else {
                super.setText("Connect a debugger on " + runtime.getName());
            }
            super.setEnabled(true);
        } else {
            super.setEnabled(false);
        }

    }

    public void setActiveSelect(AbstractData<?, ?> ref) {
        // TODO Auto-generated method stubs
    }

}