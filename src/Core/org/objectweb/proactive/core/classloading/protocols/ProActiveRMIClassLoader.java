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
