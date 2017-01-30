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
package org.objectweb.proactive.core.remoteobject.http.util;

import java.io.IOException;

import org.objectweb.proactive.core.util.converter.ProActiveByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ProActiveObjectToByteConverter;


/**
 * @author The ProActive Team
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpMarshaller {

    /**
     *
     * @param o
     * @return byte array representation of the object o
     */
    public static byte[] marshallObject(Object o) {
        byte[] buffer = null;

        try {
            buffer = ProActiveObjectToByteConverter.ProActiveObjectStream.convert(o);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return buffer;
    }

    public static Object unmarshallObject(byte[] bytes) {
        Object o = null;
        try {
            o = ProActiveByteToObjectConverter.ProActiveObjectStream.convert(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return o;
    }
}
