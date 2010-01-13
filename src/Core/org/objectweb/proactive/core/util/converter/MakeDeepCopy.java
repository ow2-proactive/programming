/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.core.util.converter;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class MakeDeepCopy {
    public static Logger logger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    protected enum ConversionMode {
        MARSHALL, OBJECT, PAOBJECT;
    }

    public static class WithMarshallStream {

        /**
         * Perform a deep copy of an object using a marshall stream.
         * @param o The object to be deep copied
         * @return the copy.
         * @throws IOException
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
         * @throws IOException
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
         * @throws IOException
         * @throws ClassNotFoundException
         */
        public static Object makeDeepCopy(Object o) throws IOException, ClassNotFoundException {
            byte[] array = ObjectToByteConverter.ProActiveObjectStream.convert(o);
            return ByteToObjectConverter.ProActiveObjectStream.convert(array);
        }
    }
}
