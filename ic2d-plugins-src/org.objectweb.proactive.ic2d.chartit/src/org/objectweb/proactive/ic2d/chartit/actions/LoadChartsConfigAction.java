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
package org.objectweb.proactive.ic2d.chartit.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.editor.page.ChartsSectionWrapper;


/**
 * This action allows the user to load a saved previously saved charts configuration.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class LoadChartsConfigAction extends Action {

    /**
     * A name for this action
     */
    public static final String LOAD_CHARTS_CONFIG_ACTION = "Load Configuration";

    /**
     * The chart description handler
     */
    private final ChartsSectionWrapper chartsSW;

    /**
     * Creates an instance of this class, the chart section wrapper is used to update the gui part once
     * the configuration has been loaded. 
     * 
     * @param chartsSW The charts section wrapper
     */
    public LoadChartsConfigAction(final ChartsSectionWrapper chartsSW) {
        this.chartsSW = chartsSW;
        super.setId(LOAD_CHARTS_CONFIG_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/fldr_obj.gif"), null)));
        super.setToolTipText(LOAD_CHARTS_CONFIG_ACTION);
        super.setEnabled(true);
    }

    @Override
    public void run() {
        // Open a file dialog
        final FileDialog fileDialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.SAVE);
        fileDialog.setText("Load Configuration");
        fileDialog.setFilterExtensions(new String[] { "*.xml" });
        // Get path
        final String path = fileDialog.open();
        this.chartsSW.loadConfigFromXML(path);
    }
}
