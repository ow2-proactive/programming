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
package ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Reference;


/**
 * An Ant task for generating active object stubs.
 *
 * If a stub generation fail, an exception is thrown and the build fails.
 *
 * @author ProActive team
 * @since  ProActive 4.4.0
 */
public class AntStubGenerator extends Java {
    private List<String> classNames = new LinkedList<String>();

    private String srcDir;

    private String dstDir;

    public AntStubGenerator() {
        this.setFork(true);
        this.setFailonerror(true);
        this.setClassname(Main.class.getName());
    }

    public void setDstDir(String dir) {
        this.dstDir = dir;
    }

    public void setSrcDir(String dir) {
        this.srcDir = dir;
    }

    public void setRefclasspath(Reference ref) {
        this.setClasspathRef(ref);
    }

    public void addConfiguredClass(AntStubGeneratorClass clazz) {
        this.classNames.add(clazz.name);
    }

    @Override
    public void execute() throws BuildException {
        if (this.dstDir == null) {
            this.dstDir = this.srcDir;
        }

        // Generate the arguments
        StringBuilder sb = new StringBuilder();
        sb.append(protectWithQuote(this.srcDir));
        sb.append(" ");
        sb.append(protectWithQuote(this.dstDir));
        sb.append(" ");
        for (String className : classNames) {
            sb.append(className);
            sb.append(" ");
        }
        this.setArgs(sb.toString());
        super.execute();
    }

    static private String protectWithQuote(String str) {
        return "\"" + str + "\"";
    }

    /**
     * Generate active object stub
     *
     * @author ProActive team
     * @since  ProActive 4.4.0
     */
    static public class Main {
        private File srcDir;

        private File dstDir;

        private List<String> classNames = new LinkedList<String>();

        // Called by the java ant task
        public static void main(String[] args) {
            new Main(args);
        }

        public Main(String[] args) {
            this.srcDir = new File(args[0]);
            this.dstDir = new File(args[1]);

            for (int i = 2; i < args.length; i++) {
                this.classNames.add(args[i]);
            }

            checkConfig();
            quiet();
            for (String className : classNames) {
                this.generateClass(className);
            }
        }

        private void quiet() {
            // Do not use standard ProActive logger classes. Utils must not depends on the core
            Logger logger = Logger.getLogger("proactive.configuration");
            //            logger.setLevel(Level.ERROR);
        }

        private void checkConfig() throws BuildException {
            if (this.srcDir == null) {
                throw new BuildException("srcDir attribute is not set");
            }

            if (!this.srcDir.exists()) {
                throw new BuildException("Invalid srcDir attribute: " + srcDir.toString() + " does not exist");
            }

            if (!this.srcDir.isDirectory()) {
                throw new BuildException("Invalid srcDir attribute: " + srcDir.toString() + " is not a directory");
            }

            if (!this.dstDir.isDirectory()) {
                throw new BuildException("Invalid dest attribute: " + dstDir.toString() + " is not a directory");
            }
            if (!this.dstDir.isDirectory()) {
                throw new BuildException("Invalid src attribute: " + dstDir.toString() + " is not a directory");
            }
        }

        public void generateClass(String className) throws BuildException {
            String stubClassName = null;

            try {
                // Generates the bytecode for the class
                byte[] data;

                data = this.createStub(className);
                stubClassName = this.getStubClassName(className);

                // Write the bytecode into a File
                char sep = File.separatorChar;
                String fileName = new File(this.dstDir, stubClassName.replace('.', sep) + ".class").toString();
                try {
                    // Create directory is needed
                    new File(fileName.substring(0, fileName.lastIndexOf(sep))).mkdirs();
                    // dump the bytecode into the file
                    File f = new File(fileName);
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(data);
                    fos.flush();
                    fos.close();
                    System.out.println("Wrote " + fileName);
                } catch (IOException e) {
                    throw new BuildException("Failed to write stub for " + className + " in " + fileName, e);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                throw new BuildException("Stub generation failed for class: " + className, e);
            }

        }

        private byte[] createStub(String className) throws Exception {
            // Do not import the class since Utils must not depends on the core
            Class<?> cl = Class.forName("org.objectweb.proactive.core.mop.JavassistByteCodeStubBuilder");
            Method m = cl.getMethod("create", String.class, Class[].class);
            return (byte[]) (m.invoke(null, className, null));
        }

        private String getStubClassName(String className) throws Exception {
            // Do not import the class sinceUtils must not depeonds on the core
            Class<?> cl = Class.forName("org.objectweb.proactive.core.mop.Utils");
            Method m = cl.getMethod("convertClassNameToStubClassName", String.class, Class[].class);
            return (String) (m.invoke(null, className, null));
        }
    }
}
