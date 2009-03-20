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
package org.objectweb.proactive.ic2d.chartit.canvas;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.rrd4j.graph.RrdGraph;
import org.rrd4j.graph.RrdGraphDef;


/**
 * The canvas to show chart with RRD4J charting api.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class RRD4JChartCanvas extends Canvas implements PaintListener, ControlListener {

    /**
     * Default canvas width
     */
    public static final int DEFAULT_WIDTH = 400;

    /**
     * Default canvas height
     */
    public static final int DEFAULT_HEIGHT = 200;

    /**
     * The palette data used for building an ImageData
     */
    private static final PaletteData PALETTE_DATA = new PaletteData(0xFF0000, 0xFF00, 0xFF);

    /** The default crop width */
    public static final int DEFAULT_WIDTH_CROP = 80;

    /** The default crop height */
    public static final int DEFAULT_HEIGHT_CROP = 50;//30;

    /**
     * The image which caches the chart image to improve drawing performance.
     */
    protected Image cachedImage;

    /**
     * To know if we need to recompute the chart
     */
    public boolean recomputeChart;

    /**
     * The graph definition built once
     */
    private final RrdGraphDef graphDef;

    /**
     * Cached awt image
     */
    private BufferedImage cachedAwtImage;

    private ImageData swtImageData;

    /**
     * Constructs one canvas containing chart.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     */
    public RRD4JChartCanvas(final Composite parent, int style, final RrdGraphDef graphDef) {
        super(parent, style | SWT.NO_BACKGROUND);

        // Set default size 
        this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Set the default Layout data
        final GridData gd = new GridData();
        gd.widthHint = DEFAULT_WIDTH;
        gd.heightHint = DEFAULT_HEIGHT;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        this.setLayoutData(gd);

        // Initialize the chached image
        this.cachedImage = new Image(Display.getDefault(), DEFAULT_WIDTH, DEFAULT_HEIGHT);

        // Add listeners
        super.addPaintListener(this);
        super.addControlListener(this);

        this.graphDef = graphDef;
    }

    /**
     * 
     * @param left
     * @param right
     */
    public void updateTimeSpan(final long left, final long right) {
        // Update the time span
        this.graphDef.setTimeSpan(left, right);
        this.drawToCachedImage();
    }

    /**
     * Draws the chart onto the cached image in the area of the given
     * <code>Image</code>.
     */
    public void drawToCachedImage() {
        GC gc = null;
        try {
            final int boundsWidth = this.cachedImage.getBounds().width;
            final int boundsHeigth = this.cachedImage.getBounds().height;

            this.graphDef.setWidth(boundsWidth - DEFAULT_WIDTH_CROP);
            this.graphDef.setHeight(boundsHeigth - DEFAULT_HEIGHT_CROP);

            // Create the graph
            final RrdGraph graph = new RrdGraph(graphDef);

            // First Draw to an AWT Image then convert to SWT if bounds has changed
            if (this.cachedAwtImage == null || this.cachedAwtImage.getWidth() != boundsWidth) {
                this.cachedAwtImage = new BufferedImage(boundsWidth, boundsHeigth, BufferedImage.TYPE_INT_RGB);
                // We can force bit depth to be 24 bit because BufferedImage getRGB allows us to always
                // retrieve 24 bit data regardless of source color depth.
                this.swtImageData = new ImageData(boundsWidth, boundsHeigth, 24, PALETTE_DATA);
            }
            // Render the graph
            graph.render(cachedAwtImage.getGraphics());

            // Dispose the precedent cachedImage
            if (this.cachedImage != null) {
                this.cachedImage.dispose();
            }

            // Convert from BufferedImage to Image                      
            this.cachedImage = convert(cachedAwtImage, swtImageData);

            // Draw the image in the gc
            gc = new GC(this.cachedImage);

            // Manually erase gray contours
            gc.setForeground(ColorConstants.white);
            gc.setLineWidth(3);
            gc.drawRectangle(0, 0, boundsWidth - 1, boundsHeigth - 1);

        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            if (gc != null)
                gc.dispose();
        }
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
            this.drawToCachedImage();
            this.recomputeChart = false;
        }

        e.gc.drawImage(this.cachedImage, 0, 0);
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

    /**
     * Converts an AWT based buffered image into an SWT <code>Image</code>.  This will always return an
     * <code>Image</code> that has 24 bit depth regardless of the type of AWT buffered image that is 
     * passed into the method.
     * 
     * @param srcImage the {@link java.awt.image.BufferedImage} to be converted to an <code>Image</code>
     * @param swtImageData the {@link org.eclipse.swt.graphics.ImageData} used as target
     * @return an <code>Image</code> that represents the same image data as the AWT 
     * <code>BufferedImage</code> type.
     */
    public static Image convert(final BufferedImage srcImage, final ImageData swtImageData) {
        final int w = srcImage.getWidth();
        final int h = srcImage.getHeight();

        // ensure scansize is aligned on 32 bit.
        final int scansize = (((w * 3) + 3) * 4) / 4;

        final WritableRaster alphaRaster = srcImage.getAlphaRaster();
        final byte[] alphaBytes = new byte[w];
        int[] buff, alpha;
        for (int y = h; y-- > 0;) {
            buff = srcImage.getRGB(0, y, w, 1, null, 0, scansize);
            swtImageData.setPixels(0, y, w, buff, 0);

            // check for alpha channel
            if (alphaRaster != null) {
                alpha = alphaRaster.getPixels(0, y, w, 1, (int[]) null);
                for (int i = w; i-- > 0;)
                    alphaBytes[i] = (byte) alpha[i];
                swtImageData.setAlphas(0, y, w, alphaBytes, 0);
            }
        }

        return new Image(PlatformUI.getWorkbench().getDisplay(), swtImageData);
    }
}
