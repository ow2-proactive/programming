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