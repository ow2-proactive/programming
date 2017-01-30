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
package org.objectweb.proactive.core.xml.handler;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.XMLHandler;


/**
 *
 * Receives SAX event and pass them on
 *
 * @author The ProActive Team
 * @version      0.91
 *
 */
public interface UnmarshallerHandler extends XMLHandler {
    static Logger logger = ProActiveLogger.getLogger(Loggers.XML);

    /**
     * Returns the object resulting of the processing of the SAX events.
     * @return the object resulting of the processing of the SAX events or null
     * @exception org.xml.sax.SAXException if the state of the handler does not allow to
     *            return any resulting object.
     */
    public Object getResultObject() throws org.xml.sax.SAXException;

    /**
     * Receives notification that the XML element of given name and attributes has been read in the
     * XML being deserialized. This element is the context element of the Object being unmarshalled.
     * This context element is the element that has triggered the use of this handler.
     * @param name the name of the element just opened
     * @param attributes the attributes of this element
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException;
}
