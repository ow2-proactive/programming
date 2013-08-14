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
package functionalTests.annotations;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import functionalTests.FunctionalTest;


/**
 * Root class grouping common functionality for all annotation tests
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
@Ignore
public abstract class AnnotationTest extends FunctionalTest {

    // automatic environment configuration stuff
    protected String PROACTIVE_HOME;
    protected String PROC_PATH;

    @Before
    public void testSetup() throws NoCompilerDetectedException {
        this.envInit();
        this.inputFilesPathInit();
        this.testInit();
    }

    // test-specific cleanup code
    @After
    public abstract void testCleanup();

    protected void envInit() {
        if (CentralPAPropertyRepository.PA_HOME.isSet()) {
            PROACTIVE_HOME = CentralPAPropertyRepository.PA_HOME.getValue();
        } else {
            // guess the value
            String location = AnnotationTest.class.getProtectionDomain().getCodeSource().getLocation()
                    .getPath();
            PROACTIVE_HOME = getPAHomeFromClassPath(location);
        }

        PROC_PATH = buildAnnotationProcessorPath(PROACTIVE_HOME);

    }

    // TODO this method contains a hardcoded procedure, heavily dependent on the package structure. Change it!
    private final String getPAHomeFromClassPath(String location) {
        int pos = location.lastIndexOf(File.separator);
        String sb = location.substring(0, pos);
        pos = sb.lastIndexOf(File.separator);
        sb = sb.substring(0, pos);
        pos = sb.lastIndexOf(File.separator);
        sb = sb.substring(0, pos);
        pos = sb.lastIndexOf(File.separator);
        sb = sb.substring(0, pos);

        return sb;
    }

    private final String buildAnnotationProcessorPath(String proactive_home) {

        File proactive_classes = new File(proactive_home, "classes");
        StringBuilder buildProcPath = new StringBuilder();
        String[] pathDirs = new String[] { "Core", "Extra", "Utils", "Extensions" };
        for (String pathDir : pathDirs) {
            buildProcPath.append((new File(proactive_classes, pathDir)) + File.pathSeparator);
        }

        return buildProcPath.toString();
    }

    // to be initialized by the subclasses
    protected String INPUT_FILES_PATH;
    protected String TEST_FILES_PACKAGE;

    // "guesses" the path to the test files. this method assumes(does not check!) that 
    // the structure of the tests is the same as described 
    // <a href="http://confluence.activeeon.com/display/PROG/Feature+Compile+time+annotations">here</a>
    protected void inputFilesPathInit() {
        Class<? extends Object> testClass = this.getClass();
        TEST_FILES_PACKAGE = testClass.getPackage().getName() + ".inputs.";
        String testFilesRelpath = File.separator + "src" + File.separator + "Tests" + File.separator +
            TEST_FILES_PACKAGE.replace('.', File.separatorChar);

        // HACK set the test classes in the classpath
        String cp = System.getProperty("java.class.path");
        cp = cp + File.pathSeparator + PROACTIVE_HOME + File.separator + "classes" + File.separator + "Tests";
        System.setProperty("java.class.path", cp);

        INPUT_FILES_PATH = PROACTIVE_HOME + testFilesRelpath;
    }

    // initialization needed in order to perform the tests
    protected abstract void testInit() throws NoCompilerDetectedException;

    // how to execute a compilation process on a compilation unit
    protected abstract Result checkFile(String fileName) throws CompilationExecutionException;

    // how to execute a compilation process on multiple compilation units
    protected abstract Result checkFiles(String... fileNames) throws CompilationExecutionException;

    // the results of compilation execution
    public final class Result {
        public int errors;
        public int warnings;

        public Result() {
            errors = warnings = 0;
        }

        public Result(int e, int w) {
            errors = e;
            warnings = w;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Result))
                return false;
            Result rhs = (Result) obj;
            return errors == rhs.errors && warnings == rhs.warnings;
        }

        @Override
        public String toString() {
            return "errors:" + errors + ";warnings:" + warnings;
        }
    }

    protected final Result OK = new Result(0, 0);
    protected final Result WARNING = new Result(0, 1);
    protected final Result ERROR = new Result(1, 0);

    // the errors of compilation execution

    public final class CompilationExecutionException extends Exception {

        public CompilationExecutionException(String str) {
            super(str);
        }

        public CompilationExecutionException(String str, Throwable e) {
            super(str, e);
        }

    }

    // if I don't find a compiler...

    public class NoCompilerDetectedException extends Exception {

        public NoCompilerDetectedException(String message) {
            super(message);
        }

        public NoCompilerDetectedException(String message, Throwable e) {
            super(message, e);
        }

    }

}
