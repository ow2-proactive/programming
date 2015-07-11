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
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.objectweb.proactive.extensions.annotation.common.ProActiveProcessorCTree;
import org.junit.Ignore;


/**
 * Root class for tests for annotation implemented using JDK 1.6 - the Compiler Tree API
 * @author fabratu
 * @version %G%, %I%
 * @since ProActive 4.10
 */
@Ignore
public abstract class CTreeTest extends AnnotationTest {

    private JavaCompiler _compiler;
    private NoClassOutputFileManager _fileManager;
    private DiagnosticCollector<JavaFileObject> _nonFatalErrors;

    /* (non-Javadoc)
     * @see functionalTests.annotations.AnnotationTest#testInit()
     */
    @Override
    protected void testInit() throws NoCompilerDetectedException {
        // get the compiler
        _compiler = ToolProvider.getSystemJavaCompiler();
        if (_compiler == null) {
            logger.error("Cannot detect the system Java compiler. Check for your JDK settings(btw, you DO have a JDK installed, right?)");
            // this test can no longer continue...
            throw new NoCompilerDetectedException(
                "The annotations test will not be run, because a Java compiler was not detected.");
        }
        _nonFatalErrors = new DiagnosticCollector<JavaFileObject>();
        // get the file manager
        StandardJavaFileManager stdFileManager = _compiler
                .getStandardFileManager(_nonFatalErrors, null, null); // go for the defaults
        _fileManager = new NoClassOutputFileManager(stdFileManager);

    }

    @Override
    public void testCleanup() {
        // close the file manager
        try {
            _fileManager.close();
        } catch (IOException e) {
            // 
        }
    }

    /* (non-Javadoc)
     * @see functionalTests.annotations.AnnotationTest#checkFile(java.lang.String)
     */
    @Override
    protected Result checkFile(String fileName) throws CompilationExecutionException {
        final String[] fileNames = new String[] { INPUT_FILES_PATH + File.separator + fileName + ".java", };
        final String[] annotationsClassNames = { TEST_FILES_PACKAGE + fileName };
        return checkFilesAbsolutePath(fileNames, annotationsClassNames);
    }

    /* (non-Javadoc)
     * @see functionalTests.annotations.AnnotationTest#checkFile(java.lang.String)
     */
    @Override
    protected Result checkFiles(String... fileNames) throws CompilationExecutionException {
        String[] fileNamesAbs = new String[fileNames.length];
        String[] annotationClassNames = new String[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            fileNamesAbs[i] = INPUT_FILES_PATH + File.separator +
                fileNames[i].replace('.', File.separatorChar) + ".java";
            annotationClassNames[i] = TEST_FILES_PACKAGE + fileNames[i];
        }

        return checkFilesAbsolutePath(fileNamesAbs, annotationClassNames);
    }

    private Result checkFilesAbsolutePath(String[] fileNames, String[] annotationsClassNames) {
        // get the compilation units
        Iterable<? extends JavaFileObject> compilationUnits = _fileManager.getJavaFileObjects(fileNames);

        // setup diagnostic collector
        DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<JavaFileObject>();

        // compiler options
        // the arguments of the options come after the option
        String[] compilerOptions = { "-proc:only", "-processorpath", PROC_PATH, "-processor",
                ProActiveProcessorCTree.class.getName() };

        StringWriter output = new StringWriter();

        // create the compilation task
        CompilationTask compilationTask = _compiler.getTask(output, // where to write error messages
                _fileManager, // the file manager
                diagnosticListener, // where to receive the errors from compilation
                Arrays.asList(compilerOptions), // the compiler options
                Arrays.asList(annotationsClassNames), // classes on which to perform annotation processing
                compilationUnits);

        // call the compilation task
        compilationTask.call();

        /**
         *
         * All errors are accumulated in diagnosticListener
         * warning have to be found in the output separately
         * BTW errors cannot be found in the output because they are not marked explicitly as errors
         *
         */

        int errors = 0;
        int warnings = 0;
        for (Diagnostic<? extends JavaFileObject> diagnistic : diagnosticListener.getDiagnostics()) {
            if (diagnistic.getKind().equals(Diagnostic.Kind.ERROR)) {
                errors++;
            }
            if (diagnistic.getKind().equals(Diagnostic.Kind.WARNING) &&
                !diagnistic.getCode().equals("compiler.warn.proc.processor.incompatible.source.version")) {
                warnings++;
            }
        }

        return new Result(errors, warnings);
    }

    // a JavaFileManager used in order to suppress any .class file generation in the compilation phase
    final class NoClassOutputFileManager extends ForwardingJavaFileManager<JavaFileManager> {

        private final BlackHoleFileObject _blackHoleFileObject;
        private final JavaFileManager _underlyingFileManager;

        protected NoClassOutputFileManager(JavaFileManager fileManager) {
            super(fileManager);
            _underlyingFileManager = fileManager;
            _blackHoleFileObject = new BlackHoleFileObject();
        }

        public Iterable<? extends JavaFileObject> getJavaFileObjects(String... fileNames) {
            if (!(_underlyingFileManager instanceof StandardJavaFileManager))
                return null;
            return ((StandardJavaFileManager) _underlyingFileManager).getJavaFileObjects(fileNames);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind,
                FileObject sibling) throws IOException {

            if (kind == JavaFileObject.Kind.CLASS && isClassLocation(location)) {
                return _blackHoleFileObject;
            }

            return super.getJavaFileForOutput(location, className, kind, sibling);
        }

        private boolean isClassLocation(Location location) {

            if (!location.isOutputLocation())
                return false;

            if (location instanceof StandardLocation &&
                ((StandardLocation) location) == StandardLocation.CLASS_OUTPUT)
                return true;

            return false;
        }

        // a FileObject that discards all output it receives
        final class BlackHoleFileObject extends SimpleJavaFileObject {

            protected BlackHoleFileObject() {
                this(URI.create("blabla"), JavaFileObject.Kind.CLASS);
            }

            protected BlackHoleFileObject(URI uri, Kind kind) {
                super(uri, kind);
            }

            @Override
            public OutputStream openOutputStream() throws IOException {
                return new BlackHoleOutputStream();
            }

            // an OutputStream that discards all output it receives
            final class BlackHoleOutputStream extends OutputStream {

                @Override
                public void write(int b) throws IOException {
                    // black hole - do nothing!
                    return;
                }

            }

        }

    }

}
