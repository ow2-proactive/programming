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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.timit.editors.canvas;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;


/**
 * The canvas to show chart.
 * @author The ProActive Team
 */
public final class GenericChartCanvas extends Canvas implements PaintListener, ControlListener {

    /**
     * Default canvas width
     */
    public static final int DEFAULT_WIDTH = 400;

    /**
     * Default canvas height
     */
    public static final int DEFAULT_HEIGHT = 200;

    /**
     * The image which caches the chart image to improve drawing performance.
     */
    protected Image cachedImage;

    /**
     * To know if we need to recompute the chart
     */
    public boolean recomputeChart;

    /**
     * The device render for rendering chart.
     */
    protected IDeviceRenderer render;

    /**
     * The chart instance.
     */
    protected Chart chart;

    /**
     * The scale resolution used for chart rendering 
     */
    private final double scaleResolution;

    /**
     * Constructs one canvas containing chart.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public GenericChartCanvas(final Composite parent, final int style, final Chart chart) {
        // SWT.NO_BACKGROUND is used to avoid flickering
        super(parent, style | SWT.NO_BACKGROUND);

        this.chart = chart;

        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Initialize the cached image
        this.cachedImage = new Image(Display.getDefault(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Add listeners
        super.addPaintListener(this);
        super.addControlListener(this);

        // initialize the SWT rendering device               
        try {
            this.render = PluginSettings.instance().getDevice("dv.SWT");
        } catch (ChartException ex) {
            ex.printStackTrace();
        }
        // Compute the scale resolution 
        this.scaleResolution = 72d / this.render.getDisplayServer().getDpiResolution();
    }

    /**
     * Called when the canvas is repaint
     */
    public final void paintControl(final PaintEvent e) {
        final Composite co = (Composite) e.getSource();
        final Rectangle rect = co.getClientArea();

        if (this.recomputeChart) {
            // Dispose the precedent image
            this.cachedImage.dispose();
            // Then create another
            this.cachedImage = new Image(co.getDisplay(), rect);

            // Chart rendering be called when data is changed.
            // May be slow : 20-60ms Intel(R) Core(TM)2 Duo CPU E4400 @ 2.00GHz JVM 1.5 64bits               
            final Point size = super.getSize();
            final Bounds bo = BoundsImpl.create(0, 0, size.x, size.y);
            bo.scale(this.scaleResolution);
            // The cached image must not be null
            final GC gcImage = new GC(this.cachedImage);
            this.render.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gcImage);
            final Generator gr = Generator.instance();
            try {
                final GeneratedChartState state = gr.build(this.render.getDisplayServer(), this.chart, bo,
                        null, null, null);
                gr.render(this.render, state);
            } catch (ChartException ex) {
                ex.printStackTrace();
            } finally {
                gcImage.dispose();
            }

            this.recomputeChart = false;
        }
        e.gc.drawImage(this.cachedImage, 0, 0);
    }

    /**
     * Returns the chart which is contained in this canvas.
     * 
     * @return the chart contained in this canvas.
     */
    public Chart getChart() {
        return this.chart;
    }

    /**
     * Sets the chart into this canvas. Note: When the chart is set, the cached
     * image will be dropped, but this method doesn't reset the flag
     * <code>cachedImage</code>.
     * 
     * @param chart
     *            the chart to set
     */
    public void setChart(final Chart chart) {
        if (this.cachedImage != null)
            this.cachedImage.dispose();

        this.cachedImage = null;
        this.chart = chart;
    }

    /**
     * Called when the control is resized
     */
    public final void controlResized(final ControlEvent e) {
        // Recompute chart
        this.recomputeChart = true;
        // Redraw
        this.redraw();
    }

    /**
     * Controls cannot be moved for the moment
     */
    public final void controlMoved(final ControlEvent e) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
        if (this.cachedImage != null)
            this.cachedImage.dispose();
        super.dispose();
    }

}
