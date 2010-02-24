/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ext.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.Utils;


/**
 * StubChecker check the stub of the class given as argument.
 * The generated stub must have the same generated serial version UID than the one
 * inside the second given arguments representing the root of an other project.
 * 
 * As a consequence, this class must be used between two maintenance version, detecting if stubs 
 * are compatible between each other.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class StubChecker extends ClassLoader {

    public long getSerialVersionUID(String className, byte[] b) {
        Class<?> c = defineClass(className, b, 0, b.length);
        return ObjectStreamClass.lookup(c).getSerialVersionUID();
    }

    public static void main(String[] args) throws Exception {
        String classArg = null;
        File rootDir = null;

        int index = 0;
        while (index < args.length) {
            if (args[index].equals("-class")) {
                classArg = args[index + 1];
                index += 2;
            } else if (args[index].equals("-rootDir")) {
                String rootStr = args[index + 1];
                rootDir = new File(rootStr);
                if (!rootDir.isDirectory()) {
                    logAndExit("ERROR : rootDir is not a valid directory !");
                }
                if (!rootDir.exists()) {
                    logAndExit("ERROR : rootDir does not exist !");
                }
                index += 2;
            } else {
                usage();
                System.exit(1);
            }
        }

        if (classArg == null || rootDir == null) {
            usage();
            System.exit(1);
        }

        //local class stub in current version
        String className = processClassName(classArg);
        String stubClassName = Utils.convertClassNameToStubClassName(className, null);
        try {
            /*byte[] data = JavassistByteCodeStubBuilder.create(className, null);
            long suidLocalStub = new StubChecker().getSerialVersionUID(stubClassName, data);*/
            long suidLocalStub = ObjectStreamClass.lookup(Class.forName(stubClassName)).getSerialVersionUID();

            //get other version stub
            byte[] data = null;
            try {
                data = lookIntoDirectory(stubClassName, new File(rootDir.getAbsolutePath() + File.separator +
                    "classes"));
            } catch (IOException Ex) {
            }
            if (data == null) {
                data = lookIntoDirectory(stubClassName, new File(rootDir.getAbsolutePath() + File.separator +
                    "dist" + File.separator + "lib"));
                if (data == null) {
                    logAndExit("Check Stub '" + stubClassName + "' : WARNING - Stub not found in " +
                        rootDir.getAbsolutePath());
                }
            }
            long suidComparedStub = new StubChecker().getSerialVersionUID(stubClassName, data);

            //compare
            if (suidLocalStub == suidComparedStub) {
                System.out.println("Check Stub '" + stubClassName + "' : OK");
            } else {
                throw new VerifyError();
            }
        } catch (VerifyError err) {
            System.out.println("Check Stub '" + stubClassName + "' : WARNING - Incompatible stub");
        }

    }

    private static void usage() {
        System.out.println("Usage:");
        System.out.println("\t-class check the stub of this class");
        System.out.println("\t-rootDir root directory of the other project to be checked with");
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

    /**
     * Look for a classfile into a directory.
     * 
     * @param classname the looked up class.
     * @param directory the directory to look into.
     * @return the byte[] representation of the class if found, null otherwise.
     * @throws IOException if the jar file cannot be read.
     */
    public static byte[] lookIntoDirectory(String classname, File directory) throws IOException {
        String pathToClass = convertNameToPath(classname, true);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    byte[] resInDir = lookIntoDirectory(classname, files[i]);
                    if (resInDir != null) {
                        return resInDir;
                    }
                } else if (isJarFile(files[i])) {
                    byte[] resInJar = lookIntoJarFile(classname, new JarFile(files[i]));
                    if (resInJar != null) {
                        return resInJar;
                    }
                } else if (isClassFile(files[i]) && files[i].getAbsolutePath().endsWith(pathToClass)) {
                    return convertFileToByteArray(files[i]);
                }
            }
            // not found
            return null;
        } else {
            throw new IOException("Directory " + directory.getAbsolutePath() + " does not exist");
        }
    }

    /**
     * Look for a class definition into a jar file.
     * @param classname the looked up class.
     * @param file the jar file.
     * @return the byte[] representation of the class if found, null otherwise.
     * @throws IOException if the jar file cannot be read.
     */
    public static byte[] lookIntoJarFile(String classname, JarFile file) throws IOException {
        byte result[] = null;
        String path = convertNameToPath(classname, false);
        ZipEntry entry = file.getEntry(path);
        if (entry != null) {
            final InputStream inStream = file.getInputStream(entry);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream(); // ByteArrayOutputStream.close() is noop
            final byte[] data = new byte[1024];
            int count;
            while ((count = inStream.read(data, 0, 1024)) > -1) {
                bos.write(data, 0, count);
            }
            result = bos.toByteArray();
            inStream.close();
            return result;
        } else {
            return null;
        }
    }

    /**
     * Convert classname parameter (qualified) into path to the class file
     * (with the .class suffix)
     */
    public static String convertNameToPath(String classname, boolean useSystemFileSeparator) {
        return classname.replace('.', useSystemFileSeparator ? File.separatorChar : '/') + ".class";
    }

    /**
     * Return true if f is a jar file.
     */
    private static boolean isJarFile(File f) {
        return f.isFile() && f.getName().endsWith(".jar");
    }

    /**
     * Return true if f is a class file.
     */
    private static boolean isClassFile(File f) {
        return f.isFile() && f.getName().endsWith(".class");
    }

    /** Read contents of a file and return it as a byte array
     * @param file the file to read
     * @return an array of bytes containing file's data.
     * @throws IOException
     */
    public static byte[] convertFileToByteArray(File file) throws IOException {
        InputStream in = null;
        in = new FileInputStream(file);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        long count = 0;
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        in.close();
        return output.toByteArray();
    }

}
