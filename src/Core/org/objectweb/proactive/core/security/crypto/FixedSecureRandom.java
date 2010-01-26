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

import java.security.SecureRandom;


class FixedSecureRandom extends SecureRandom {
    /**
     * 
     */
    private static final long serialVersionUID = 420L;
    byte[] seed = { (byte) 0xaa, (byte) 0xfd, (byte) 0x12, (byte) 0xf6, (byte) 0x59, (byte) 0xca,
            (byte) 0xe6, (byte) 0x34, (byte) 0x89, (byte) 0xb4, (byte) 0x79, (byte) 0xe5, (byte) 0x07,
            (byte) 0x6d, (byte) 0xde, (byte) 0xc2, (byte) 0xf0, (byte) 0x6c, (byte) 0xb5, (byte) 0x8f };

    @Override
    public void nextBytes(byte[] bytes) {
        int offset = 0;

        while ((offset + seed.length) < bytes.length) {
            System.arraycopy(seed, 0, bytes, offset, seed.length);
            offset += seed.length;
        }

        System.arraycopy(seed, 0, bytes, offset, bytes.length - offset);
    }
}
