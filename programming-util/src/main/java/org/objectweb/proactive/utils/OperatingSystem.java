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
package org.objectweb.proactive.utils;

import java.io.Serializable;


public enum OperatingSystem implements Serializable {
    windows(';', '\\'),
    unix(':', '/');
    protected char pathSeparator;

    protected char fileSeparator;

    OperatingSystem(char pathSeparator, char fileSeparator) {
        this.pathSeparator = pathSeparator;
        this.fileSeparator = fileSeparator;
    }

    public char pathSeparator() {
        return pathSeparator;
    }

    public char fileSeparator() {
        return fileSeparator;
    }

    static public OperatingSystem getOperatingSystem() {
        String val = System.getProperty("os.name");
        if (val.contains("Windows")) {
            return windows;
        }

        return unix;
    }
}
