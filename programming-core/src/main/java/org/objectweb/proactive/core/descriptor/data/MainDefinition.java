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
package org.objectweb.proactive.core.descriptor.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 * A <code>MainDefinition</code> is an internal representation of XML mainDefinition element.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2005/09/20
 * @since   ProActive 3.0
 * @see VirtualNodeInternal
 * @see VirtualMachine
 */
public class MainDefinition implements Serializable {

    private static final long serialVersionUID = 60L;
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //

    /** all virtualNodes are put in a List of VirtualNode */
    private List<VirtualNodeInternal> virtualNodeList;

    /** fully qualified name of the main Class */
    private String mainClass;

    /** List containing all parameters of the main method, as String */
    private List<String> parameters;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //
    public MainDefinition(String mainClass, List<String> parameters, List<VirtualNodeInternal> virtualNodeList) {
        this.virtualNodeList = virtualNodeList;
        this.mainClass = mainClass;
        this.parameters = parameters;
    }

    public MainDefinition(String mainClass) {
        this(mainClass, new ArrayList<String>(), new ArrayList<VirtualNodeInternal>());
    }

    public MainDefinition() {
        this(null);
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    //

    /**
     * activates all nodes of the list virtualNodeList
     */
    public void activateMain() {
        // activate all nodes
        for (int i = 0; i < virtualNodeList.size(); i++) {
            getVirtualNode(i).activate();
        }
    }

    /**
     * set the list of virtual nodes
     * @param virtualNodeList new list
     */
    public void setVirtualNodeList(List<VirtualNodeInternal> virtualNodeList) {
        this.virtualNodeList = virtualNodeList;
    }

    /**
     * set the main class attribute
     * @param mainClass fully qualified name of the class containing a main method
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * set the list of parameters
     * @param parameters list of String
     */
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    /**
     * return the list of virtual nodes
     * @return list of virtual nodes
     */
    public List<VirtualNodeInternal> getVirtualNodeList() {
        return virtualNodeList;
    }

    /**
     * return a table of virtual nodes
     * @return a table of virtual nodes
     */
    public VirtualNodeInternal[] getVirtualNodes() {
        VirtualNodeInternal[] result = new VirtualNodeInternal[virtualNodeList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = virtualNodeList.get(i);
        }
        return result;
    }

    /**
     * add a virtual node to the list of virtal nodes
     * @param virtualNode virtual node to add
     */
    public void addVirtualNode(VirtualNodeInternal virtualNode) {
        virtualNodeList.add(virtualNode);
    }

    /**
     * return the i-th virtual node of the list
     * @param i index of the virtual node to get
     * @return the i-th virtual node of the list
     */
    public VirtualNodeInternal getVirtualNode(int i) {
        return virtualNodeList.get(i);
    }

    /**
     * return the fully qualified name of the class containing the main method
     * @return fully qualified name of the class containing the main method
     */
    public String getMainClass() {
        return mainClass;
    }

    /**
     * return a table of String containing all the parameters to apply to the main method
     * @return a table of String containing all the parameters to apply to the main method
     */
    public String[] getParameters() {
        String[] result = new String[parameters.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = parameters.get(i);
        }
        return result;
    }

    /**
     * add a parameter to the list of parameters, at the last position
     * @param parameter parameter to add to the list of parameters
     */
    public void addParameter(String parameter) {
        parameters.add(parameter);
    }
}
