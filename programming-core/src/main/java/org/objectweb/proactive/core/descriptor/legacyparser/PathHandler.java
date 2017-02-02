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
package org.objectweb.proactive.core.descriptor.legacyparser;

import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.io.Attributes;


/**
 * This class receives deployment events
 *
 * @author The ProActive Team
 * @version      1.0
 */
public class PathHandler extends BasicUnmarshaller implements ProActiveDescriptorConstants {
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //
    private static final String ORIGIN_ATTRIBUTE = "origin";

    private static final String USER_HOME_ORIGIN = "user.home";

    private static final String WORKING_DIRECTORY_ORIGIN = "user.dir";

    private static final String FROM_CLASSPATH_ORIGIN = "user.classpath";

    // private static final String PROACTIVE_ORIGIN = "proactive.home";
    private static final String DEFAULT_ORIGIN = USER_HOME_ORIGIN;

    private static final String VALUE_ATTRIBUTE = "value";

    private static final String userDir = System.getProperty("user.dir");

    private static final String userHome = System.getProperty("user.home");

    private static final String javaHome = System.getProperty("java.home");

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //
    public PathHandler() {
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    @Override
    public Object getResultObject() throws org.xml.sax.SAXException {
        return super.getResultObject();
    }

    @Override
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
        // read from XML
        //    String type = attributes.getValue(TYPE_ATTRIBUTE);
        //    if (! checkNonEmpty(type)) type = DEFAULT_TYPE;
        String origin = attributes.getValue(ORIGIN_ATTRIBUTE);
        if (!checkNonEmpty(origin)) {
            origin = DEFAULT_ORIGIN;
        }
        String value = attributes.getValue(VALUE_ATTRIBUTE);

        //        if (logger.isDebugEnabled()) {
        //            logger.debug("Found Path Element origin=" + origin + " value=" +
        //                value);
        //        }
        if (!checkNonEmpty(value)) {
            throw new org.xml.sax.SAXException("Path element defined without a value");
        }

        // build the associated string
        if (name.equals(ABS_PATH_TAG)) {
            setResultObject(value);
        } else if (name.equals(REL_PATH_TAG)) {
            if (origin.equals(USER_HOME_ORIGIN)) {
                setResultObject(resolvePath(userHome, value));
            } else if (origin.equals(WORKING_DIRECTORY_ORIGIN)) {
                setResultObject(resolvePath(userDir, value));
                //            } else if (origin.equals(PROACTIVE_ORIGIN)) {
                //                setResultObject(resolvePath(proActiveDir, value));
            } else if (origin.equals(FROM_CLASSPATH_ORIGIN)) {
                setResultObject(resolvePathFromClasspath(value));
            } else {
                throw new org.xml.sax.SAXException("Relative Path element defined with an unknown origin=" + origin);
            }
        }
    }

    //
    //  ----- PRIVATE METHODS -----------------------------------------------------------------------------------
    //
    private String resolvePath(String origin, String value) {
        java.io.File originDirectory = new java.io.File(origin);

        // in case of relative path, if the user put a / then remove it transparently
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        java.io.File file = new java.io.File(originDirectory, value);
        return file.getAbsolutePath();
    }

    private String resolvePathFromClasspath(String value) {
        ClassLoader cl = this.getClass().getClassLoader();
        java.net.URL url = cl.getResource(value);
        return url.getPath();
    }

    //
    //  ----- INNER CLASSES -----------------------------------------------------------------------------------
    //
}
