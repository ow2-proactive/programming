/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
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
import java.util.Date;


/**
 * The CertificateProperty class is used as an attribute of the PublicCertificate and PrivateCertificate classes.
 *
 * @author The ProActive Team
 * <br>created    July 19, 2001
 */
public class CertificateProperty implements Serializable {
    private Date deliveryDate;
    private Date expirationDate;
    private byte[] randomData;

    /**
     *  Constructor for the CertificateProperty object
     *
     * @param  deliveryDate    Date of the Certificate Generation
     * @param  expirationDate  Date of the Certificate expiration
     * @since
     */
    public CertificateProperty(Date deliveryDate, Date expirationDate) {
        this.deliveryDate = deliveryDate;
        this.expirationDate = expirationDate;
    }

    /**
     *
     *
     * @param  msSince1970  Certificate delivery date in ms (number of ms since 1970)
     * @since
     */
    public void set_deliveryDate(long msSince1970) {
        deliveryDate = new Date(msSince1970);
    }

    /**
     *
     *
     * @param  msSince1970  Certificate expiration date in ms (number of ms since 1970)
     * @since
     */
    public void set_expirationDate(long msSince1970) {
        expirationDate = new Date(msSince1970);
    }

    /**
     *
     *
     * @return   Certificate delivery date in ms (number of ms since 1970)
     * @since
     */
    public Date get_deliveryDate() {
        return deliveryDate;
    }

    /**
     *
     *
     * @return   Certificate expiration date in ms (number of ms since 1970)
     * @since
     */
    public Date get_expirationDate() {
        return expirationDate;
    }
}
