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

import org.xml.sax.SAXException;


/**
 * A handler for reading values from simple elements, such as
 * <pre><myElement>myValue</myElement></pre>
 *
 * @author The ProActive Team
 *
 */
public class SingleValueUnmarshaller extends BasicUnmarshaller {

    /**
     * The implementation of this method ensures that even though the element value is split into several chunks,
     * we concatenate the chunks to build the actual value.
     * see http://www.saxproject.org/faq.html (The ContentHandler.characters() callback is missing data!)
     * and http://xml.apache.org/xerces2-j/faq-sax.html#faq-2
     * This method is called several times by {@link org.objectweb.proactive.core.xml.io.DefaultHandlerAdapter#characters(char[], int, int)}
     * if the data is split into several chunks.
     */
    @Override
    public void readValue(String value) throws SAXException {

        /*
         * if (resultObject == null) {
         * setResultObject(value);
         * } else {
         * setResultObject(resultObject + value);
         * }
         */

        //Fix chunk reading problem
        if (resultObject != null) {
            value = resultObject + value;
        }

        //Transform variables into values if necessary
        if (org.objectweb.proactive.core.xml.VariableContractImpl.xmlproperties != null) {
            value = org.objectweb.proactive.core.xml.VariableContractImpl.xmlproperties.transform(value);
        }

        setResultObject(value);
    }
}
