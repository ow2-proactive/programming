/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.timitspmd.util.observing;

import java.util.Vector;

import org.objectweb.proactive.extensions.timitspmd.util.EventStatistics;


/**
 * This class represents several StatDatas provided by observers of a subject.
 *
 * @author The ProActive Team
 *
 */
public class EventDataBag implements java.io.Serializable {

    /**
     *
     */

    /** The rank that identifies the subject ie the worker in a group */
    private int subjectRank;

    /** The vector of StatDatas */
    private Vector<EventData> bag;

    public EventDataBag() {
    }

    /** Creates a new instance of StatDataBag */
    public EventDataBag(int rank) {
        this.subjectRank = rank;
        this.bag = null;
    }

    public int getSubjectRank() {
        return this.subjectRank;
    }

    public void setBag(Vector<EventData> bag) {
        this.bag = bag;
    }

    public Vector<EventData> getBag() {
        return this.bag;
    }

    public EventData getEventData(int index) {
        return this.bag.get(index);
    }

    public int size() {
        return this.bag.size();
    }

    public EventStatistics getStats() {
        String[] counterName = new String[this.size()];
        Object[] value = new Object[this.size()];

        for (int i = 0; i < this.size(); i++) {
            counterName[i] = this.getEventData(i).getName();
            value[i] = this.getEventData(i);
        }

        return new EventStatistics(counterName, value, this.size(), this);
    }

    @Override
    public String toString() {
        String res = "";
        for (int i = 0; i < this.bag.size(); i++) {
            res += (this.bag.get(i).toString() + "\n");
        }

        return res;
    }
}
