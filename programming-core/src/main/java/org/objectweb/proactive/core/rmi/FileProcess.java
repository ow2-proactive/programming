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
package org.objectweb.proactive.core.rmi;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * @author The ProActive Team
 *
 */
public class FileProcess {
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.CLASSLOADING);

    private java.io.File[] codebases;

    protected String className;

    public FileProcess(String paths, String className) {
        if (paths != null) {
            codebases = findClasspathRoots(paths);
        }
        this.className = className;
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     *
     * @return the bytecodes for the class
     * @exception ClassNotFoundException if the class corresponding
     * to <b>path</b> could not be loaded.
     */
    public byte[] getBytes() throws ClassNotFoundException {
        byte[] b = null;
        if (codebases == null) {
            try {
                // reading from resources in the classpath
                b = getBytesFromResource(className);
            } catch (IOException e) {
                throw new ClassNotFoundException("Cannot find class " + className, e);
            }
        } else {
            for (int i = 0; i < codebases.length; i++) {
                try {
                    if (codebases[i].isDirectory()) {
                        b = getBytesFromDirectory(className, codebases[i]);
                    } else {
                        b = getBytesFromArchive(className, codebases[i]);
                    }
                } catch (java.io.IOException e) {
                }
            }
        }
        if (b != null) {
            return b;
        }

        // try to get the class as a generated stub
        // generate it if necessary
        b = org.objectweb.proactive.core.mop.MOPClassLoader.getMOPClassLoader().getClassData(className);
        if (b != null) {
            return b;
        }

        //if (info.path != null) {
        //    System.out.println("ClassServer sent class " + info.path +
        //        " successfully");
        //}
        throw new ClassNotFoundException("Cannot find class " + className);
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     * @param path the fqn of the class
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    public static byte[] getBytesFromResource(String path) throws java.io.IOException {
        String filename = path.replace('.', '/') + ".class";
        java.io.InputStream in = FileProcess.class.getClassLoader().getResourceAsStream(filename);
        if (in == null) {
            return null;
        }
        int length = in.available();

        //if (logger.isDebugEnabled()) {
        //      //logger.debug("ClassFileServer reading: " + filename+"  length="+length+" from classpath");
        //}
        if (length == -1) {
            throw new java.io.IOException("File length is unknown: " + filename);
        } else {
            return getBytesFromInputStream(in, length);
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     * @param path the fqn of the class
     * @param codeBase the File that must be a jar or zip archive that may contain the class
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private byte[] getBytesFromArchive(String path, java.io.File archive) throws java.io.IOException {
        String filename = path.replace('.', '/') + ".class";
        java.util.zip.ZipFile jarFile = new java.util.zip.ZipFile(archive);
        java.util.zip.ZipEntry zipEntry = jarFile.getEntry(filename);
        if (zipEntry == null) {
            return null;
        }
        int length = (int) (zipEntry.getSize());

        //if (logger.isDebugEnabled()) {
        //      //logger.debug("ClassFileServer reading: " + filename+"  length="+length+" from jar/xip file "+archive.getAbsolutePath());
        //}
        if (length == -1) {
            throw new java.io.IOException("File length is unknown: " + filename);
        } else {
            return getBytesFromInputStream(jarFile.getInputStream(zipEntry), length);
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the argument <b>path</b>.
     * The <b>path</b> is a dot separated class name with
     * the ".class" extension removed.
     * @param path the fqn of the class
     * @param codeBase the File that must be a directory that may contain the class
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private byte[] getBytesFromDirectory(String path, java.io.File directory) throws java.io.IOException {
        java.io.File f = new java.io.File(directory, path.replace('.', java.io.File.separatorChar) + ".class");
        if (!f.exists()) {
            return null;
        }
        int length = (int) (f.length());

        //if (logger.isDebugEnabled()) {
        //      //logger.debug("ClassFileServer reading: " + f.getAbsolutePath()+"  length="+length);
        //}
        if (length == 0) {
            throw new java.io.IOException("File length is zero: " + path);
        } else {
            return getBytesFromInputStream(new java.io.FileInputStream(f), length);
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in, int length) throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[length];
        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }
        return bytecodes;
    }

    private java.io.File[] findClasspathRoots(String classpath) {
        String pathSeparator = File.pathSeparator;
        java.util.StringTokenizer st = new java.util.StringTokenizer(classpath, pathSeparator);
        int n = st.countTokens();
        java.io.File[] roots = new java.io.File[n];
        for (int i = 0; i < n; i++) {
            roots[i] = new java.io.File(st.nextToken());
        }
        return roots;
    }
}
