/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.ic2d.jmxmonitoring.data;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.UniqueID;


/**
 * This class handles the atomical generation of names associated with the name of an active object.
 * <p>
 * For example if the class name of the active object is <code>Worker</code> then the first resulted
 * associated name to the id of the active object will be <code>Worker#1</code>.
 * The counter will be incremented each time the class name <code>Worker</code> appears with another id.
 * 
 * @author vbodnart
 */
public final class NamesFactory {

    private final static NamesFactory instance = new NamesFactory();

    /**
     * A map that contains associations
     */
    private final Map<UniqueID, String> names;
    /**
     * A counter that will be incremented each time a name is associated
     */
    private int counter;

    /**
     * Returns the single instance of this class. 
     * @return the singleton 
     */
    public static NamesFactory getInstance() {
        return NamesFactory.instance;
    }

    private NamesFactory() {
        this.names = new HashMap<UniqueID, String>();
        this.counter = 1;
    }

    /**
     * Associates a name to an unique id.
     * The returned name is 'the_given_name#a_number'.
     * Example: if the name is 'ao', the returned name is like 'ao#1'
     * @param id An unique id.
     * @param name The name to associated to the unique id.
     * @return The active object name associated to this unique id.
     */
    public synchronized String associateName(final UniqueID id, final String name) {
        String recordedName = this.names.get(id);
        if (recordedName == null) {
            recordedName = name.substring(name.lastIndexOf(".") + 1) + "#" + (this.counter++);
            this.names.put(id, recordedName);
            return recordedName;
        }
        return recordedName;
    }

    /**
     * Returns the name associated to the given id.
     * @param id An unique id.
     * @return The name associated to the given id.
     */
    public String getName(final UniqueID id) {
        return names.get(id);
    }
}