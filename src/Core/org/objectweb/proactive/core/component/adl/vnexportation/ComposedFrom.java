/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.core.component.adl.vnexportation;

/**
 * An AST node interface to define a set of virtual nodes composing an exported virtual node.
 *
 * @author The ProActive Team
 */
public interface ComposedFrom {

    /**
     *
     * @return an array of composing virtual nodes
     */
    ComposingVirtualNode[] getComposingVirtualNodes();

    /**
     * add a composing virtual node
     * @param cvn the composing virtual node to add
     */
    void addComposingVirtualNode(ComposingVirtualNode cvn);

    /**
     * removes a composing virtual node
     * @param cvn the composing virtual node to remove
     */
    void removeComposingVirtualNode(ComposingVirtualNode cvn);
}
