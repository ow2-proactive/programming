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
package org.objectweb.proactive.core.xml.io;

/**
 *
 * Receives SAX event and pass them on
 *
 * @author The ProActive Team
 * @version      0.91
 *
 */
public interface XMLHandler {

    /**
     * Receives notification that an XML element of given name and attributes has been read in the
     * XML being deserialized
     * @param name the name of the element just opened
     * @param attributes the attributes of this element
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void startElement(String name, Attributes attributes) throws org.xml.sax.SAXException;

    /**
     * Receives notification that an XML value has been read in the XML being deserialized
     * @param value the value just read
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void readValue(String value) throws org.xml.sax.SAXException;

    /**
     * Receives notification that the end of an XML element of given name has been read
     * in the XML being deserialized
     * @param name the name of the element just ended
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void endElement(String name) throws org.xml.sax.SAXException;

    /**
     * Receives notification that an XML prefix has just been defined
     * @param prefix the name of the prefix defined
     * @param uri the uri qualifying the prefix
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException;

    /**
     * Receives notification that the scope of an XML prefix has just ended
     * @param prefix the name of the prefix ended
     * @exception org.xml.sax.SAXException if an exception occur during processing
     */
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException;
}
