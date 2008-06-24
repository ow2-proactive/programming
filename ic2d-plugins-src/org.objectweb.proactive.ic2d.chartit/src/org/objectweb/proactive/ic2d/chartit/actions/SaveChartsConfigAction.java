/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chartit.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.chartit.Activator;
import org.objectweb.proactive.ic2d.chartit.editor.page.ChartsSectionWrapper;


/**
 * 
 * @author vbodnart
 * 
 */
public final class SaveChartsConfigAction extends Action {

    /**
     * A name for this action
     */
    public static final String SAVE_CHARTS_CONFIG_ACTION = "Save Current Configuration";

    /**
     * The charts section wrapper
     */
    private final ChartsSectionWrapper chartsSW;

    /**
     * 
     * @param chartModelContainer
     */
    public SaveChartsConfigAction(final ChartsSectionWrapper chartsSW) {
        this.chartsSW = chartsSW;
        super.setId(SAVE_CHARTS_CONFIG_ACTION);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/save_edit.gif"), null)));
        super.setToolTipText(SAVE_CHARTS_CONFIG_ACTION);
        super.setEnabled(true);
    }

    @Override
    public void run() {
        this.chartsSW.saveConfigToXML();
    }
}
