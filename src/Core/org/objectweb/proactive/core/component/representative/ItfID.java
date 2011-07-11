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
package org.objectweb.proactive.core.component.representative;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


/**
 * Identifies the functional interface of a component by its name and the id of the body of the component
 * it belongs to.
 *
 * @author The ProActive Team
 */
public class ItfID implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    private String itfName;
    private UniqueID componentBodyID;
    boolean isClientItf = false;

    public ItfID(String itfName, UniqueID componentBodyID) {
        this.itfName = itfName;
        this.componentBodyID = componentBodyID;
    }

    public String getItfName() {
        return itfName;
    }

    public UniqueID getComponentBodyID() {
        return componentBodyID;
    }

    @Override
    public int hashCode() {
        return componentBodyID.hashCode() + itfName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        //System.out.println("Now checking for equality");
        if (o instanceof ItfID) {
            return (itfName.equals(((ItfID) o).itfName) && (componentBodyID
                    .equals(((ItfID) o).componentBodyID)));
        } else {
            return false;
        }
    }
}
