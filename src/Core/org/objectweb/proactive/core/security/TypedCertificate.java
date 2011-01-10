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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.objectweb.proactive.core.security.SecurityConstants.EntityType;


public class TypedCertificate implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;
    /**
     *
     */
    private transient X509Certificate cert;
    private final EntityType type;
    private final PrivateKey privateKey;
    private byte[] encodedCert;

    public TypedCertificate(X509Certificate cert, EntityType type, PrivateKey privateKey) {
        this.cert = cert;
        this.type = type;
        this.privateKey = privateKey;
        this.encodedCert = null;
    }

    public X509Certificate getCert() {
        return this.cert;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public EntityType getType() {
        return this.type;
    }

    //	public void setType(EntityType type) {
    //		this.type = type;
    //	}
    public TypedCertificate noPrivateKey() {
        return new TypedCertificate(this.cert, this.type, null);
    }

    @Override
    public String toString() {
        return getType().toString() + ":" + getCert().getSubjectDN();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        if (getCert() != null) {
            try {
                this.encodedCert = this.cert.getEncoded();
            } catch (CertificateEncodingException e) {
                e.printStackTrace();
            }
        }

        out.defaultWriteObject();
        this.encodedCert = null;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.encodedCert != null) {
            this.cert = ProActiveSecurity.decodeCertificate(this.encodedCert);
        }

        this.encodedCert = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TypedCertificate)) {
            return false;
        }
        TypedCertificate otherCert = (TypedCertificate) obj;

        if (!otherCert.getType().match(this.getType())) {
            return false;
        }
        if (!otherCert.getCert().equals(this.getCert())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return this.cert.hashCode();
    }

}
