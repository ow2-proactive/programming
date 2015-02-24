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
package org.objectweb.proactive.core.mop;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;


public class MOPClassLoader extends URLClassLoader {

    private static Logger logger = ProActiveLogger.getLogger(Loggers.MOP);

    public Map<String, byte[]> classDataCache = new HashMap<String, byte[]>();

    // lazy-loaded singleton
    private static class LazyHolder {

        private static final MOPClassLoader INSTANCE = createMOPClassLoader();

    }

    /**
     * Return the unique MOPClassLoader for the current JVM.
     * Create it if it does not exist.
     */
    public static MOPClassLoader getMOPClassLoader() {
        return LazyHolder.INSTANCE;
    }

    public MOPClassLoader() {
        super(new URL[] {});
    }

    private MOPClassLoader(ClassLoader parent, URL[] urls) {
        super(urls, parent);
    }

    /**
     * Get the bytecode of a stub given its name. If the stub can not be found
     * in the cache, the MOPClassLoader tries to generate it.
     *
     * @param classname The name of the stub class
     * @return An array representing the bytecode of the stub, null if the
     * stub could not be found or created
     */
    public synchronized byte[] getClassData(String classname) {
        byte[] cb = classDataCache.get(classname);

        if (cb == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("MOPClassLoader: class " + classname + " not found, trying to generate it");
            }

            try {
                this.loadClass(classname);
            } catch (ClassNotFoundException e) {
                logger.debug(e);
            }

            cb = classDataCache.get(classname);
        }
        return cb;
    }

    public void launchMain(String[] args) throws Throwable {
        try {
            // Looks up the class that contains main
            Class<?> cl = Class.forName(args[0], true, this);

            // Looks up method main
            Class<?>[] argTypes = { args.getClass() };
            Method mainMethod = cl.getMethod("main", argTypes);

            // And calls it
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);

            Object[] mainArgs = { newArgs };
            mainMethod.invoke(null, mainArgs);
        } catch (ClassNotFoundException e) {
            logger.error("Launcher: cannot find class " + args[0]);
        } catch (NoSuchMethodException e) {
            logger.error("Launcher: class " + args[0] +
                " does not contain have method void 'public void main (String[])'");
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }

        return;
    }

    protected static MOPClassLoader createMOPClassLoader() {
        // Gets the current classloader
        ClassLoader currentClassLoader = null;

        // TODO(lpellegr): I think the following block of code could be replaced
        // by a simple currentClassLoader = MOPClassLoader.class.getClassLoader()
        // but more investigations would be necessary to check that anything is broken
        try {
            Class<?> c = Class.forName("org.objectweb.proactive.core.mop.MOPClassLoader");
            currentClassLoader = c.getClassLoader();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        URL[] urls = null;

        // Checks if the current classloader is actually an instance of
        // java.net.URLClassLoader, or of one of its subclasses.
        if (currentClassLoader instanceof java.net.URLClassLoader) {
            // Retrieves the set of URLs from the current classloader     
            urls = ((URLClassLoader) currentClassLoader).getURLs();
        } else {
            urls = new URL[0];
        }

        // Creates a new MOPClassLoader
        return new MOPClassLoader(currentClassLoader, urls);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, null, null);
    }

    public Class<?> loadClass(String name, Class<?>[] genericParameters) throws ClassNotFoundException {
        return this.loadClass(name, genericParameters, null);
    }

    protected synchronized Class<?> loadClass(String name, Class<?>[] genericParameters, ClassLoader cl)
            throws ClassNotFoundException {
        if (this.getParent() != null) {
            try {
                return this.getParent().loadClass(name);
            } catch (ClassNotFoundException e) {
                // proceeding
            }
        } else {
            //ok, we don't have any parent, so maybe we previously
            //defined the stub class using the context class loader
            //we check here
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(name);
            } catch (ClassNotFoundException e) {
                //no luck, proceed
            }
        }
        try {
            if (cl != null) {
                return cl.loadClass(name);
            } else {
                return Class.forName(name);
            }
        } catch (ClassNotFoundException e) {
            if (PAProxyBuilder.doesClassNameEndWithPAProxySuffix(name) && !(Utils.isStubClassName(name))) {
                try {
                    byte[] data = PAProxyBuilder.generatePAProxy(PAProxyBuilder
                            .getBaseClassNameFromPAProxyName(name));
                    classDataCache.put(name, data);

                    Class<?> baseCl = Class.forName(PAProxyBuilder.getBaseClassNameFromPAProxyName(name));

                    if (cl == null) {
                        cl = baseCl.getClassLoader();
                    }

                    Class<?> clazz = callDefineClassUsingReflection(name, data, cl);
                    logger.debug("Generated paproxy class : " + name + "loaded into " +
                        clazz.getClassLoader().toString());
                    return clazz;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.debug(ex);
                    throw new ClassNotFoundException(ex.getMessage());
                }

            }

            if (Utils.isStubClassName(name)) {
                // Test if the name of the class is actually a request for
                // a stub class to be created

                //    e.printStackTrace();
                String classname = Utils.convertStubClassNameToClassName(name);

                if (PAProxyBuilder.doesClassNameEndWithPAProxySuffix(classname)) {
                    try {
                        loadClass(classname);
                        //                    callDefineClassUsingReflection(classname, data);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        logger.debug(ex);
                        throw new ClassNotFoundException(ex.getMessage());
                    }
                }

                byte[] data = null;

                data = JavassistByteCodeStubBuilder.create(classname, genericParameters);
                classDataCache.put(name, data);

                // We use introspection to invoke the defineClass method to avoid the normal 
                // class Access checking. This method is supposed to be protected which means 
                // we should not be accessing it but the access policy file allows us to access it freely.
                try {
                    Class<?> clazz = callDefineClassUsingReflection(name, data, null);

                    logger.debug("Generated class : " + name + "loaded into " +
                        clazz.getClassLoader().toString());
                    return clazz;
                } catch (Exception ex) {
                    logger.debug(ex);
                    throw new ClassNotFoundException(ex.getMessage());
                }
            } else {
                logger.debug("Cannot generate class " + name + " as a stub class");
                throw e;
            }
        }
    }

    private Class<?> callDefineClassUsingReflection(String name, byte[] data, ClassLoader delegateCl)
            throws ClassNotFoundException, SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> clc = Class.forName("java.lang.ClassLoader");
        Class<?>[] argumentTypes = new Class<?>[5];
        argumentTypes[0] = name.getClass();
        argumentTypes[1] = data.getClass();
        argumentTypes[2] = Integer.TYPE;
        argumentTypes[3] = Integer.TYPE;
        argumentTypes[4] = Class.forName("java.security.ProtectionDomain");

        Method m = clc.getDeclaredMethod("defineClass", argumentTypes);
        m.setAccessible(true);

        Object[] effectiveArguments = new Object[5];
        effectiveArguments[0] = name;
        effectiveArguments[1] = data;
        effectiveArguments[2] = Integer.valueOf(0);
        effectiveArguments[3] = Integer.valueOf(data.length);
        effectiveArguments[4] = this.getClass().getProtectionDomain();

        if (delegateCl != null) {
            return (Class<?>) m.invoke(delegateCl, effectiveArguments);
        }

        //  we have been loaded through the bootclasspath
        // so we use the context classloader
        if (this.getParent() == null) {
            return (Class<?>) m.invoke(Thread.currentThread().getContextClassLoader(), effectiveArguments);
        } else {
            return (Class<?>) m.invoke(this.getParent(), effectiveArguments);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream is = null;

        name = convertResourceToClass(name);

        byte[] data = getClassData(name);
        if (data != null) {
            is = new ByteArrayInputStream(data);
        }

        return is;
    }

    @Override
    public URL getResource(String name) {
        URL url = super.getResource(name);
        name = convertResourceToClass(name);
        if (url == null) {
            byte[] data = getClassData(name);
            if (data != null) {
                try {
                    url = new URL("pamop:///" + name);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        return url;
    }

    private String convertResourceToClass(String ressource) {
        String s = ressource.replace(".class", "");
        s = s.replace("/", ".");
        return s;
    }

}
