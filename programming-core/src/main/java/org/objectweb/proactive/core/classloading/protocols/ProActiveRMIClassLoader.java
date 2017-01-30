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
package org.objectweb.proactive.core.classloading.protocols;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;


/**
 * A Custom RMI Classloader to allow dynamic code downloading through ProActive calls.
 *
 * ProActive provides several communication protocol. All of theses communication protocols rely
 * on the RMI marshaling/unmarshaling process. This classloader is used to extend the marshaling
 * step by adding the local ProActive runtime to the codebase. Unfortunately there is no other
 * way to add this URL than using a custom classloader.
 *
 * All the method calls are delegated to the default SPI, {@link #getClassAnnotation(Class)} excepted
 */
public class ProActiveRMIClassLoader extends RMIClassLoaderSpi {
    /** The default SPI provided by RMI*/
    final private RMIClassLoaderSpi defaultSPI;

    final private AtomicReference<String> cachedPACodebase;

    public ProActiveRMIClassLoader() {
        this.defaultSPI = RMIClassLoader.getDefaultProviderInstance();
        this.cachedPACodebase = new AtomicReference<String>(null);
    }

    @Override
    public String getClassAnnotation(Class<?> cl) {
        String codebase = this.defaultSPI.getClassAnnotation(cl);
        if (codebase == null) {
            codebase = "";
        }

        String pacodebase = cachedPACodebase.get();
        if (pacodebase == null) {
            pacodebase = CentralPAPropertyRepository.PA_CODEBASE.getValue();
            if (pacodebase != null) {
                cachedPACodebase.compareAndSet(null, pacodebase);
                codebase += " " + pacodebase;
            }
        } else {
            codebase += " " + pacodebase;
        }

        return codebase;
    }

    @Override
    public ClassLoader getClassLoader(String codebase) throws MalformedURLException {
        return this.defaultSPI.getClassLoader(codebase);
    }

    @Override
    public Class<?> loadClass(String codebase, String name, ClassLoader defaultLoader)
            throws MalformedURLException, ClassNotFoundException {
        return this.defaultSPI.loadClass(codebase, name, defaultLoader);
    }

    @Override
    public Class<?> loadProxyClass(String codebase, String[] interfaces, ClassLoader defaultLoader)
            throws MalformedURLException, ClassNotFoundException {
        return this.defaultSPI.loadProxyClass(codebase, interfaces, defaultLoader);
    }
}
