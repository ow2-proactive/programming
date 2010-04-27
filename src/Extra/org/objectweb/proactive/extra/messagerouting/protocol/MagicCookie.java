package org.objectweb.proactive.extra.messagerouting.protocol;

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
                throw new IllegalArgumentException("Cookie is too long. Must be shorter than " +
                    (COOKIE_SIZE / 4) + " characters");
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
