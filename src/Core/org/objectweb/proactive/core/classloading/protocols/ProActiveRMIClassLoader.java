/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.classloading.protocols;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoader;
import java.rmi.server.RMIClassLoaderSpi;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.proactive.core.config.PAProperties;


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
            pacodebase = PAProperties.PA_CODEBASE.getValue();
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
