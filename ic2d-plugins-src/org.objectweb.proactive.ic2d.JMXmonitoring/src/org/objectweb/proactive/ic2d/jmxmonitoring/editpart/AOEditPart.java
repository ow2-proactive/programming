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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication;
import org.objectweb.proactive.ic2d.jmxmonitoring.editpart.WorldEditPart.RefreshMode;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.AOFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.AOListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


public class AOEditPart extends AbstractMonitoringEditPart<ActiveObject> implements NodeEditPart {

    /**
     * The default color of an arrow used in <code>getArrowColor()</code>
     * method of this class
     */
    public static final Color DEFAULT_ARROW_COLOR = new Color(Display.getCurrent(), 108, 108, 116);

    /**
     * The anchor used to hook connections for communications representation
     */
    private ConnectionAnchor anchor;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new instance of the class
     * 
     * @param model
     *            The active object model
     */
    public AOEditPart(ActiveObject model) {
        super(model);
        // Initialize lists for source and target connections edit parts
        super.sourceConnections = new ArrayList<CommunicationEditPart>(10);
        super.targetConnections = new ArrayList<CommunicationEditPart>(10);
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //

    /**
     * This method is called whenever the observed object is changed. It calls
     * the method <code>refresh()</code>.
     * 
     * @param o
     *            the observable object (instance of AbstractDataObject).
     * @param arg
     *            an argument passed to the notifyObservers method.
     */
    @Override
    public void update(Observable o, Object arg) {
        if ((arg != null) && (arg instanceof MVCNotification)) {
            final MVCNotificationTag mvcNotif = ((MVCNotification) arg).getMVCNotification();
            final Object notificationdata = ((MVCNotification) arg).getData();

            // State updated
            switch (mvcNotif) {
                case STATE_CHANGED: {
                    final State state = (State) notificationdata;
                    if (state == State.NOT_MONITORED) {
                        // getCastedModel().removeAllConnections();
                        // getCastedFigure().removeConnections(getGlobalPanel());

                        // If this clear is too brutal just filter on this
                        // (source||tagret) on clear
                        // if (getWorldEditPart().getModel() == RefreshMode.FULL)
                        // getWorldEditPart().clearCommunicationsAndRepaintFigure();

                    } else {
                        // COMMON REFRESH IE THE AO IS MONITORED JUST DO A REPAINT
                        // Set the new state directly
                        getCastedFigure().setState(state);
                        // The state of this controller has changed repaint at the
                        // next
                        // reasonable opportunity
                        if (getWorldEditPart().getRefreshMode() == RefreshMode.FULL)
                            getViewer().getControl().getDisplay().syncExec(new Runnable() {
                                public final void run() {
                                    // getCastedFigure().refresh();
                                    getCastedFigure().repaint();
                                }
                            });
                    } // if else NOT_MONITORED
                    break;
                } // case STATE_CHANGED
                case ACTIVE_OBJECT_ADD_OUTGOING_COMMUNICATION: {
                    // The outgoing communication to add is in the notification
                    final Communication communicationToAdd = (Communication) notificationdata;
                    // IMPORTANT NOTE : Usually the Thread that executes this code
                    // is NOT an SWT Thread.
                    // Therefore we have to submit a runnable that will be executed
                    // by the SWT Thread.
                    Display.getDefault().asyncExec(new Runnable() {
                        @SuppressWarnings("unchecked")
                        public final void run() {
                            // Create new connection edit part
                            final ConnectionEditPart connection = createOrFindConnection(communicationToAdd);
                            sourceConnections.add(connection);
                            final AOEditPart source = (AOEditPart) connection.getSource();
                            if (source != null)
                                source.getSourceConnections().remove(connection);
                            connection.setSource(AOEditPart.this);
                        }
                    });
                    break;
                }
                case ACTIVE_OBJECT_ADD_INCOMING_COMMUNICATION: {
                    // The incoming communication to add is in the notification
                    final Communication communicationToAdd = (Communication) notificationdata;
                    // IMPORTANT NOTE : Usually the Thread that executes this code
                    // is NOT an SWT Thread.
                    // Therefore we have to submit a runnable that will be executed
                    // by the SWT Thread.
                    Display.getDefault().asyncExec(new Runnable() {
                        @SuppressWarnings("unchecked")
                        public final void run() {
                            // Create a new connection edit part
                            final ConnectionEditPart connection = createOrFindConnection(communicationToAdd);
                            targetConnections.add(connection);
                            final AOEditPart target = (AOEditPart) connection.getTarget();
                            if (target != null)
                                target.getTargetConnections().remove(connection);
                            connection.setTarget(AOEditPart.this);
                        }
                    });
                    break;
                }
                case ACTIVE_OBJECT_REMOVE_OUTGOING_COMMUNICATION: {
                    // The model of the outgoing communication to remove
                    final Communication communicationToRemove = (Communication) notificationdata;
                    CommunicationEditPart partToRemove = null;
                    // Find the corresponding edit part of the communication
                    for (final Object object : this.getSourceConnections()) {
                        final CommunicationEditPart ep = (CommunicationEditPart) object;
                        if (communicationToRemove == ep.getModel()) {
                            partToRemove = ep;
                            break;
                        }
                    }
                    // If the part of the model is not found throw an exception
                    if (partToRemove == null) {
                        throw new IllegalArgumentException(
                            "Cannot remove a source connection edit part for communication " +
                                communicationToRemove + " of the active object " + getCastedModel().getName());
                    }
                    final CommunicationEditPart finalPartToRemove = partToRemove;
                    // IMPORTANT NOTE : If the Thread that executes this instruction
                    // is an SWT Thread (see Display.getDefault().asyncExec())
                    // we MUST NOT submit a runnable asynchronously or GEF will
                    // throw a NullPointerException
                    Display.getDefault().asyncExec(new Runnable() {
                        public final void run() {
                            removeSourceConnection(finalPartToRemove);
                        }
                    });
                    break;
                }
                case ACTIVE_OBJECT_REMOVE_INCOMING_COMMUNICATION: {
                    // The model of the incoming communication to remove
                    final Communication communicationToRemove = (Communication) notificationdata;
                    CommunicationEditPart partToRemove = null;
                    // Find the corresponding edit part of the communication
                    for (final Object object : this.getTargetConnections()) {
                        final CommunicationEditPart ep = (CommunicationEditPart) object;
                        if (communicationToRemove == ep.getModel()) {
                            partToRemove = ep;
                            break;
                        }
                    }
                    // If the part of the model is not found throw an exception
                    if (partToRemove == null) {
                        throw new IllegalArgumentException(
                            "Cannot remove a target connection edit part for communication " +
                                communicationToRemove + " of the active object " + getCastedModel().getName());
                    }
                    final CommunicationEditPart finalPartToRemove = partToRemove;
                    // IMPORTANT NOTE : If the Thread that executes this instruction
                    // is an SWT Thread (see Display.getDefault().asyncExec())
                    // we MUST NOT submit a runnable asynchronously or GEF will
                    // throw a NullPointerException
                    Display.getDefault().asyncExec(new Runnable() {
                        public final void run() {
                            removeTargetConnection(finalPartToRemove);
                        }
                    });
                    break;
                }
                case ACTIVE_OBJECT_REMOVE_ALL_OUTGOING_COMMUNICATION: {
                    // Simply refresh all source connections

                    // IMPORTANT NOTE : If the Thread that executes this instruction
                    // is an SWT Thread (see Display.getDefault().asyncExec())
                    // we MUST NOT submit a runnable asynchronously or GEF can
                    // throw a NullPointerException
                    Display.getDefault().asyncExec(new Runnable() {
                        public final void run() {
                            refreshSourceConnections();
                        }
                    });
                    break;
                }
                case ACTIVE_OBJECT_REMOVE_ALL_INCOMING_COMMUNICATION: {
                    // Simply refresh all target connections

                    // IMPORTANT NOTE : If the Thread that executes this instruction
                    // is an SWT Thread (see Display.getDefault().asyncExec())
                    // we MUST NOT submit a runnable asynchronously or GEF can
                    // throw a NullPointerException
                    Display.getDefault().asyncExec(new Runnable() {
                        public final void run() {
                            refreshTargetConnections();
                        }
                    });
                    break;
                }
                case ACTIVE_OBJECT_REQUEST_QUEUE_LENGHT_CHANGED: {
                    getCastedFigure().setRequestQueueLength((Integer) notificationdata);
                    break;
                }
                default:
                    super.update(o, arg);
            } // switch
        } // if arg is Notification
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated with the type of model object the EditPart
     * is associated with. So here, it returns a new NodeFigure.
     * 
     * @return a new NodeFigure view associated with the NodeObject model.
     */
    protected IFigure createFigure() {
        AOFigure figure = new AOFigure(getCastedModel().getName() /* getFullName() */);
        AOListener listener = new AOListener(getCastedModel(), figure, getMonitoringView(),
            getCastedParentFigure());
        figure.addMouseListener(listener);
        figure.addMouseMotionListener(listener);
        return figure;
    }

    protected Color getArrowColor() {
        // Avoid creating a new instance of color by returning a default one
        return DEFAULT_ARROW_COLOR; // new Color(Display.getCurrent(), 108, 108,
        // 116);
    }

    @Override
    protected void createEditPolicies() { /* Do nothing */
    }

    /**
     * This method is overridden since this class has no model children 
     */
    @Override
    protected List<?> getModelChildren() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Convert the result of EditPart.getFigure() to AOFigure (the real type of
     * the figure).
     * 
     * @return the casted figure
     */
    @Override
    public AOFigure getCastedFigure() {
        return (AOFigure) super.getFigure();
    }

    private ProActiveNodeEditPart getCastedParentEditPart() {
        return (ProActiveNodeEditPart) super.getParent();
    }

    private NodeFigure getCastedParentFigure() {
        return (NodeFigure) this.getCastedParentEditPart().getFigure();
    }

    //
    // -- GEF NodeEditPart IMPLEMENTATION ----------------------------
    //

    protected ConnectionAnchor getConnectionAnchor() {
        if (anchor == null) {
            anchor = new EllipseAnchor(getFigure());
        }
        return anchor;
    }

    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return this.getConnectionAnchor();
    }

    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return this.getConnectionAnchor();
    }

    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return this.getConnectionAnchor();
    }

    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return this.getConnectionAnchor();
    }

    protected List<Communication> getModelSourceConnections() {
        return getCastedModel().getOutgoingCommunications();
    }

    protected List<Communication> getModelTargetConnections() {
        return getCastedModel().getIncomingCommunications();
    }
}
