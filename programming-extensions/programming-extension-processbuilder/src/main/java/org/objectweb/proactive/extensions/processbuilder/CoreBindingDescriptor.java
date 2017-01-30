/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.extensions.processbuilder;

import java.util.List;


/**
 * This interface represents an abstract set of processor cores, which can be
 * enabled or disabled for process-to-core binding.
 * <p>
 * Default state for binding descriptors is to enable binding to all cores.
 * </p>
 * 
 * @author The ProActive Team
 * @since ProActive 5.0.0
 */
public interface CoreBindingDescriptor {

    /**
     * Returns the number of cores that can be used for binding.
     * 
     * @return
     */
    public int getCoreCount();

    /**
     * Enables binding to this core.
     * 
     * @throws IndexOutOfBoundsException
     * @param coreIndex
     */
    public void setBound(int coreIndex);

    /**
     * Enables binding to the subset of cores given by start index and count.
     * 
     * @throws IndexOutOfBoundsException
     * @param fromCore
     * @param count
     */
    public void setBound(int fromCore, int count);

    /**
     * Disables binding to this core.
     * 
     * @throws IndexOutOfBoundsException
     * @param coreIndex
     */
    public void setNotBound(int coreIndex);

    /**
     * Disables binding to this subset of cores (given by start index and
     * count).
     * 
     * @throws IndexOutOfBoundsException
     * @param fromCore
     * @param count
     */
    public void setNotBound(int fromCore, int count);

    /**
     * Returns true if the core is enabled for binding and false if not.
     * 
     * @throws IndexOutOfBoundsException
     * @param coreIndex
     * @return
     */
    public boolean isBound(int coreIndex);

    /**
     * Returns true if <b>all</b> cores in the subset (defined by start index
     * and count) are enabled for binding.
     * 
     * @throws IndexOutOfBoundsException
     * @param fromCore
     * @param count
     * @return
     */
    public boolean areAllBound(int fromCore, int count);

    /**
     * Returns a list representation of the bound cores.
     * 
     * @return
     */
    public List<Integer> listBoundCores();

}
