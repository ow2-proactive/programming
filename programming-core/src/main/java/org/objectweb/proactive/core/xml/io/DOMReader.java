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

import java.io.IOException;


/**
 *
 * Implement an XLMReader based on a existing DOM. We assume that the node given in parameter to the
 * constructor is the context of the tree to read.
 *
 * @author The ProActive Team
 * @version      0.91
 *
 */
public class DOMReader implements XMLReader {
    private org.w3c.dom.Element rootElement;

    private DOMAdaptor domAdaptor;

    public DOMReader(org.w3c.dom.Element rootElement, XMLHandler xmlHandler) {
        this.rootElement = rootElement;
        this.domAdaptor = new DOMAdaptor(xmlHandler);
    }

    // -- implements XMLReader ------------------------------------------------------
    public void read() throws IOException {
        domAdaptor.read(rootElement);
    }
}
