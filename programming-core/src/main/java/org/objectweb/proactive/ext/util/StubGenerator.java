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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import javassist.ClassClassPath;
import javassist.ClassPool;

import org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


@Deprecated
public class StubGenerator {
    public static void main(String[] args) throws InterruptedException {
        StubGenerator sg = new StubGenerator(args);
        sg.run();
    }

    private File srcDir;

    private String pkg = "";

    private File destDir;

    private String cl;

    private boolean verbose = false;

    public StubGenerator(String[] args) {
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(this.getClass()));

        int index = 0;
        while (index < args.length) {
            if (args[index].equals("-srcDir")) {
                srcDir = new File(args[index + 1]);
                index += 2;
            } else if (args[index].equals("-pkg")) {
                pkg = args[index + 1];
                index += 2;
            } else if (args[index].equals("-destDir")) {
                destDir = new File(args[index + 1]);
                index += 2;
            } else if (args[index].equals("-class")) {
                cl = args[index + 1];
                index += 2;
            } else if (args[index].equals("-verbose")) {
                verbose = true;
                index++;
            } else {
                usage();
                System.exit(1);
            }
        }
    }

    public void usage() {
        System.out.println("Usage:");
        System.out.println("\t-srcDir  directory where to find source classes");
        System.out.println("\t-destDir directory where to put generated stubs");
        System.out.println("\t-pkg     package name");
        System.out.println("\t-class   generate only a stub for this class");
        System.out.println("\t-verbose enable verbose mode");
        System.out.println("");
    }

    public void logAndExit(String str) {
        System.err.println(str);
        System.exit(2);
    }

    public void run() {
        if (srcDir == null) {
            logAndExit("srcDir attribute is not set");
        }
        if (!srcDir.exists()) {
            logAndExit("Invalid srcDir attribute: " + srcDir.toString() + " does not exist");
        }
        if (!srcDir.isDirectory()) {
            logAndExit("Invalid srcDir attribute: " + srcDir.toString() + " is not a directory");
        }

        if (pkg == null) {
            logAndExit("pkg attribute is not set");
        }
        File pkgDir = new File(srcDir.toString() + File.separator + pkg.replace('.', File.separatorChar));
        if (!pkgDir.exists()) {
            logAndExit("Invalid pkg attribute: " + pkgDir.toString() + " does not exist");
        }

        if (destDir == null) {
            destDir = srcDir;
        }
        if (!destDir.isDirectory()) {
            logAndExit("Invalid dest attribute: " + destDir.toString() + " is not a directory");
        }
        if (!destDir.isDirectory()) {
            logAndExit("Invalid src attribute: " + destDir.toString() + " is not a directory");
        }

        List<File> files = new ArrayList<File>();

        if (cl == null) {
            // Find all the classes in this package
            files.addAll(exploreDirectory(pkgDir));
        } else {
            File file = new File(pkgDir + File.separator + cl + ".class");
            if (!file.exists() || !file.isFile()) {
                logAndExit("Invalid pkg or class attribute: " + file.toString() + " does not exist");
            }

            files.add(file);
        }

        PrintStream stderr = System.err;
        PrintStream mute = new PrintStream(new MuteOutputStream());

        if (!verbose) {
            System.setErr(mute);
        }

        // ClassPool.releaseUnmodifiedClassFile = true;
        for (File file : files) {
            String str = file.toString().replaceFirst(Matcher.quoteReplacement(srcDir.toString()), "");

            if (!verbose) {
                System.setErr(mute);
            }

            boolean success = StubGenerator.generateClass(str, destDir.toString() + File.separator);
            if (success) {
                System.out.println("Generated stub: " +
                                   Utils.convertClassNameToStubClassName(processClassName(str), null));
            } else {
                System.out.println("Failed to generate stub: " +
                                   Utils.convertClassNameToStubClassName(processClassName(str), null));
                System.exit(1);
            }
        }

        if (!verbose) {
            System.setErr(stderr);
        }
    }

    private List<File> exploreDirectory(File dir) {
        List<File> files = new ArrayList<File>();

        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(exploreDirectory(file));
            }

            if (!file.toString().endsWith(".class")) {
                continue;
            }

            files.add(file);
        }

        return files;
    }

    /**
     * @param arg
     * @param directoryName
     */
    public static boolean generateClass(String arg, String directoryName) {
        String className = processClassName(arg);
        String fileName = null;

        String stubClassName = null;

        boolean success = false;
        try {
            // Generates the bytecode for the class
            byte[] data;

            data = JavassistByteCodeStubBuilder.create(className, null);
            stubClassName = Utils.convertClassNameToStubClassName(className, null);

            char sep = File.separatorChar;
            fileName = directoryName + stubClassName.replace('.', sep) + ".class";

            // And writes it to a file
            new File(fileName.substring(0, fileName.lastIndexOf(sep))).mkdirs();

            // String fileName = directoryName + System.getProperty
            // ("file.separator") +
            File f = new File(fileName);
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(data);
            fos.flush();
            fos.close();
            success = true;
        } catch (Throwable e) {
            System.err.println("Stub generation failed for class: " + className);
            e.printStackTrace();
        }

        return success;
    }

    /**
     * Turn a file name into a class name if necessary. Remove the ending .class
     * and change all the '/' into '.'
     * 
     * @param name
     */
    protected static String processClassName(String name) {
        int i = name.indexOf(".class");
        String tmp = name;
        if (i < 0) {
            return name;
        }
        tmp = name.substring(0, i);

        String tmp2 = tmp.replace(File.separatorChar, '.');

        if (tmp2.indexOf('.') == 0) {
            return tmp2.substring(1);
        }
        return tmp2;
    }

    public class MuteOutputStream extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            // Please shut up !
        }
    }
}
