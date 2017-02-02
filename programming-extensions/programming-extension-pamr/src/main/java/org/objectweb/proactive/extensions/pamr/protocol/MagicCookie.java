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
package org.objectweb.proactive.extensions.pamr.protocol;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.objectweb.proactive.core.util.ProActiveRandom;


public class MagicCookie {
    static final public int COOKIE_SIZE = 256;

    final private byte[] cookie;

    public MagicCookie(byte[] buf) {
        if (buf.length > COOKIE_SIZE) {
            throw new IllegalArgumentException("Buffer too long");
        }

        this.cookie = new byte[COOKIE_SIZE];
        System.arraycopy(buf, 0, this.cookie, 0, Math.min(buf.length, this.cookie.length));
    }

    public MagicCookie(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("str cannot be null");
        }

        byte[] buf = null;
        try {
            buf = str.getBytes("UTF-8");
            if (buf.length > COOKIE_SIZE) {
                throw new IllegalArgumentException("Cookie is too long. Must be shorter than " + (COOKIE_SIZE / 4) +
                                                   " characters");
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println();
        }

        this.cookie = new byte[COOKIE_SIZE];
        System.arraycopy(buf, 0, this.cookie, 0, Math.min(buf.length, this.cookie.length));
    }

    public MagicCookie() throws IllegalArgumentException {
        this.cookie = new byte[COOKIE_SIZE];
        ProActiveRandom.nextBytes(this.cookie);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(cookie);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MagicCookie other = (MagicCookie) obj;
        if (!Arrays.equals(cookie, other.cookie))
            return false;
        return true;
    }

    public byte[] getBytes() {
        return this.cookie.clone();
    }
}
