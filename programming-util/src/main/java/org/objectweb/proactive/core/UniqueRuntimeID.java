/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core;

import java.rmi.dgc.VMID;


/**
 * <p>
 * UniqueRuntimeID is a unique runtime identifier across all jvm. It is made of a unique VMID combined
 * with the runtime name.
 * </p>
 * @author The ProActive Team
 */

public class UniqueRuntimeID implements java.io.Serializable {
    private java.rmi.dgc.VMID vmID;

    private String vmName;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new UniqueRuntimeID
     */
    public UniqueRuntimeID() {
    }

    public UniqueRuntimeID(String vmName) {
        this.vmID = new java.rmi.dgc.VMID();
        this.vmName = vmName;
    }

    public UniqueRuntimeID(String vmName, VMID vmID) {
        this.vmID = vmID;
        this.vmName = vmName;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //

    /**
     * Returns the VMID of this UniqueID. Note that the VMID of one UniqueID may differ
     * from the local VMID (that one can get using <code>getCurrentVMID()</code> in case
     * this UniqueID is attached to an object that has migrated.
     * @return the VMID part of this UniqueID
     */
    public java.rmi.dgc.VMID getVMID() {
        return vmID;
    }

    /**
     * Returns the vmName part of this UniqueID.
     * @return the vmName part of this UniqueID
     */
    public String getVMName() {
        return vmName;
    }

    /**
     * Returns a string representation of this UniqueID.
     * @return a string representation of this UniqueID
     */
    @Override
    public String toString() {
        return "" + vmName + " " + vmID;
    }

    /**
     * Overrides hashCode. We are using the vmName hashCode to speedup
     * computation.
     * @return the hashcode of this object
     */
    @Override
    public int hashCode() {
        return vmName.hashCode();
    }

    /**
     * Overrides equals to take into account the two part of this UniqueRuntimeID.
     * @return the true if and only if o is an UniqueID equals to this UniqueRuntimeID
     */
    @Override
    public boolean equals(Object o) {
        //System.out.println("Now checking for equality");
        if (o instanceof UniqueRuntimeID) {
            UniqueRuntimeID uri = (UniqueRuntimeID) o;
            return vmName.equals(uri.vmName) && vmID.equals(uri.vmID);
        } else {
            return false;
        }
    }
}
