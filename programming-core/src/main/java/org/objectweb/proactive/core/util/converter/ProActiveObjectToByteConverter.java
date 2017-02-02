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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.objectweb.proactive.core.mop.PAObjectOutputStream;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy.ConversionMode;


/**
 * This class acts as a wrapper to enable the use of different serialization code
 * depending on the proactive configuration
 *
 */
public class ProActiveObjectToByteConverter {

    static {
        // resolve PROACTIVE-742
        ProActiveRuntimeImpl.getProActiveRuntime();
    }

    public static class MarshallStream {

        /**
         * Convert to an object using a marshall stream;
         * @param byteArray the byte array to covnert
         * @return the unserialized object
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */

        /**
         * Convert an object to a byte array using a marshall stream
         * @param o The object to convert.
         * @return The object converted to a byte array
         * @throws java.io.IOException
         */
        public static byte[] convert(Object o) throws IOException {
            return ProActiveObjectToByteConverter.convert(o, ConversionMode.MARSHALL);
        }
    }

    public static class ProActiveObjectStream {

        /**
         * Convert an object to a byte array using a proactive object stream
         * @param o The object to convert.
         * @return The object converted to a byte array
         * @throws java.io.IOException
         */
        public static byte[] convert(Object o) throws IOException {
            return ProActiveObjectToByteConverter.convert(o, ConversionMode.PAOBJECT);
        }
    }

    private static byte[] convert(Object o, ConversionMode conversionMode) throws IOException {
        return standardConvert(o, conversionMode);
    }

    private static void writeToStream(ObjectOutputStream objectOutputStream, Object o) throws IOException {
        objectOutputStream.writeObject(o);
        objectOutputStream.flush();
    }

    private static byte[] standardConvert(Object o, ConversionMode conversionMode) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
        ObjectOutputStream objectOutputStream = null;

        try {
            // we use enum and static calls to avoid object instanciation
            if (conversionMode == ConversionMode.MARSHALL) {
                objectOutputStream = new SunMarshalOutputStream(byteArrayOutputStream);
            } else if (conversionMode == ConversionMode.OBJECT) {
                objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            } else if (conversionMode == ConversionMode.PAOBJECT) {
                objectOutputStream = new PAObjectOutputStream(byteArrayOutputStream);
            }

            ProActiveObjectToByteConverter.writeToStream(objectOutputStream, o);
            return byteArrayOutputStream.toByteArray();
        } finally {
            // close streams
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            byteArrayOutputStream.close();
        }
    }
}
