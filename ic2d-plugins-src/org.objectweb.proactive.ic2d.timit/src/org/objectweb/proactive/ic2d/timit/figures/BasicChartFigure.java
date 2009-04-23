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
package org.objectweb.proactive.ic2d.timit.figures;

import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.ui.util.ChartUIUtil;
import org.eclipse.birt.chart.util.PluginSettings;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;


public class BasicChartFigure extends Figure {
    public final static Color UNSELECTED_BORDER_COLOR = Display.getCurrent().getSystemColor(
            SWT.COLOR_DARK_GRAY);
    public final static Color SELECTED_BORDER_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_RED);

    public final static Color WHITE_BACKGROUND_COLOR = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    private final static int UNSELECTED_BORDER_SIZE = 2;
    private final static int SELECTED_BORDER_SIZE = 4;

    private final static double SCALED_VALUE = 72d / ChartUIUtil.getDisplayServer().getDpiResolution();

    ////////////////////////////////////////////////////////
    private Chart chart;
    private Color currentBorderColor;
    private int currentBorderSize;
    private final Bounds bo;

    private Image imgChart;
    private IDeviceRenderer idr;

    /**
     * To know if we need to recompute the chart
     */
    public boolean recomputeChart;

    public BasicChartFigure(final Chart chart) {
        this.chart = chart;

        this.currentBorderColor = UNSELECTED_BORDER_COLOR;
        this.currentBorderSize = UNSELECTED_BORDER_SIZE;

        this.imgChart = new Image(Display.getDefault(), 200, 100);

        this.bo = chart.getBlock().getBounds().scaledInstance(SCALED_VALUE);

        this.recomputeChart = true;

        try {
            idr = PluginSettings.instance().getDevice("dv.SWT");
        } catch (Exception pex) {
            pex.printStackTrace();
        }
    }

    /**
     *
     * @param chart
     */
    public final void setChart(final Chart chart) {
        this.chart = chart;
        this.recomputeChart = true;
        // Since the chart has changed we need to compute a new ChartState 
        this.repaint();
    }

    public final void setSelected() {
        this.currentBorderColor = SELECTED_BORDER_COLOR;
        this.currentBorderSize = SELECTED_BORDER_SIZE;
        this.recomputeChart = false;
        this.repaint();
    }

    public final void setUnselected() {
        this.currentBorderColor = UNSELECTED_BORDER_COLOR;
        this.currentBorderSize = UNSELECTED_BORDER_SIZE;
        this.recomputeChart = false;
        this.repaint();
    }

    /**
     * The paintFigure method that handles all
     * painting system.
     */
    @Override
    protected final void paintFigure(final Graphics graphics) {
        final Rectangle r = getClientArea();
        if ((r.width <= 0) || (r.height <= 0)) {
            return;
        }

        if (this.recomputeChart) {
            // Dispose the precedent image
            this.imgChart.dispose();
            // Then create another
            this.imgChart = new Image(Display.getCurrent(), r.width, r.height);

            // Chart rendering be called when data is changed.
            // May be slow : 20-60ms Intel(R) Core(TM)2 Duo CPU E4400 @ 2.00GHz JVM 1.5 64bits               
            // RESCALE THE RENDERING AREA
            this.bo.setWidth(getClientArea().width);
            this.bo.setHeight(getClientArea().height);
            this.bo.scale(SCALED_VALUE);
            // The cached image must not be null
            final GC gcImage = new GC(this.imgChart);
            this.idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gcImage);
            final Generator gr = Generator.instance();
            try {
                final GeneratedChartState state = gr.build(this.idr.getDisplayServer(), this.chart, bo, null,
                        null, null);
                gr.render(this.idr, state);
            } catch (ChartException ex) {
                ex.printStackTrace();
            } finally {
                gcImage.dispose();
            }

            this.recomputeChart = false;
        }

        if (imgChart != null) {
            graphics.drawImage(imgChart, r.x, r.y);
        }
        graphics.setLineWidth(this.currentBorderSize);
        graphics.setForegroundColor(this.currentBorderColor);
        graphics.drawRectangle(r.x + (this.currentBorderSize / 2), r.y + (this.currentBorderSize / 2),
                r.width - this.currentBorderSize, r.height - this.currentBorderSize);
    }
}
