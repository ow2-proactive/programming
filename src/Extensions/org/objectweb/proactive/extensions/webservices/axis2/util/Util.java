/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.extensions.webservices.axis2.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


/**
 * This class provides methods to extract files or directory from a jar archive
 * Used in particular by Jetty to get the axi2 configuration file and the axis2 repository
 *
 * @author The proActive Team
 */
public class Util {

    public static Logger logger = ProActiveLogger.getLogger(Loggers.WEB_SERVICES);

    private static String simpleName(ZipEntry entry) {
        String entryName = entry.getName();
        if (entryName.lastIndexOf('/') != -1) {
            if (!entryName.endsWith("/")) {
                entryName = entryName.substring(entryName.lastIndexOf('/') + 1);
            } else {
                entryName = entryName.substring(0, entryName.length() - 1);
                if (entryName.lastIndexOf('/') != -1) {
                    entryName = entryName.substring(entryName.lastIndexOf('/') + 1);
                }
            }
        }
        return entryName;
    }

    /**
     * Extracts a file from a jar archive to the directory located at destPath
     *
     * @param jarPath path to the jar file
     * @param entryPath relative path of the file into the jar archive
     * @param destPath path of the directory where file will be extracted
     * @param insertRandom if true, then inserts a random number before the file name.
     * @return the absolute path the extracted file
     * @throws WebServicesException
     */
    public static String extractFileFromJar(String jarPath, String entryPath, String destPath,
            boolean insertRandom) throws WebServicesException {
        try {
            JarFile jar = new JarFile(jarPath);
            ZipEntry entry = jar.getEntry(entryPath);

            if (entry.isDirectory()) {
                logger.error("Entry is a directory");
                return null;
            }

            String entryName = Util.simpleName(entry);

            InputStream in = jar.getInputStream(entry);

            String createdFile;
            if (insertRandom) {
                createdFile = destPath + "/" + Math.random() + "-" + entryName;
            } else {
                createdFile = destPath + "/" + entryName;
            }
            FileOutputStream returnedFile = new FileOutputStream(createdFile);
            OutputStream out = new BufferedOutputStream(returnedFile);
            byte[] buffer = new byte[1024];

            int nBytes;
            while ((nBytes = in.read(buffer)) > 0) {
                out.write(buffer, 0, nBytes);
            }
            out.flush();
            out.close();
            in.close();

            logger.debug("Extracted file " + entryName + " to " + destPath);
            return createdFile;
        } catch (Exception e) {
            throw new WebServicesException("An exception occured when trying to extract " + entryPath +
                " from " + jarPath, e);
        }
    }

    /**
     * Extracts a file or a directory from a jar archive to the directory located at destPath
     *
     * @param jarPath path to the jar file
     * @param entryPath relative path of the file or the directory into the jar archive
     * @param destPath path of the directory where file/directory will be extracted
     * @param insertRandom if true, then inserts a random number before the file/directory name.
     * @return the absolute path the extracted file
     * @throws WebServicesException
     */
    public static String extractFromJar(String jarPath, String entryPath, String destPath,
            boolean insertRandom) throws WebServicesException {

        try {
            JarFile jar = new JarFile(jarPath);
            ZipEntry entry = jar.getEntry(entryPath);

            if (!entry.isDirectory()) {
                return Util.extractFileFromJar(jarPath, entryPath, destPath, insertRandom);
            }

            String entrySimpleName = Util.simpleName(entry);
            String entryName = entry.getName();

            String createdDir = destPath;
            if (insertRandom) {
                createdDir += "/" + Math.random() + "-" + entrySimpleName;
            } else {
                createdDir += "/" + entrySimpleName;
            }
            new File(createdDir).mkdir();

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jEntry = entries.nextElement();
                String name = jEntry.getName();
                int index = name.indexOf(entryName);
                if (index != -1) {
                    String simpleName = name.substring(index + entryName.length());
                    if (!simpleName.endsWith("/") && simpleName.length() != 0) {
                        int slashIndex = simpleName.lastIndexOf('/');
                        String filePath = createdDir;
                        if (slashIndex != -1) {
                            filePath += "/" + simpleName.substring(0, simpleName.lastIndexOf('/'));
                            new File(filePath).mkdirs();
                            logger.debug("Created the directory: " + filePath);
                        }
                        Util.extractFileFromJar(jarPath, name, filePath, false);
                    }
                }
            }

            return createdDir;
        } catch (IOException e) {
            throw new WebServicesException("An IOException occured when trying to extract " + entryPath +
                " from " + jarPath, e);
        }
    }
}
