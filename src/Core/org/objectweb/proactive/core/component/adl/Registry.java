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
package org.objectweb.proactive.core.component.adl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class is a static registry used for storing component instances according to their name.
 *
 * @author The ProActive Team
 */
public class Registry {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
    static private Registry instance = null;
    private Map<String, Component> table;

    private Registry() {
        table = new Hashtable<String, Component>();
    }

    /**
     * Returns a single instance
     * @return the unique instance in the vm
     */
    static public Registry instance() {
        if (instance == null) {
            instance = new Registry();
        }
        return instance;
    }

    /**
     * see @link org.objectweb.fractal.adl.RegistryManager#addComponent(org.objectweb.fractal.api.Component)
     */
    public void addComponent(Component component) throws ADLException {
        if (PAGroup.isGroup(component)) {
            Group<Component> group = PAGroup.getGroup(component);
            Iterator<Component> it = group.iterator();
            while (it.hasNext()) {
                addComponent(it.next());
            }
        } else {
            try {
                String name = GCM.getNameController(component).getFcName();
                if (table.containsKey(name)) {
                    throw new ADLException(RegistryErrors.DUPLICATED_COMPONENT_NAME, name);
                }
                table.put(name, component);
                if (logger.isDebugEnabled()) {
                    logger.debug("added component " + name + " to the local registry");
                }
            } catch (NoSuchInterfaceException e) {
                throw new ADLException(RegistryErrors.NAME_CONTROLLER_MISSING);
            }
        }
    }

    /**
     * see @link org.objectweb.fractal.adl.RegistryManager#getComponent(java.lang.String)
     */
    public Component getComponent(String name) {
        return table.get(name);
    }

    /**
     * empties the registry
     *
     */
    public void clear() {
        table.clear();
    }
}
