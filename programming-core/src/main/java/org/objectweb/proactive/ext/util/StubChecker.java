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
package org.objectweb.proactive.ext.util;

import java.io.File;
import java.io.FileFilter;
import java.io.ObjectStreamClass;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.mop.Utils;


/**
 * StubChecker check the stub of the class given as argument.<br>
 * The generated stub must have the same generated serial version UID than the one
 * inside the second given arguments representing the root of an other project.<br>
 * <br>
 * As a consequence, this class must be used between two maintenance version, detecting if stubs 
 * are compatible between each other.<br>
 * <br>
 * ClassLoader are based on dist/lib directory, so compile and deploy are useful before checking stubs.
 *
 * @author The ProActive Team
 * @since ProActive Programming 4.2.1
 */
public final class StubChecker {

    public static void main(String[] args) throws Exception {
        String classArg = null;
        File rootDir_current = null;
        File rootDir_other = null;

        int index = 0;
        while (index < args.length) {
            if (args[index].equals("-class")) {
                classArg = args[index + 1];
                index += 2;
            } else if (args[index].equals("-rootDirV1")) {
                String rootStr = args[index + 1];
                rootDir_current = new File(rootStr);
                if (!rootDir_current.isDirectory()) {
                    logAndExit("ERROR : rootDirV1 is not a valid directory !");
                }
                if (!rootDir_current.exists()) {
                    logAndExit("ERROR : rootDirV1 does not exist !");
                }
                index += 2;
            } else if (args[index].equals("-rootDirV2")) {
                String rootStr = args[index + 1];
                rootDir_other = new File(rootStr);
                if (!rootDir_other.isDirectory()) {
                    logAndExit("ERROR : rootDirV2 is not a valid directory !");
                }
                if (!rootDir_other.exists()) {
                    logAndExit("ERROR : rootDirV2 does not exist !");
                }
                index += 2;
            } else {
                usage();
                System.exit(1);
            }
        }

        if (classArg == null || rootDir_current == null || rootDir_other == null) {
            usage();
            System.exit(1);
        }

        //Create classLoader for current version
        ClassLoader cl_current = getClassLoader(rootDir_current);
        ClassLoader cl_other = getClassLoader(rootDir_other);

        //local stub class name in current version
        String className = processClassName(classArg);
        String stubClassName = Utils.convertClassNameToStubClassName(className, null);

        //declare both SerialVersionUID
        long suid_current = 0, suid_other = 0;

        //create stub for current version
        try {
            Class<?> stub_current = Class.forName(stubClassName, true, cl_current);
            suid_current = ObjectStreamClass.lookup(stub_current).getSerialVersionUID();
        } catch (ClassNotFoundException cnfe) {
            logAndExit("Check Stub '" + stubClassName + "' : WARNING - Stub not found in " +
                       rootDir_current.getAbsolutePath());
        }

        //create stub for other version
        try {
            Class<?> stub_other = Class.forName(stubClassName, true, cl_other);
            suid_other = ObjectStreamClass.lookup(stub_other).getSerialVersionUID();
        } catch (ClassNotFoundException cnfe) {
            logAndExit("Check Stub '" + stubClassName + "' : WARNING - Stub not found in " +
                       rootDir_other.getAbsolutePath());
        }

        //compare
        if (suid_current == suid_other) {
            System.out.println("Check Stub '" + stubClassName + "' : OK");
        } else {
            System.out.println("Check Stub '" + stubClassName + "' : WARNING - Incompatible");
        }

    }

    private static ClassLoader getClassLoader(File rootDir) {
        File distlib = new File(rootDir.getAbsolutePath() + File.separator + "dist" + File.separator + "lib");
        if (!distlib.exists() || !distlib.isDirectory()) {
            logAndExit("ERROR : dist/lib directory does not exist in " + rootDir.getAbsolutePath());
        }
        File[] jars = distlib.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().toUpperCase().endsWith(".JAR");
            }
        });
        List<URL> urls = new ArrayList<URL>();
        for (File file : jars) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                System.err.println("/!\\ " + e.getMessage());
            }
        }
        return new URLClassLoader(urls.toArray(new URL[] {}));
    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("\t-class check the stub of this class");
        System.out.println("\t-rootDirV1 root directory of the current project");
        System.out.println("\t-rootDirV2 root directory of the other project to be checked with");
        System.out.println("");
    }

    private static void logAndExit(String msg) {
        System.err.println(msg);
        System.exit(0);
    }

    /**
     * Turn a file name into a class name if necessary. Remove the ending .class
     * and change all the '/' or '\\' into '.'
     * 
     * @param name a class name
     */
    public static String processClassName(String name) {
        String tmp = name.endsWith(".class") ? name.substring(0, name.length() - 6) : name;
        tmp = tmp.replaceAll("[/\\\\]", ".");
        return tmp;
    }

}
