/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
