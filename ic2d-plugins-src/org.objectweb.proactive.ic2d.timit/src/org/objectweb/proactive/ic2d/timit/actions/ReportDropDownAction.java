/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.timit.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.objectweb.proactive.ic2d.timit.Activator;
import org.objectweb.proactive.ic2d.timit.util.ExecuteReport;
import org.objectweb.proactive.ic2d.timit.views.TimItView;


public final class ReportDropDownAction extends Action implements IMenuCreator {
    private final TimItView timItView;

    private Menu fMenu;

    public ReportDropDownAction(final TimItView timItView) {
        this.timItView = timItView;
        setMenuCreator(this);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/generate_report.gif"), null)));
    }

    public Menu getMenu(Menu parent) {
        return null;
    }

    public Menu getMenu(Control parent) {
        if (fMenu != null)
            fMenu.dispose();
        fMenu = new Menu(parent);
        // Create an item for html report
        new ActionContributionItem(new GenerateReportAndSaveAction(this.timItView, ExecuteReport.HTML_FORMAT))
                .fill(fMenu, -1);
        // Create an item for pdf report
        new ActionContributionItem(new GenerateReportAndSaveAction(this.timItView, ExecuteReport.PDF_FORMAT))
                .fill(fMenu, -1);
        return fMenu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.IMenuCreator#dispose()
     */
    public void dispose() {
        if (fMenu != null) {
            fMenu.dispose();
            fMenu = null;
        }
    }
}