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
package org.objectweb.proactive.ic2d.chartit.editpart;

import org.eclipse.swt.widgets.Composite;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.IChartModelListener;


public interface IChartItEditPart extends IChartModelListener, Runnable {

    /**
     * Returns the model
     * @return
     */
    public ChartModel getModel();

    /**
     * For example subclasses may use GEF based elements (if so direct sub class must extends SimpleRootEditPart),
     * override this method like :
     * <code>
     *   final Canvas c = new Canvas(client, SWT.FILL);
     *   final GraphicalViewerImpl gv = new GraphicalViewerImpl();
     *   gv.setControl(c);       
     *   gv.setRootEditPart((SimpleRootEditPart)ep);
     * </code>
     */
    public void fillSWTCompositeClient(final Composite client, final int style);

    public void activate();

    public void deactivate();
}
