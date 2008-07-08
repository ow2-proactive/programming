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

import java.util.Observable;

import org.eclipse.draw2d.IFigure;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.jmxmonitoring.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.VMFigure;
import org.objectweb.proactive.ic2d.jmxmonitoring.figure.listener.JVMListener;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotification;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.MVCNotificationTag;
import org.objectweb.proactive.ic2d.jmxmonitoring.util.State;


public final class RuntimeEditPart extends AbstractMonitoringEditPart<RuntimeObject> {
    private VMFigure castedFigure;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public RuntimeEditPart(RuntimeObject model) {
        super(model);
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    @Override
    //TODO: this method was taken out from a dedicated Thread 
    //which means that the observable (VMObject) will be blocked
    //on the notifyObservers method.
    public void update(Observable o, Object arg) {
        if (!(arg instanceof MVCNotification)) {
            return;
        }

        final MVCNotification notif = ((MVCNotification) arg);
        final MVCNotificationTag mvcNotification = notif.getMVCNotification();
        getViewer().getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                switch (mvcNotification) {
                    case RUNTIME_OBJECT_RUNTIME_KILLED:
                        Console.getInstance(Activator.CONSOLE_NAME).log(getModel() + " killed!");
                        getCastedFigure().notResponding();
                        break;
                    case STATE_CHANGED: {
                        if (notif.getData() == State.NOT_MONITORED) {
                            refreshChildren();
                            return;
                        }
                    }
                    default:
                        // Refresh only if this editpart is active
                        // remember edit parts are active after activate() is called
                        // and until deactivate() is called.
                        if (isActive()) {
                            refresh();
                        }
                }
            }
        });

        /*
        if(param instanceof State && (State)param == State.NOT_RESPONDING)
                ((VMFigure)getFigure()).notResponding();
        else if(param instanceof State && (State)param == State.NOT_MONITORED) {
                deactivate();
        }*/

        //refresh();
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //

    /**
     * Returns a new view associated
     * with the type of model object the
     * EditPart is associated with. So here, it returns a new VMFigure.
     * @return a new VMFigure view associated with the VMObject model.
     */
    protected IFigure createFigure() {
        VMFigure figure = new VMFigure(getCastedModel().getName() /*FullName()*/);
        JVMListener listener = new JVMListener(getCastedModel(), getMonitoringView());
        figure.addMouseListener(listener);
        figure.addMouseMotionListener(listener);
        return figure;
    }

    /**
     * Creates the initial EditPolicies and/or reserves slots for dynamic ones.
     */
    protected void createEditPolicies() { /* Do nothing */
    }

    /**
     * Convert the result of EditPart.getFigure()
     * to VMFigure (the real type of the figure).
     * @return the casted figure
     */
    @SuppressWarnings("unchecked")
    @Override
    public VMFigure getCastedFigure() {
        if (castedFigure == null) {
            castedFigure = (VMFigure) getFigure();
        }
        return castedFigure;
    }
}
