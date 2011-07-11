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
package org.objectweb.proactive.core.security;

import java.io.Serializable;

import org.objectweb.proactive.core.security.exceptions.IncompatiblePolicyException;


/**
 * This class represents security attributes granted to a targeted communication
 *
 */
public class Communication implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    /**
     *
     */
    private final Authorization authentication;
    private final Authorization confidentiality;
    private final Authorization integrity;

    // public static final int REQUIRED = 1;
    // public static final int DENIED = -1;
    // public static final int OPTIONAL = 0;

    // public static final String STRING_REQUIRED = "required";
    // public static final String STRING_OPTIONAL = "optional";
    // public static final String STRING_DENIED = "denied";

    // /* indicates if authentication is required,optional or denied */
    // private int authentication;
    //
    // /* indicates if confidentiality is required,optional or denied */
    // private int confidentiality;
    //
    // /* indicates if integrity is required,optional or denied */
    // private int integrity;

    /* indicates if communication between active objects is allowed or not */
    private final boolean communication;

    /**
     * Default constructor, initialize a policy with communication attribute
     * sets to allowed and authentication,confidentiality and integrity set to
     * optional
     */
    public Communication() {
        this.authentication = Authorization.REQUIRED;
        this.confidentiality = Authorization.REQUIRED;
        this.integrity = Authorization.REQUIRED;
        this.communication = false;
    }

    /**
     * Copy constructor
     */
    public Communication(Communication com) {
        this.authentication = com.getAuthentication();
        this.confidentiality = com.getConfidentiality();
        this.integrity = com.getIntegrity();
        this.communication = com.getCommunication();
    }

    /**
     * This method specifies if communication is allowed
     *
     * @param authentication
     *            specifies if authentication is required, optional, or denied
     * @param confidentiality
     *            specifies if confidentiality is required, optional, or denied
     * @param integrity
     *            specifies if integrity is required, optional, or denied
     */
    public Communication(boolean allowed, Authorization authentication, Authorization confidentiality,
            Authorization integrity) {
        this.communication = allowed;
        this.authentication = authentication;
        this.confidentiality = confidentiality;
        this.integrity = integrity;
    }

    /**
     * Method isAuthenticationEnabled.
     *
     * @return boolean true if authentication is required
     */
    public boolean isAuthenticationEnabled() {
        return this.authentication == Authorization.REQUIRED;
    }

    /**
     * Method isConfidentialityEnabled.
     *
     * @return boolean true if confidentiality is required
     */
    public boolean isConfidentialityEnabled() {
        return this.confidentiality == Authorization.REQUIRED;
    }

    /**
     * Method isIntegrityEnabled.
     *
     * @return boolean true if integrity is required
     */
    public boolean isIntegrityEnabled() {
        return this.integrity == Authorization.REQUIRED;
    }

    // /**
    // * Method isAuthenticationForbidden.
    // * @return boolean true if confidentiality is forbidden
    // */
    // public boolean isAuthenticationForbidden() {
    // return this.authentication == Authorization.DENIED;
    // }
    //
    // /**
    // * Method isConfidentialityForbidden.
    // * @return boolean true if confidentiality is forbidden
    // */
    // public boolean isConfidentialityForbidden() {
    // return this.confidentiality == Authorization.DENIED;
    // }
    //
    // /**
    // * Method isIntegrityForbidden.
    // * @return boolean true if integrity is forbidden
    // */
    // public boolean isIntegrityForbidden() {
    // return this.integrity == Authorization.DENIED;
    // }

    /**
     * Method isCommunicationAllowed.
     *
     * @return boolean true if confidentiality is allowed
     */
    public boolean isCommunicationAllowed() {
        return this.communication;
    }

    @Override
    public String toString() {
        return "Com : " + (this.communication ? 1 : 0) + this.authentication.getValue() +
            this.confidentiality.getValue() + this.integrity.getValue();
    }

    /**
     * Method computePolicy.
     *
     * @param from
     *            the client policy
     * @param to
     *            the server policy
     * @return Policy returns a computation of the from and server policies
     * @throws IncompatiblePolicyException
     *             policies are incomptables, conflicting communication
     *             attributes
     */
    public static Communication computeCommunication(Communication from, Communication to)
            throws IncompatiblePolicyException {
        return new Communication(from.communication && to.communication, Authorization.compute(
                from.authentication, to.authentication), Authorization.compute(from.confidentiality,
                to.confidentiality), Authorization.compute(from.integrity, to.integrity));
    }

    /**
     * @return communication
     */
    public boolean getCommunication() {
        return this.communication;
    }

    // /**
    // * @param i
    // */
    // public void setCommunication(boolean i) {
    // this.communication = i;
    // }
    public Authorization getAuthentication() {
        return this.authentication;
    }

    public Authorization getConfidentiality() {
        return this.confidentiality;
    }

    public Authorization getIntegrity() {
        return this.integrity;
    }
}
