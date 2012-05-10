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
package org.objectweb.proactive.core.component.adl.vnexportation;

import org.objectweb.fractal.adl.ADLException;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;


/**
 * An implementation of the {@link org.objectweb.proactive.core.component.adl.vnexportation.ExportedVirtualNodesBuilder} interface.
 * This class performs a logical composition of the exported virtual nodes of the components ADL.
 *
 * @author The ProActive Team
 *
 */
public class ExportedVirtualNodesBuilderImpl implements ExportedVirtualNodesBuilder {
    // implementation of ExportedVirtualNodesBuilder
    public void compose(String componentName, ExportedVirtualNode[] exportedVirtualNodes,
            VirtualNode currentComponentVN) throws ADLException {
        for (int i = 0; i < exportedVirtualNodes.length; i++) {
            ComposingVirtualNode[] composing_vns = exportedVirtualNodes[i].getComposedFrom()
                    .getComposingVirtualNodes();

            for (int j = 0; j < composing_vns.length; j++) {
                boolean composing_vn_is_multiple = false;
                if ("this".equals(composing_vns[j].getComponent())) {
                    if (currentComponentVN == null) {
                        throw new ADLException(ExportedVirtualNodeErrors.VIRTUAL_NODE_NOT_FOUND,
                            composing_vns[j].getName(), componentName);
                    }
                    if (!currentComponentVN.getName().equals(composing_vns[j].getName())) {
                        throw new ADLException(ExportedVirtualNodeErrors.VIRTUAL_NODE_NOT_FOUND,
                            composing_vns[i].getName(), componentName, currentComponentVN.getName());
                    }

                    // change "this" into the name of the component
                    composing_vns[j].setComponent(componentName);
                }
                if ((currentComponentVN != null) &&
                    currentComponentVN.getCardinality().equals(VirtualNode.MULTIPLE)) {
                    composing_vn_is_multiple = true;
                }

                //                /System.out.println("COMPOSING : " + componentName + "." + exportedVirtualNodes[i].getName() + "--> " + composing_vns[j].getComponent() + "." + composing_vns[j].getName());
                ExportedVirtualNodesList.instance().compose(componentName, exportedVirtualNodes[i],
                        composing_vns[j], composing_vn_is_multiple);
                //System.out.println("COMPOSED VN LIST : " +ExportedVirtualNodesList.instance().toString());
            }
        }
    }
}
