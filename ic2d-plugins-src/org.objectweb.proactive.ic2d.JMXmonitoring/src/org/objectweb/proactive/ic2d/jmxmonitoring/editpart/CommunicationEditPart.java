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
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.CommunicationFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;


/**
 * This class represents the controller part (edit part) of a communication object
 * @author vbodnart
 *
 */
public final class CommunicationEditPart extends AbstractConnectionEditPart implements Observer {
    private static final Color DEFAULT_ARROW_COLOR = new Color(Display.getCurrent(), 108, 108, 116);
    private static final int MAX_STROKE_WIDTH_RATIO = 12;
    private static final int MAX_STROKE_WIDTH_PROPORTIONAL = 80;

    private static float maxCommunicationCounter = 1;

    public static final DrawingStyle DEFAULT_STYLE = DrawingStyle.FIXED;

    public static DrawingStyle drawingStyle = DEFAULT_STYLE;

    private float communicationCounter = 1;

    private boolean active = true;

    public CommunicationEditPart(Communication model) {
        this.setModel(model);
    }

    public void update(Observable o, Object arg) {
        if ((arg != null) && (arg instanceof MVCNotification)) {
            final MVCNotificationTag mvcNotif = ((MVCNotification) arg).getMVCNotification();

            // State updated
            switch (mvcNotif) {

                // This message is sent by Communication Object when a communication
                // has been received.
                // Right now the feature has been disabled
                // See Communication class
                case ACTIVE_OBJECT_ADD_INCOMING_COMMUNICATION:
                case ACTIVE_OBJECT_ADD_OUTGOING_COMMUNICATION: {
                    this.communicationCounter = ((Communication) this.getModel()).getNbCalls();
                    ((CommunicationFigure) this.getFigure()).setLineWidth(drawingStyleSize());
                }
            }
        }
    }

    protected IFigure createFigure() {
        final PolylineConnection connection = new CommunicationFigure();
        connection.setTargetDecoration(new PolygonDecoration());
        connection.setForegroundColor(DEFAULT_ARROW_COLOR);
        return connection;
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
    }

    public void refresh() {
        // this.communicationCounter=((Communication)this.getModel()).getnumberOfCalls();

        // System.out.println("CommunicationEditPart.refresh()"+communicationCounter);

        // ((CommunicationFigure)this.getFigure()).setLineWidth(drawingStyleSize());

        // if ((getSourceConnectionAnchor().getOwner()==null)||
        // (getTargetConnectionAnchor().getOwner())==null)
        // {
        // // System.out.println("...................CommunicationEditPart: bug
        // here .........");
        // this.deactivateFigure();
        // active=false;
        // return;
        // }
        //	
        //	
        // if ((!active) && (getSourceConnectionAnchor().getOwner()!=null) &&
        // (getTargetConnectionAnchor().getOwner())!=null)
        // {
        // // System.out.println("...................CommunicationEditPart: bug
        // here .........");
        // this.activateFigure();
        // active=true;
        // return;
        // }

        if (active && ((this.getSource() == null) || (this.getTarget() == null))) {
            active = false;
            this.deactivateFigure();

        } else if (!active && (this.getSource() != null) && (this.getTarget() != null)) {
            active = true;
            this.activateFigure();
        }

        // RoundedLineConnection connection =
        // (RoundedLineConnection)this.getFigure();
        // if ((connection.getConnectionRouter() instanceof
        // NullConnectionRouter) &&((this.getSource()!=null) &&
        // (this.getTarget()!=null)))
        // {
        // AOFigure source = ((AOEditPart)this.getSource()).getCastedFigure();
        // AOFigure target = ((AOEditPart)this.getTarget()).getCastedFigure();
        // RoundedLineConnection c= ((RoundedLineConnection)this.getFigure());
        // // AOConnection.addRouterToConnection(c, source, target);
        //
        //		   
        //		   
        //	 
        // }
        //	 
        super.refresh();
    }

    protected int drawingStyleSize() {
        switch (drawingStyle) {
            case FIXED:
                return 1;
            case PROPORTIONAL:
                if (maxCommunicationCounter > MAX_STROKE_WIDTH_PROPORTIONAL) {
                    return (int) (((MAX_STROKE_WIDTH_PROPORTIONAL / maxCommunicationCounter) * communicationCounter) + 1);
                } else {
                    return (int) (communicationCounter + 1);
                }
            case RATIO:
                return (int) (((MAX_STROKE_WIDTH_RATIO / maxCommunicationCounter) * communicationCounter) + 1);
        }
        return 1;
    }

    public enum DrawingStyle {
        PROPORTIONAL, RATIO, FIXED;
    }

    public static void setDrawingStyle(DrawingStyle newDrawingStyle) {
        drawingStyle = newDrawingStyle;
    }

    /**
     * When an EditPart is added to the EditParts tree and when its figure is
     * added to the figure tree, the method EditPart.activate() is called.
     */
    @Override
    public void activate() {
        if (!isActive()) {
            ((Communication) super.getModel()).addObserver(this);
        }
        super.activate();
    }

    /**
     * When an EditPart is removed from the EditParts tree, the method
     * deactivate() is called.
     */
    @Override
    public void deactivate() {
        if (isActive()) {
            ((Communication) super.getModel()).deleteObserver(this);
        }
        super.deactivate();
    }
}
