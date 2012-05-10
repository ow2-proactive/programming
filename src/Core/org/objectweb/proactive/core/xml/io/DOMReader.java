/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
