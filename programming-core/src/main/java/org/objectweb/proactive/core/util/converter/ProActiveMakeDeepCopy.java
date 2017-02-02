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

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class ProActiveMakeDeepCopy {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    protected enum ConversionMode {
        MARSHALL,
        OBJECT,
        PAOBJECT;
    }

    public static class WithMarshallStream {

        /**
         * Perform a deep copy of an object using a marshall stream.
         * @param o The object to be deep copied
         * @return the copy.
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        public static Object makeDeepCopy(Object o) throws IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.MarshallStream.convert(o);
            return ByteToObjectConverter.MarshallStream.convert(array);
        }
    }

    public static class WithObjectStream {

        /**
         * Perform a deep copy of an object using a regular object stream.
         * @param o The object to be deep copied
         * @return the copy.
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        public static Object makeDeepCopy(Object o) throws IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.ObjectStream.convert(o);
            return ByteToObjectConverter.ObjectStream.convert(array);
        }
    }

    public static class WithProActiveObjectStream {

        /**
         * Perform a deep copy of an object using a proactive object stream.
         * @param o The object to be deep copied
         * @return the copy.
         * @throws java.io.IOException
         * @throws ClassNotFoundException
         */
        public static Object makeDeepCopy(Object o) throws IOException, ClassNotFoundException {
            byte[] array = ProActiveObjectToByteConverter.ProActiveObjectStream.convert(o);
            return ProActiveByteToObjectConverter.ProActiveObjectStream.convert(array);
        }
    }
}
