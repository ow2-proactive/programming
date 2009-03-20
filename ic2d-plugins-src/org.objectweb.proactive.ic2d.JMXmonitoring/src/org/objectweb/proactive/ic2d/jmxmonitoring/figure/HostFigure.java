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
package org.objectweb.proactive.ic2d.jmxmonitoring.figure;

import java.util.List;

import org.eclipse.draw2d.BorderLayout;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;


public class HostFigure extends AbstractRectangleFigure {
    protected final static int DEFAULT_WIDTH = 25;
    private IFigure contentPane;
    protected String title = "";
    private final static Color DEFAULT_BORDER_COLOR;

    static {
        Display device = Display.getCurrent();
        DEFAULT_BORDER_COLOR = new Color(device, 0, 0, 128);
    }

    //
    // -- CONSTRUCTOR -----------------------------------------------
    //
    public HostFigure(String text) {
        super(text);
    }

    /**
     * Used to display the legend.
     *
     */
    public HostFigure() {
        super();
    }

    //
    // -- PUBLIC METHOD --------------------------------------------
    //
    public IFigure getContentPane() {
        return contentPane;
    }

    //
    // -- PROTECTED METHODS --------------------------------------------
    //
    protected void initColor() {
        Device device = Display.getCurrent();
        borderColor = DEFAULT_BORDER_COLOR;
        backgroundColor = new Color(device, 208, 208, 208);
        shadowColor = new Color(device, 230, 230, 230);
    }

    protected void initFigure() {
        BorderLayout layout = new HostBorderLayout();
        setLayoutManager(layout);
        add(label, BorderLayout.TOP);
        //        contentPane = new Figure();
        //        DynamicGridLayout contentPaneLayout = new DynamicGridLayout("Host",this);
        //        contentPaneLayout.setSpacing(5);
        //        contentPaneLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
        //        contentPane.setLayoutManager(contentPaneLayout);

        contentPane = new GridContentPane("Host");

        add(contentPane, BorderLayout.CENTER);
    }

    @Override
    protected int getDefaultWidth() {
        return DEFAULT_WIDTH;
    }

    @Override
    protected Color getDefaultBorderColor() {
        return null;
    }

    public void changeTitle(String newTitle) {
        String text = getTextResized(newTitle);
        this.title = text;
        setToolTip(new ToolTipFigure(newTitle));
        Display.getDefault().asyncExec(this);
    }

    @Override
    public void run() {
        setTitle(title);
        repaint();
    }

//    @Override
//    protected void paintIC2DFigure(Graphics graphics) {
//
//        //        int nb_colums = new Double(Math.sqrt(contentPane.getChildren().size())).intValue();
//        //        nb_colums = Math.max(nb_colums, 1);
//        //        ((GridLayout) contentPane.getLayoutManager()).numColumns = nb_colums;
//        contentPane.getLayoutManager().layout(contentPane);
//        this.invalidateTree();
//
//        super.paintIC2DFigure(graphics);
//    }

    //
    // -- INNER CLASS --------------------------------------------
    //
    private class HostBorderLayout extends BorderLayout {

        public HostBorderLayout() {

        }

        protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
            if (legend) {
                return super.calculatePreferredSize(container, wHint, hHint).expand( /*100*/
                50, /*10*/
                0);
            }
            return super.calculatePreferredSize(container, wHint, hHint).expand(10, 0);
        }

    }

}
