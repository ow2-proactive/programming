/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.masterworker.core;

import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the worker memory
 *
 * @author The ProActive Team
 */
public class WorkerMemoryImpl implements WorkerMemory {
    /**
     * The memory of the worker <br>
     * the worker can keep some data between different tasks executions <br>
     * e.g. connection to a database, file descriptor, etc ...
     */
    private Map<String, Object> memory;

    public WorkerMemoryImpl(Map<String, Serializable> memory) {
        this.memory = new HashMap<String, Object>(memory);
    }

    /**
     * {@inheritDoc}
     */
    public void save(final String dataName, final Object data) {
        memory.put(dataName, data);
    }

    /**
     * {@inheritDoc}
     */
    public Object load(final String dataName) {
        return memory.get(dataName);
    }

    /**
     * {@inheritDoc}
     */
    public void erase(final String dataName) {
        memory.remove(dataName);
    }

    public void clear() {
        memory.clear();
    }

}
