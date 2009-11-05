/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.nativeinterface.spmd;

import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.nativeinterface.application.NativeApplicationFactory;


public class NativeSpmdImpl implements NativeSpmd, java.io.Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    private final static Logger NATIVE_IMPL_LOGGER = ProActiveLogger.getLogger(Loggers.NATIVE);
    private List<Node> nodes;

    /**  name of the NativeSpmd object */
    private String name;

    private NativeApplicationFactory factory;

    // empty no-args constructor
    public NativeSpmdImpl() {
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public NativeApplicationFactory getFactory() {
        return factory;
    }

    /**
     * API method for creating a new NativeSPMD object from an existing Virtual Node
     * @throws NodeException
     */
    public NativeSpmdImpl(String name, List<Node> nodes, NativeApplicationFactory factory) {
        NATIVE_IMPL_LOGGER.debug("[NativeSpmd object] creating Native SPMD active object: " + name);
        this.nodes = nodes;
        this.factory = factory;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n Class: ");
        sb.append(this.getClass().getName());
        sb.append("\n Name: ");
        sb.append(this.name);
        sb.append("\n Command: ");
        sb.append("\n Processes number: ");
        return sb.toString();
    }
}
