/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
//@snippet-start TechnicalService_Interface
package org.objectweb.proactive.core.descriptor.services;

import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.core.node.Node;


/**
 * <p>
 * Interface to implement for defining a Technical Service.
 * </p>
 * <b>Definition of Technical Service:</b>
 * <p>
 * A Technical Service is a non-functional requirement that may be dynamically fulfilled at runtime
 * by updating the configuration of selected resources (here a ProActive Node).
 * </p>
 * 
 * @author The ProActive Team
 * 
 */
public interface TechnicalService extends Serializable {

    /**
     * Initialize the Technical Service with its argument values.
     * 
     * @param argValues
     *            values of the Technical Service arguments.
     */
    public abstract void init(Map<String, String> argValues);

    /**
     * Initialize the given node with the Technical Service.
     * 
     * @param node
     *            the node where to apply the Technical Service.
     */
    public abstract void apply(Node node);
}
//@snippet-end TechnicalService_Interface