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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.ic2d.chartit.data.store;

import java.util.List;

import org.objectweb.proactive.ic2d.chartit.data.ChartModel;


/**
 * A common interface for all data store implementation
 * @author vbodnart
 *
 */
public interface IDataStore {

    /**
     * This method must be called once all elements were added.
     *
     * @return <code>true</code> if the initialization was performed; <code>false</code> otherwise
     */
    public boolean init(final List<ChartModel> modelsToStore, final int stepInSeconds);

    /**
     * Stores all values, this method must be called after all values for each
     * element were added.
     */
    public void update();

    /**
     * Closes this data store.
     */
    public void close();

    /**
     * To know if this data store is closed.
     * 
     * @return True if this data store is closed False otherwise
     */
    public boolean isClosed();

    /**
     * Dumps this data store to the standard output.
     */
    public void dump();

    /**
     * Returns the left bound time in seconds.
     * 
     * @return the left bound time
     */
    public long getLeftBoundTime();

    /**
     * Returns the right bound time in seconds.
     * 
     * @return the right bound time
     */
    public long getRightBoundTime();

    /**
     * Returns the name of the data store.
     * 
     * @return the dataStoreName
     */
    public String getDataStoreName();
}
