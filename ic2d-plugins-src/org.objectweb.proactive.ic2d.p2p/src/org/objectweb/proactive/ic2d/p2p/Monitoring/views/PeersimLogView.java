package org.objectweb.proactive.ic2d.p2p.Monitoring.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.p2p.Monitoring.gui.PeersimGroup;


public class PeersimLogView extends ViewPart implements IPerspectiveListener4 {

    static public PeersimGroup group;

    public void createPartControl(Composite parent) {
        // Slider s = new Slider(parent, SWT.HORIZONTAL);
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);
        group = new PeersimGroup(parent, SWT.SHADOW_NONE);
    }

    public void perspectivePreDeactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    }

    public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
            IPerspectiveDescriptor newPerspective) {

    }

    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
            IWorkbenchPartReference partRef, String changeId) {

    }

    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {

    }

    public void setFocus() {

    }

}
