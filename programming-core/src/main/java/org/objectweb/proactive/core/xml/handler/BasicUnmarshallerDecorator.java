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

import org.objectweb.proactive.core.xml.io.Attributes;


/**
 *
 * Receives SAX event and pass them on
 *
 * @author The ProActive Team
 * @version      0.91
 *
 */
public class BasicUnmarshallerDecorator extends AbstractUnmarshallerDecorator {
    protected Object resultObject;

    protected boolean isResultValid = true;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    public BasicUnmarshallerDecorator(boolean lenient) {
        super(lenient);
    }

    public BasicUnmarshallerDecorator() {
        super();
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements UnmarshallerHandler ------------------------------------------------------
    //
    public Object getResultObject() throws org.xml.sax.SAXException {
        if (!isResultValid) {
            throw new org.xml.sax.SAXException("The result object is not valid");
        }
        Object o = resultObject;
        resultObject = null;
        isResultValid = false;
        return o;
    }

    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
    }

    //
    // -- PROTECTED METHODS ------------------------------------------------------
    //
    @Override
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
            throws org.xml.sax.SAXException {
        setResultObject(activeHandler.getResultObject());
    }

    protected void setResultObject(Object value) {
        isResultValid = true;
        resultObject = value;
    }

    //
    // -- PRIVATE METHODS ------------------------------------------------------
    //
    //
    // -- INNER CLASSES ------------------------------------------------------
    //
}
