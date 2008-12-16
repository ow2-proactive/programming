/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.debug.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import cern.colt.Arrays;


public class Data implements Serializable {
    private static final long serialVersionUID = 4068435103580170397L;

    /** data readed */
    private byte[] data;

    /** number of byte readed */
    private int length;

    public Data() {
    }

    public Data(int length) {
        this.data = new byte[length];
        this.length = 0;
    }

    public void write(OutputStream out) throws IOException {
        try {
            out.write(data, 0, length);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.out.println(toString());
            System.err.println("data.length: " + data.length + ", length: " + length);
            throw e;
        }
    }

    public void read(String str) throws UnsupportedEncodingException {
        data = str.getBytes("UTF-8");
        length = data.length;
    }

    public int read(InputStream in) throws IOException {
        length = in.read(data, 0, data.length);
        return length;
    }

    public boolean isEmpty() {
        return length <= 0;
    }

    public String toString() {
        if (isEmpty()) {
            return "";
        }
        String str;
        byte[] arr = Arrays.trimToCapacity(data, length);
        try {
            str = new String(arr, "UTF8");
        } catch (UnsupportedEncodingException e) {
            str = Arrays.toString(arr);
        }
        return str;
    }
}