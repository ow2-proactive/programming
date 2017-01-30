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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 *
 * @since ProActive 4.3.0
 */
public class ProActiveRemoteClassLoader {

    final static private Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);

    private final Map<String, Loader> loaderCache;

    public ProActiveRemoteClassLoader() {
        loaderCache = new ConcurrentHashMap<String, Loader>();
    }

    public Class<?> loadClass(String clazzName, String runtimeURL) throws ClassNotFoundException {

        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        // attempt to load using the parent
        try {
            return parent.loadClass(clazzName);
        } catch (ClassNotFoundException e) {
            try {
                Loader pamrLoader = getOrCreateLoader(parent, runtimeURL);
                return pamrLoader.loadClass(clazzName);
            } catch (ProActiveException proActiveEx) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cannot load " + clazzName +
                                 " using the pamr class loader, reason: cannot look up the runtime where the class data is, at url " +
                                 runtimeURL);
                }
                throw e;
            }
        }
    }

    private Loader getOrCreateLoader(ClassLoader parent, String runtimeURL) throws ProActiveException {

        synchronized (loaderCache) {
            if (loaderCache.containsKey(runtimeURL)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("ClassLoader for the URL " + runtimeURL + " found in the cache ");
                }
                return loaderCache.get(runtimeURL);
            }

            // we know that it does not contain the key
            // hold the loaderCache lock; we want the lookup operation to be performed only once
            // TODO the lock should be on a per-runtimeURL basis
            if (logger.isTraceEnabled()) {
                logger.trace("Did not find ClassLoader for the URL " + runtimeURL +
                             " in the cache, creating a new one...");
            }
            // lookup the remote part
            ProActiveRuntime rt = RuntimeFactory.getRuntime(runtimeURL);
            Loader loader = new Loader(parent, rt);
            loaderCache.put(runtimeURL, loader);
            if (logger.isTraceEnabled()) {
                logger.trace("Succesfully created a new ClassLoader for the URL " + runtimeURL);
            }
            return loader;
        }

    }

    private static class Loader extends ClassLoader {

        private final ProActiveRuntime clazzLocation;

        public Loader(ClassLoader parent, ProActiveRuntime runtime) {
            super(parent);
            this.clazzLocation = runtime;
        }

        protected Class<?> findClass(String clazzName) throws ClassNotFoundException {
            // first, the parent
            try {
                Class<?> ret = this.getParent().loadClass(clazzName);
                if (logger.isTraceEnabled()) {
                    logger.trace("Class " + clazzName + " loaded by the parent class loader " + this.getParent());
                }
                return ret;
            } catch (ClassNotFoundException e) {
                try {
                    if (clazzLocation == null)
                        throw new ClassNotFoundException("Cannot load class " + clazzName +
                                                         " reason: the remote ProActive runtime where this class resides is not available");

                    byte[] clazzData = readClassData(clazzName);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Succeffully downloaded " + clazzName + " class definition ");
                    }
                    return defineClass(clazzName, clazzData, 0, clazzData.length);
                } catch (ClassNotFoundException e2) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot load class from the remote ProActive runtime:" + e.getMessage(), e);
                    }
                    throw new ClassNotFoundException(clazzName + " (both locally and from " + clazzLocation + ")", e2);
                }
            }
        }

        private byte[] readClassData(String clazzName) throws ClassNotFoundException {

            if (logger.isTraceEnabled())
                logger.trace("Attempt to download class " + clazzName + " from the remote runtime");
            try {
                byte[] b = this.clazzLocation.getClassData(clazzName);
                if (b == null || b.length == 0) {
                    throw new ClassNotFoundException("Class not found on " + clazzLocation + ": " + clazzName);
                }

                return b;
            } catch (ProActiveRuntimeException e) {
                throw new ClassNotFoundException("Communication error occured while trying to download " + clazzName +
                                                 " from" + clazzLocation);
            }

        }
    }

}
