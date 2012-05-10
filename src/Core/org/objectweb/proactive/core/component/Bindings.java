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
package org.objectweb.proactive.core.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.fractal.api.type.InterfaceType;


/**
 * A bindings container. This class stores the bindings for a given component (it also takes in charge collective
 * bindings, ie 1 client to several servers).
 *
 * @author The ProActive Team
 */
public class Bindings implements Serializable {
    //	 In case of collective bindings, the interfaces of the collection can be : 
    //	  1. named (as in Fractal 2.0 spec) : they are mapped in clientInterfaceBindings according to their name
    //	  2. anonymous : they are put in a list which is mapped in clientInterfaceBindings with the type name of the collective interface 
    protected Map<String, Object> normalBindings; //values are Binding or List of Binding
    protected Map<String, Object> exportBindings;

    // key = interfaceName ; value = binding
    // if collective binding : key = interfaceName ; value = Vector (Binding objects)
    public Bindings() {
        normalBindings = new HashMap<String, Object>();
        exportBindings = new HashMap<String, Object>();
    }

    /**
     * @param binding the binding to add
     */
    public void add(Binding binding) {

        InterfaceType client_itf_type = (InterfaceType) binding.getClientInterface().getFcItfType();

        // export bindings
        if (!client_itf_type.isFcClientItf()) {
            exportBindings.put(binding.getClientInterfaceName(), binding);
        } else {
            // normal bindings
            if (client_itf_type.isFcCollectionItf()) {
                addCollectiveBindingOnExternalClientItf(binding);
            } else {
                normalBindings.put(binding.getClientInterfaceName(), binding);
            }
        }

    }

    // returns either a Binding or a List of Binding objects (collection interface case)

    /**
     * removes the binding on the given client interface
     * @param clientItfName String name of the binding
     * @return Object binding that was removed
     */
    public Object remove(String clientItfName) {
        if (normalBindings.containsKey(clientItfName)) {
            return normalBindings.remove(clientItfName);
        }
        if (exportBindings.containsKey(clientItfName)) {
            return exportBindings.remove(clientItfName);
        }
        return null;
    }

    /**
     * Method get.
     * @param clientItfName String
     * @return Object
     */
    public Object get(String clientItfName) {
        if (normalBindings.containsKey(clientItfName)) {
            return normalBindings.get(clientItfName);
        }
        if (exportBindings.containsKey(clientItfName)) {
            return exportBindings.get(clientItfName);
        }
        return null;
    }

    /**
     * tests if binding exists on the given interface
     * @param clientItfName the client inteface to check
     * @return true if binding exists
     */
    public boolean containsBindingOn(String clientItfName) {
        return (normalBindings.containsKey(clientItfName) || exportBindings.containsKey(clientItfName));
    }

    /**
     * Returns the names of the external client bindings for this component.
     * In case of a collective interface, the names of each of its constituting interfaces are not returned ;
     * only the name of the collective interface is returned.
     */
    public String[] getExternalClientBindings() {
        return normalBindings.keySet().toArray(new String[normalBindings.keySet().size()]);
    }

    /**
     * @param bindingsTable Map map that stores the bindings
     * @param binding Binding the binding to add
     */
    @SuppressWarnings("unchecked")
    private static void addCollectiveBinding(Map<String, Object> bindingsTable, Binding binding) {
        String client_itf_name = binding.getClientInterfaceName();
        if (binding.getClientInterface().getFcItfName().equals(client_itf_name)) {
            if (bindingsTable.containsKey(client_itf_name)) {
                // there should be a List for containing the bindings associated
                ((List<Binding>) bindingsTable.get(client_itf_name)).add(binding);
            } else { // we create a List for keeping the bindings
                ArrayList<Binding> bindings_collection = new ArrayList<Binding>();
                bindings_collection.add(binding);
                bindingsTable.put(client_itf_name, bindings_collection);
            }
        } else {
            bindingsTable.put(client_itf_name, binding);
        }
    }

    /**
     *
     * @param binding Binding the binding to add
     */
    private void addCollectiveBindingOnExternalClientItf(Binding binding) {
        addCollectiveBinding(normalBindings, binding);
    }
}
