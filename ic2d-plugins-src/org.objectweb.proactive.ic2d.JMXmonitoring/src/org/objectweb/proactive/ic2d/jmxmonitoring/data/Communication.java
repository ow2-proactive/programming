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
 *  Initial developer(s):  ActiveEon Team - http://www.activeeon.com
 *  Contributor(s): 
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.Observable;


/**
 * A model that represents a communication between two active objects.
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public final class Communication extends Observable {

    /**
     * The maximal number of calls
     */
    public static final int MAX_CALLS = 100;

    /**
     * The source of the communication
     */
    private final ActiveObject source;

    /**
     * The target of the communication
     */
    private final ActiveObject target;

    /**
     * The number of calls
     */
    private int nbCalls = 1;

    /**
     * Creates a new communication between the source and the target
     * @param source The source of the communication
     * @param target The target of the communication
     */
    public Communication(final ActiveObject source, final ActiveObject target) {
        if (source == null || target == null || source == target) {
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.target = target;
        this.connect();
    }

    public void addOneCall() {
        if (nbCalls < MAX_CALLS)
            nbCalls++;

        // this is usefull when showing connection in proportional and ratio
        // mode
        // need to take a look at this modes
        // need to take a look at the autoreset in order to make these features
        // usefull
        //
        // Uncoment next code in order to send the notification to the edit part
        // and update the view with rtespect to the number of connections

        // setChanged();
        // //if (CommunicationEditPart.drawingStyle!=DrawingStyle.FIXED)
        // {
        // notifyObservers(new
        // MVCNotification(MVCNotificationTag.ACTIVE_OBJECT_ADD_COMMUNICATION,
        // nbCalls));
        // }
    }

    /**
     * Connects this communication to the source and target active objects.
     */
    public void connect() {
        this.source.internalAddOutgoingCommunication(this);
        this.target.internalAddIncomingCommunication(this);
    }

    /**
     * Disconnects this communication from the source and target active objects.
     */
    public void disconnect() {
        this.source.internalRemoveOutgoingCommunication(this);
        this.target.internalRemoveIncomingCommunication(this);
    }

    /**
     * Returns the number of calls that was performed through this communication.
     * 
     * @return the number of calls that was performed through this communication
     */
    public int getNbCalls() {
        return this.nbCalls;
    }

    /**
     * Returns the source active object of this communication.
     * 
     * @return the source active object of this communication.
     */
    public ActiveObject getSource() {
        return this.source;
    }

    /**
     * Returns the target active object of this communication.
     * 
     * @return the target active object of this communication.
     */
    public ActiveObject getTarget() {
        return this.target;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[ " + this.source.getName() + " -> " + this.target.getName() + " ]";
    }
}