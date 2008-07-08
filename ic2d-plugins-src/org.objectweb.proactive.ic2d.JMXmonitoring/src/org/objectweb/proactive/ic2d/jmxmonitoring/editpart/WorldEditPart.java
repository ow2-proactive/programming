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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.WorldListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public final class WorldEditPart extends AbstractMonitoringEditPart<WorldObject> {

    public static final boolean DEFAULT_DISPLAY_TOPOLOGY = true;
    private static boolean displayTopology = DEFAULT_DISPLAY_TOPOLOGY;

    private FreeformLayer layer;
    private final MonitoringView monitoringView;
    private IFigure castedFigure;

    /*
     * A repaint will be done each TIME_TO_REFRESH mls 
     */
    private static int TIME_TO_REPAINT = 200;

    /*
     * refreshMode=FULL -> a refresh is asked for each event 
     * refreshMode=OPTIMAL -> a refresh is done by the WorldEditPart each TIME_TO_REPAINT mls
     */
    public enum RefreshMode {
        FULL, OPTIMAL
    };

    private RefreshMode mode = RefreshMode.OPTIMAL;

    private boolean shouldRepaint = true;

    private final Runnable drawRunnable = new Runnable() {
        public final void run() {
            getFigure().repaint();

        }
    };

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public WorldEditPart(final WorldObject model, final MonitoringView monitoringView) {
        super(model);
        this.monitoringView = monitoringView;

        new Thread() {
            @Override
            public final void run() {
                try {
                    Control control;
                    while (shouldRepaint) {
                        Thread.sleep(TIME_TO_REPAINT);

                        control = getViewer().getControl();
                        if (control != null) {
                            control.getDisplay().syncExec(WorldEditPart.this.drawRunnable);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    @SuppressWarnings("unchecked")
    @Override
    public IFigure getCastedFigure() {
        if (this.castedFigure == null) {
            this.castedFigure = getFigure();
        }
        return this.castedFigure;
    }

    @Override
    public IFigure getContentPane() {
        return this.layer;
    }

    @Override
    public MonitoringView getMonitoringView() {
        return this.monitoringView;
    }

    @Override
    public WorldEditPart getWorldEditPart() {
        return this;
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated
     * with the type of model object the
     * EditPart is associated with. So here, it returns a new FreeFormLayer.
     * @return a new FreeFormLayer view associated with the WorldObject model.
     */
    @Override
    protected IFigure createFigure() {
        layer = new FreeformLayer();

        FlowLayout layout = /*new FlowLayout()*/new MonitoringLayout();
        layout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
        layout.setMajorSpacing(50);
        layout.setMinorSpacing(50);
        layer.setLayoutManager(layout);

        WorldListener listener = new WorldListener(monitoringView);
        layer.addMouseListener(listener);
        layer.addMouseMotionListener(listener);

        return layer;
    }

    /**
     * Creates the initial EditPolicies and/or reserves slots for dynamic ones.
     */
    @Override
    protected void createEditPolicies() { /* Do nothing */
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    private class MonitoringLayout extends FlowLayout {
        @Override
        protected void setBoundsOfChild(IFigure parent, IFigure child, Rectangle bounds) {
            parent.getClientArea(Rectangle.SINGLETON);
            bounds.translate(Rectangle.SINGLETON.x, Rectangle.SINGLETON.y);// + 100);
            child.setBounds(bounds);
        }
    }

    public RefreshMode getRefreshMode() {
        return this.mode;
    }

    public void setRefreshMode(RefreshMode m) {
        this.mode = m;
    }

    /**
     * To choose if you want to show the topology.
     */
    public static void setDisplayTopology(boolean show) {
        displayTopology = show;
    }

    /**
     * Indicates if the topology must be displayed.
     */
    public static boolean displayTopology() {
        return displayTopology;
    }

}
