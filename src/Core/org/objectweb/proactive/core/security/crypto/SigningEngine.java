/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package org.objectweb.proactive.core.security.crypto;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignedObject;


public class SigningEngine implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 430L;
    private transient Signature signingEngine;

    public SigningEngine() {
        try {
            signingEngine = Signature.getInstance("SHA-1/RSA", "BC");
        } catch (Exception e) {
            System.out.println("Exception in SigningEngine instanciation : " + e);
            e.printStackTrace();
        }
    }

    public Object signObject(Serializable object, PrivateKey privateKey) {
        try {
            return new SignedObject(object, privateKey, signingEngine);
        } catch (Exception e) {
            System.out.println("Exception in object signature : " + e);
            System.out.println(privateKey);
            e.printStackTrace();
        }

        return null;
    }

    public boolean checkSignature(Object signedObject, PublicKey publicKey) {
        try {
            if (((SignedObject) signedObject).verify(publicKey, signingEngine)) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("Exception object signature checking :" + e);
            e.printStackTrace();
        }

        return false;
    }
}
