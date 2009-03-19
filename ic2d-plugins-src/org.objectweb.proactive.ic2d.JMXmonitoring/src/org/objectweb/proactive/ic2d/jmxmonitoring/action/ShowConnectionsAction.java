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
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Color;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AbstractFigure;


public class ShowConnectionsAction extends Action {

    public static final String SHOW_CONNECTIONS = "Show connections";
    private AbstractFigure object;

    public ShowConnectionsAction() {
        this.setId(SHOW_CONNECTIONS);
        // this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "stop_monitoring.gif"));
    }

    public void setObject(AbstractFigure figure) {
        this.object = figure;
        String showOrHide = "Hide";
        if (!object.getShowConnections())
            showOrHide = "Show";
        this.setText(showOrHide + " connections for this object");
        this.setToolTipText(showOrHide + "  connections for this object");
    }

    @Override
    public void run() {
        object.switchShowConnections();
        if (object.getShowConnections()) {
            object.setHighlight(new Color(AOFigure.device, 150, 0, 255));
        } else
            object.setHighlight(null);
    }

}
