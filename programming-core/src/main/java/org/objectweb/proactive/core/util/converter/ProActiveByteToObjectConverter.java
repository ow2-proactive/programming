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
package org.objectweb.proactive.core.util.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.objectweb.proactive.core.mop.PAObjectInputStream;


/**
 * This class acts as a wrapper to enable the use of different serialization code
 * depending on the proactive configuration
 *
 */
public class ProActiveByteToObjectConverter {

    public static class MarshallStream {

        /**
         * Convert to an object using a marshall stream;
         *
         * @param byteArray the byte array to convert
         * @return the unserialized object
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        public static Object convert(byte[] byteArray) throws IOException, ClassNotFoundException {
            InputStream bais = new ByteArrayInputStream(byteArray);
            return convert(bais);
        }

        /**
         * Convert to an object using a marshall stream;
         *
         * @param is the input stream to convert
         * @return the unserialized object
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        public static Object convert(InputStream is) throws IOException, ClassNotFoundException {
            return ProActiveByteToObjectConverter.convert(is, MakeDeepCopy.ConversionMode.MARSHALL, null);
        }

    }

    public static class ProActiveObjectStream {

        /**
         * Convert to an object using a proactive object stream;
         * @param byteArray the byte array to convert
         * @return the unserialized object
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        public static Object convert(byte[] byteArray) throws IOException, ClassNotFoundException {
            InputStream bais = new ByteArrayInputStream(byteArray);
            return convert(bais, null);
        }

        /**
         * Convert to an object using a proactive object stream;
         *
         * @param is the input stream to convert
         * @param cl the classloader where to load the classes
         * @return the unserialized object
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        public static Object convert(InputStream is, ClassLoader cl) throws IOException, ClassNotFoundException {
            return ProActiveByteToObjectConverter.convert(is, MakeDeepCopy.ConversionMode.PAOBJECT, cl);
        }
    }

    private static Object convert(InputStream is, MakeDeepCopy.ConversionMode conversionMode, ClassLoader cl)
            throws IOException, ClassNotFoundException {
        return standardConvert(is, conversionMode, cl);
    }

    private static Object readFromStream(ObjectInputStream objectInputStream)
            throws IOException, ClassNotFoundException {
        return objectInputStream.readObject();
    }

    private static Object standardConvert(InputStream is, MakeDeepCopy.ConversionMode conversionMode, ClassLoader cl)
            throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = null;

        try {
            // we use enum and static calls to avoid object instanciation
            if (conversionMode == MakeDeepCopy.ConversionMode.MARSHALL) {
                objectInputStream = new SunMarshalInputStream(is);
            } else if (conversionMode == MakeDeepCopy.ConversionMode.PAOBJECT) {
                objectInputStream = new PAObjectInputStream(is);
            } else /* (conversionMode == ObjectToByteConverter.ConversionMode.OBJECT) */
            {
                // if a classloader is specified, use it !
                if (cl != null) {
                    objectInputStream = new ObjectInputStreamWithClassLoader(is, cl);
                } else {
                    objectInputStream = new ObjectInputStream(is);
                }
            }
            return ProActiveByteToObjectConverter.readFromStream(objectInputStream);
        } finally {
            // close streams;
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            is.close();
        }
    }

    /*
     * Standard ObjectInputStream that loads classes in the specified classloader.
     */
    private static class ObjectInputStreamWithClassLoader extends ObjectInputStream {
        private ClassLoader cl;

        public ObjectInputStreamWithClassLoader(InputStream in, ClassLoader cl) throws IOException {
            super(in);
            this.cl = cl;
        }

        protected Class<?> resolveClass(java.io.ObjectStreamClass v) throws IOException, ClassNotFoundException {
            if (cl == null) {
                return super.resolveClass(v);
            } else {
                // should not use directly loadClass due to jdk bug 6434149
                return Class.forName(v.getName(), true, this.cl);
            }
        }
    }

}
