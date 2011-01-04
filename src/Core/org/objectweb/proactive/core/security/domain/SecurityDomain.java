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
package org.objectweb.proactive.core.security.domain;

import org.objectweb.proactive.core.security.SecurityEntity;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;


/**
 * @author The ProActive Team
 *
 * A domain is used to enforce a security policy to a set of Runtimes
 *
 */
public interface SecurityDomain extends SecurityEntity {
    //    /**
    //     * @param securityContext
    //     * @return returns the policy matching the corresponding securityContext
    //     *
    //     */
    //    public SecurityContext getPolicy(SecurityContext securityContext);

    /**
     * @return returns the certificate of the entity corresponding to this domain
     * @throws SecurityNotAvailableException
     */
    public byte[] getCertificateEncoded() throws SecurityNotAvailableException;

    /**
     * @return returns the set of wrapping entities
     * @throws SecurityNotAvailableException
     */
    public Entities getEntities() throws SecurityNotAvailableException;

    /**
     * @return Returns the name of the domain.
     */
    public String getName();
}
