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
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import java.util.List;
import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;
import org.eclipse.swt.widgets.Display;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ProActiveNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.NodeFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.NodeListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


public final class ProActiveNodeEditPart extends AbstractMonitoringEditPart<ProActiveNodeObject> {
    private NodeFigure castedFigure;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public ProActiveNodeEditPart(ProActiveNodeObject model) {
        super(model);
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
    // TODO: this method was taken out from a dedicated Thread
    // which means that the observable (NodeObject) will be blocked
    // on the notifyObservers method.
    public void update(Observable o, Object arg) {
        if (!(arg instanceof MVCNotification)) {
            refresh();
            return;
        }
        final MVCNotification notif = (MVCNotification) arg;

        switch (notif.getMVCNotification()) {
            case STATE_CHANGED:
                if (notif.getData() == State.NOT_MONITORED) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public final void run() {
                            refreshChildren();
                        }
                    });
                } else {
                    // method VirtualNodesGroup.getColor(virtualNode vn)
                    // returns the color for the virtual node if the virtual node is
                    // selected
                    // or null if it is not.
                    // if the collor is null, setHighlight(null) colors the figure
                    // node to the default color
                    getViewer().getControl().getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            getCastedFigure().setHighlight(
                                    getMonitoringView().getVirtualNodesGroup().getColor(
                                            getCastedModel().getVirtualNode()));
                            refresh();
                        }
                    });
                }
                break;
            // ADDING A CHILD !!
            case ADD_CHILD:
                final ActiveObject modelToAdd = (ActiveObject) notif.getData();
                // Create new edit part
                final EditPart editPart = createChild(modelToAdd);
                if (editPart == null) {
                    return;
                }
                final int modelIndex = getModelChildren().indexOf(modelToAdd);
                Display.getDefault().asyncExec(new Runnable() {
                    public final void run() {
                        addChild(editPart, modelIndex);
                    }
                });
                break;
            // REMOVING A CHILD !!
            case REMOVE_CHILD:
                final ActiveObject modelToRemove = (ActiveObject) notif.getData();
                final List<?> children = getChildren();
                // In order to keep model<->ep synced during the search of the
                // concerned edit part
                // it's preferable to keep the retrieval of the edit part
                Display.getDefault().asyncExec(new Runnable() {
                    public final void run() {
                        // Get the associated edit part (find it from children edit
                        // parts)
                        for (final Object object : children) {
                            final AOEditPart ep = (AOEditPart) object;
                            if (modelToRemove == ep.getModel()) {
                                removeChild(ep);
                                return;
                            }
                        }
                    }
                });
                break;
            // If ADD CHILDREN notification simply refreshing children edit parts !
            case ADD_CHILDREN:
                Display.getDefault().asyncExec(new Runnable() {
                    public final void run() {
                        refreshChildren();
                    }
                });
                break;
            // If REMOVE CHILDREN to avoid unsynced states with the model sync mode
            // is preferable !
            case REMOVE_CHILDREN:
                Display.getDefault().syncExec(new Runnable() {
                    public final void run() {
                        refreshChildren();
                    }
                });
                break;
            default:
                getViewer().getControl().getDisplay().asyncExec(new Runnable() {
                    public final void run() {
                        // Refresh only if this editpart is active
                        // remember edit parts are active after activate() is called
                        // and until deactivate() is called.
                        // if (NodeEditPart.this.isActive())
                        {
                            // System.out.println("Refreshing Node
                            // "+NodeEditPart.this.getCastedModel().getName());
                            refresh();
                        }
                    }
                });
        }
    }

    /**
     * Convert the result of EditPart.getFigure() to NodeFigure (the real type
     * of the figure).
     * 
     * @return the casted figure
     */
    @SuppressWarnings("unchecked")
    @Override
    public NodeFigure getCastedFigure() {
        if (castedFigure == null) {
            castedFigure = (NodeFigure) getFigure();
        }
        return castedFigure;
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
    @Override
    protected IFigure createFigure() {
        final ProActiveNodeObject model = getCastedModel();
        NodeFigure figure = new NodeFigure("Node " + model.getName(), URIBuilder.getProtocol(model.getUrl()) /* getCastedModel().getFullName(),getCastedModel().getParentProtocol() */);

        NodeListener listener = new NodeListener(getCastedModel(), figure, getMonitoringView());
        figure.addMouseListener(listener);
        figure.addMouseMotionListener(listener);
        return figure;
    }

    /**
     * Creates the initial EditPolicies and/or reserves slots for dynamic ones.
     */
    @Override
    protected void createEditPolicies() { /* Do nothing */
    }
}
