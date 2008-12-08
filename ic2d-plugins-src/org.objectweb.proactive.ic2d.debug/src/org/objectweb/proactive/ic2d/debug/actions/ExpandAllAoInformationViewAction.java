package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.debug.views.AOInformationView;


/**
 * Clear the {@link AOInformationView} Tree by removing all Active Object from it
 */
public class ExpandAllAoInformationViewAction extends Action implements IWorkbenchWindowActionDelegate {

    public static final String DEBUGEXPAND = "DEBUGVIEW_01_Expand";

    private AOInformationView view;

    public ExpandAllAoInformationViewAction(AOInformationView view) {
        super.setId(DEBUGEXPAND);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/expand_all.png"), null)));
        super.setText("Expand all Active Objects");
        super.setToolTipText("Expand all Active Objects");
        this.view = view;
    }

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
        view.expandAll();
    }
}
