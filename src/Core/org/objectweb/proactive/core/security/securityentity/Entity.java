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
package org.objectweb.proactive.core.security.securityentity;

import java.io.Serializable;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.TypedCertificateList;


public class Entity implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    private final TypedCertificateList certChain;

    public Entity(TypedCertificateList certChain) {
        this.certChain = certChain;
    }

    public EntityType getType() {
        return this.certChain.get(0).getType();
    }

    public String getName() {
        return this.certChain.get(0).getCert().getSubjectX500Principal().getName();
    }

    public TypedCertificateList getCertificateChain() {
        return this.certChain;
    }

    public TypedCertificate getCertificate() {
        if (this.certChain == null) {
            return null;
        }
        return this.certChain.get(0);
    }

    @Override
    public String toString() {
        return getCertificate().toString();
    }
}
