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
package org.objectweb.proactive.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * <p>
 * UniqueID is a unique object identifier across all jvm. It is made of a unique VMID combined
 * with a unique UID on that VM.
 * </p><p>
 * The UniqueID is used to identify object globally, even in case of migration.
 * </p>
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */

@PublicAPI
public class UniqueID implements java.io.Serializable, Comparable<UniqueID> {
    private java.rmi.server.UID id;
    private java.rmi.dgc.VMID vmID;
    private String identifier;

    //the Unique ID of the JVM
    private static java.rmi.dgc.VMID uniqueVMID = new java.rmi.dgc.VMID();
    final protected static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);

    // Optim
    private transient String cachedShortString;
    private transient String cachedCanonString;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    public UniqueID(String identifier) {
        this();
        this.identifier = identifier;
    }

    /**
     * Creates a new UniqueID
     */
    public UniqueID() {
        this.id = new java.rmi.server.UID();
        this.vmID = uniqueVMID;
    }

    //
    // -- PUBLIC STATIC METHODS -----------------------------------------------
    //

    /**
     * Returns the VMID of the current VM in which this class has been loaded.
     * @return the VMID of the current VM
     */
    public static java.rmi.dgc.VMID getCurrentVMID() {
        return uniqueVMID;
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
        return this.vmID;
    }

    /**
     * Returns the UID part of this UniqueID.
     * @return the UID part of this UniqueID
     */
    public java.rmi.server.UID getUID() {
        return this.id;
    }

    /**
     * Returns a string representation of this UniqueID.
     * @return a string representation of this UniqueID
     */
    @Override
    public String toString() {
        return getCanonString();
    }

    public String shortString() {
        // Date-race initialization. Initialization in the ctor is to heavy
        String s = this.cachedShortString;
        if (s == null) {
            s = (identifier != null ? identifier : "") + Math.abs(this.getCanonString().hashCode() % 100000);
            this.cachedShortString = s;
        }

        return s;
    }

    public String getCanonString() {
        // Date-race initialization. Initialization in the ctor is to heavy
        String s = this.cachedCanonString;
        if (s == null) {
            s = ((identifier != null ? identifier : "") + this.id + "--" + this.vmID).replace(':', '-');
            this.cachedCanonString = s;
        }

        return s;
    }

    public int compareTo(UniqueID u) {
        return getCanonString().compareTo(u.getCanonString());
    }

    /**
     * Overrides hashCode to take into account the two part of this UniqueID.
     * @return the hashcode of this object
     */
    @Override
    public int hashCode() {
        return this.id.hashCode() + this.vmID.hashCode();
    }

    /**
     * Overrides equals to take into account the two part of this UniqueID.
     * @return the true if and only if o is an UniqueID equals to this UniqueID
     */
    @Override
    public boolean equals(Object o) {
        //System.out.println("Now checking for equality");
        if (o instanceof UniqueID) {
            return ((this.id.equals(((UniqueID) o).getUID())) && (this.vmID.equals(((UniqueID) o).getVMID())));
        } else {
            return false;
        }
    }

    /**
     * for debug purpose
     */
    public void echo() {
        logger.info("UniqueID The Id is " + this.id + " and the address is " + this.vmID);
    }
}
