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
package org.objectweb.proactive.core.util;

import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A cache for classes bytecode.
 * It also contains bytecodes of stubs generated
 * by the MOP.
 *
 * @author The ProActive Team
 *
 */
public class ClassDataCache {
    static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);

    private static ClassDataCache classCache = new ClassDataCache();

    private static Map<String, byte[]> classStorage;

    private ClassDataCache() {
        classStorage = new Hashtable<String, byte[]>();
    }

    public static ClassDataCache instance() {
        return classCache;
    }

    /**
     * Indicates whether the bytecode for the given class is already in cache.
     * @param className name of the class
     * @return true if the class is in the cache
     */
    public boolean contains(String className) {
        return classStorage.containsKey(className);
    }

    /**
     * Associates classname and bytecode in the cache.
     * @param fullname name of the class
     * @param classData bytecode of the class
     */
    public void addClassData(String fullname, byte[] classData) {
        if (logger.isTraceEnabled()) {
            logger.trace(ProActiveRuntimeImpl.getProActiveRuntime().getURL() + " --> " +
                         ("ClassDataCache caching class " + fullname));
        }
        classStorage.put(fullname, classData);
    }

    /**
     * Returns the bytecode for a given class name
     * @param fullname the name of the class
     */
    public byte[] getClassData(String fullname) {
        if (logger.isTraceEnabled()) {
            logger.trace(ProActiveRuntimeImpl.getProActiveRuntime().getURL() + " --> " +
                         ("ClassDataCache was asked for class " + fullname));
        }
        return classStorage.get(fullname);
    }
}
