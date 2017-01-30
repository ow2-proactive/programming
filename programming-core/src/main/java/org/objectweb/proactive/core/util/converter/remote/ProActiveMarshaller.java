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
package org.objectweb.proactive.core.util.converter.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This class is responsible for the serialization/deserialization process
 *
 * @since ProActive 4.3.0
 */
public class ProActiveMarshaller {

    private final String localRuntimeURL;

    public ProActiveMarshaller(String localRuntimeURL) {
        this.localRuntimeURL = localRuntimeURL;
    }

    public byte[] marshallObject(Object o) throws IOException {
        ProActiveMarshalOutputStream serializer = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            serializer = new ProActiveMarshalOutputStream(baos, this.localRuntimeURL);
            serializer.writeObject(o);
            serializer.flush();
            return baos.toByteArray();
        } finally {
            // cleanup
            if (serializer != null)
                serializer.close(); // this will also close the underlying baos
        }
    }

    public Object unmarshallObject(byte[] bytes) throws IOException, ClassNotFoundException {
        ProActiveMarshalInputStream deserializer = null;
        try {
            deserializer = new ProActiveMarshalInputStream(new ByteArrayInputStream(bytes));
            return deserializer.readObject();
        } finally {
            // cleanup
            if (deserializer != null)
                deserializer.close(); // this will also close the underlying bais
        }
    }

    public Object unmarshallObject(InputStream is) throws IOException, ClassNotFoundException {
        ProActiveMarshalInputStream deserializer = null;
        try {
            deserializer = new ProActiveMarshalInputStream(is);
            return deserializer.readObject();
        } finally {
            // cleanup
            if (deserializer != null)
                deserializer.close(); // this will also close the underlying bais
        }
    }
}
