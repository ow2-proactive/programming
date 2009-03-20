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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.chartit.data.ChartModel;
import org.objectweb.proactive.ic2d.chartit.data.IChartModelListener;


/**
 * Uses canvas, non-GEF based implementation.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>. 
 */
public abstract class AbstractChartItEditPart<C extends Canvas> implements IChartItEditPart {

    /**
     * The model of this edit part
     */
    protected final ChartModel chartModel;

    /**
     * The used canvas
     */
    protected C canvas;

    /**
     * Creates an new instance of edit part with the specified model
     * @param chartModel
     */
    public AbstractChartItEditPart(final ChartModel dataElementModel) {
        this.chartModel = dataElementModel;
    }

    /**
     * Subclass must fill the client with a specific canvas.
     * Warning ! 
     */
    public abstract void fillSWTCompositeClient(final Composite client, final int style);

    /**
     * Activates this edit part and registers a dispose listener to the canvas
     */
    public void init() {
        // Activate this edit part
        this.activate();

        // Add a dispose listener to deactivate this edit part
        this.canvas.addDisposeListener(new DisposeListener() {
            public final void widgetDisposed(final DisposeEvent e) {
                deactivate();
            }
        });
    }

    /**
     * Returns the model associated to this edit part.
     * 
     * @return The chart model
     */
    public ChartModel getModel() {
        return this.chartModel;
    }

    /**
     * Returns the canvas associated to this edit part.
     * 
     * @return The canvas
     */
    public C getCanvas() {
        return this.canvas;
    }

    /**
     * Activates this edit part by adding it as a listener of the chart model
     */
    public void activate() {
        this.chartModel.setChartModelListener(this);
    }

    /**
     * Deactivates this edit part by removing it as a listener from the chart model
     */
    public void deactivate() {
        this.chartModel.unSetChartModelListener();
    }

    /**
     * Called when the chart model has changed
     */
    public void modelChanged(int type, Object oldValue, Object newValue) {
        if (type == IChartModelListener.CHANGED) {
            Display.getDefault().asyncExec(this);
        }
    }
}
