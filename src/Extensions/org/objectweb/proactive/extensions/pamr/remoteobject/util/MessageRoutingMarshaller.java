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
package org.objectweb.proactive.extensions.pamr.remoteobject.util;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;


// FIXME: This is exactly the same marshaller than HTTPMarshaller
// They should be factorized somewhere else
/** Object marshaller and unmarshaller
 * 
 *  @since ProActive 4.1.0
 */
public class MessageRoutingMarshaller {
    static final Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.FORWARDING_REMOTE_OBJECT);

    public static byte[] marshallObject(Object o) {
        byte[] buffer = null;

        try {
            buffer = ObjectToByteConverter.MarshallStream.convert(o);
        } catch (IOException e) {
            ProActiveLogger.logImpossibleException(logger, e);
        }

        return buffer;
    }

    public static Object unmarshallObject(byte[] bytes) {
        Object o = null;
        try {
            o = ByteToObjectConverter.MarshallStream.convert(bytes);
        } catch (IOException e) {
            ProActiveLogger.logImpossibleException(logger, e);
        } catch (ClassNotFoundException e) {
            ProActiveLogger.logImpossibleException(logger, e);
        }

        return o;
    }
}
