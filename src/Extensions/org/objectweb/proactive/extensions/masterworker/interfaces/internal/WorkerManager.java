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
package org.objectweb.proactive.extensions.masterworker.interfaces.internal;

import java.net.URL;
import java.util.Collection;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.xml.VariableContract;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * Admin interface of the Worker Manager in the Master/Worker API (allows to extend the pool of workers, or terminate the manager)<br/>
 * @author The ProActive Team
 *
 */
public interface WorkerManager extends WorkerDeadListener {

    /**
     * Asks the worker manager to activate every virtual nodes inside the given descriptor
     * and use the generated nodes as resources
     * @param descriptorURL URL of a deployment descriptor
     */
    void addResources(URL descriptorURL) throws ProActiveException;

    /**
     * Asks the worker manager to activate the given virtual nodes inside the given descriptor
     * and use the generated nodes as resources
     * @param descriptorURL URL of a deployment descriptor
     * @param virtualNodeName names of the virtual node to activate
     */
    void addResources(URL descriptorURL, String virtualNodeName) throws ProActiveException;

    /**
     * Adds the given descriptor to the master<br>
     * Only the specified virtual node inside the given descriptor will be activated <br/>
     * @param descriptorURL URL of a deployment descriptor
     * @param contract a variable contract for this descriptor
     * @param virtualNodeName name of the virtual node to activate
     * @throws ProActiveException if a problem occurs while adding resources
     */
    void addResources(URL descriptorURL, VariableContract contract, String virtualNodeName)
            throws ProActiveException;

    /**
    * Adds the given descriptor to the master<br>
    * Every virtual nodes inside the given descriptor will be activated<br/>
    * @param descriptorURL URL of a deployment descriptor
    * @param contract a variable contract for this descriptor
    * @throws ProActiveException if a problem occurs while adding resources
    */
    void addResources(URL descriptorURL, VariableContract contract) throws ProActiveException;

    /**
     * Connects this master to a ProActive Scheduler<br>
     * <br/>
     * @param schedulerURL URL of a scheduler
     * @param login scheduler username
     * @param password scheduler password
     * @param classpath an array of directory or jars that will be used by the scheduler to find user classes (i.e. tasks definitions)
     * @throws ProActiveException if a problem occurs while adding resources
     */
    void addResources(String schedulerURL, String login, String password, String[] classpath)
            throws ProActiveException;

    /**
     * Adds the given Collection of nodes to the worker manager
     * @param nodes a collection of nodes
     */
    void addResources(Collection<Node> nodes);

    /**
     * Terminates the worker manager and free every resources (if asked)
     * @param freeResources tells if the Worker Manager should as well free the node resources
     * @return success
     */
    BooleanWrapper terminate(boolean freeResources);

}
