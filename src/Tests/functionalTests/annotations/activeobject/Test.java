package functionalTests.annotations.activeobject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

import junit.framework.Assert;

import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.extra.annotation.jsr269.ProActiveProcessor;

import functionalTests.FunctionalTest;

/*
 * Tests for the MigrationSignal and ActiveObject annotations
 * proactive_home could be set; if not set, the code tries to guess
 */
public class Test extends FunctionalTest{

	public static final String PROACTIVE_HOME;
	public static final String INPUT_FILES_PATH;
	public static final String PROC_PATH;
	public static final String TEST_FILES_RELPATH = "/src/Tests/functionalTests/annotations/activeobject/inputs/";
	public static final String TEST_FILES_PACKAGE = "functionalTests.annotations.activeobject.inputs.";
	public static final String TEST_TO_PASS = "accept";
	public static final String TEST_TO_FAIL = "reject";

	static {
		if(PAProperties.PA_HOME.isSet()){
			PROACTIVE_HOME = PAProperties.PA_HOME.getValue();
		}
		else {
			// guess the value
			String location = Test.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			PROACTIVE_HOME = getPAHomeFromClassPath(location);
		}

		INPUT_FILES_PATH = PROACTIVE_HOME + TEST_FILES_RELPATH;
		PROC_PATH = buildAnnotationProcessorPath(PROACTIVE_HOME);

	}

	private static final String getPAHomeFromClassPath(String location) {
		int pos = location.lastIndexOf(File.separator);
		String sb = location.substring(0, pos);
		pos = sb.lastIndexOf(File.separator);
		sb = sb.substring(0, pos);
		pos = sb.lastIndexOf(File.separator);
		sb = sb.substring(0, pos);

		return sb;
	}

	public static final String buildAnnotationProcessorPath(String proactive_home) {

		String proactive_classes = proactive_home + "/classes/";
		StringBuilder buildProcPath = new StringBuilder();
		String[] pathDirs = new String[] {
			"Core",
			"Extra",
			"Utils",
			"Extensions"
		};
		for (String pathDir : pathDirs) {
			buildProcPath.append( proactive_classes + pathDir + ":" );
		}

		return buildProcPath.toString();
	}

	private JavaCompiler _compiler;
	private NoClassOutputFileManager _fileManager;
	private DiagnosticCollector<JavaFileObject> _nonFatalErrors;

	@org.junit.Before
	public void initTest() {
		// get the compiler
		_compiler = ToolProvider.getSystemJavaCompiler();
		_nonFatalErrors = new DiagnosticCollector<JavaFileObject>();
		// get the file manager
		StandardJavaFileManager stdFileManager = _compiler.
			getStandardFileManager(_nonFatalErrors, null, null); // go for the defaults
		_fileManager = new NoClassOutputFileManager(stdFileManager);

	}
	@org.junit.Test
	public void action() throws Exception {

		// checking conditions that should be seen as errors
		Assert.assertEquals(checkFile("ErrorPrivate", TEST_TO_FAIL), 3);
		Assert.assertEquals(checkFile("ErrorNotInActiveObject", TEST_TO_FAIL), 1);
		Assert.assertEquals(checkFile("ErrorNotLast", TEST_TO_FAIL), 4);
		Assert.assertEquals(checkFile("ErrorNotLastBlock",TEST_TO_FAIL), 3);
		Assert.assertEquals(checkFile("ErrorNoMigrateTo", TEST_TO_FAIL), 1);
		Assert.assertEquals(checkFile("ErrorReturnsNull", TEST_TO_FAIL), 1);

		// checking conditions that should be ok
		Assert.assertTrue(checkFile("AcceptSimple", TEST_TO_PASS) == 0 );

	}

	// compile a single file
	// return number of compilation errors & warnings.
	// can be zero if compilation successful
	private int checkFile(String fileName , String expectedPrefix) {

		final String[] fileNames = new String[] {
			INPUT_FILES_PATH + expectedPrefix + "/" + fileName + ".java"
		};

		// get the compilation unit
		Iterable<? extends JavaFileObject> compilationUnits =
			_fileManager.getJavaFileObjects(fileNames);

		// setup diagnostic collector
		DiagnosticCollector<JavaFileObject> diagnosticListener =
			new DiagnosticCollector<JavaFileObject>();

		// compiler options
		// the arguments of the options come after the option
		String[] compilerOptions = {
			"-proc:only",
			"-processorpath",
			PROC_PATH,
			"-processor",
			ProActiveProcessor.class.getName()
		};

		String[] annotationsClassNames = {
				TEST_FILES_PACKAGE + expectedPrefix + "." + fileName
		};

		// create the compilation task
		CompilationTask compilationTask = _compiler.getTask( null, // where to write error messages
				_fileManager, // the file manager
				diagnosticListener, // where to receive the errors from compilation
				Arrays.asList(compilerOptions),  // the compiler options
				Arrays.asList(annotationsClassNames), // classes on which to perform annotation processing
				compilationUnits);

		// call the compilation task
		boolean compilationSuccesful = compilationTask.call();

		if(compilationSuccesful) {
			return 0;
		}
		else {
			return diagnosticListener.getDiagnostics().size();
		}

	}

	@org.junit.After
	public void endTest() throws Exception {

		// close the file manager
		_fileManager.close();

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

		public Iterable<? extends JavaFileObject> getJavaFileObjects(
				String... fileNames) {
			if ( !(_underlyingFileManager instanceof StandardJavaFileManager))
				return null;
			return ((StandardJavaFileManager)_underlyingFileManager).getJavaFileObjects(fileNames);
		}

		@Override
		public JavaFileObject getJavaFileForOutput(Location location,
				String className, Kind kind, FileObject sibling)
				throws IOException {

			if ( kind == JavaFileObject.Kind.CLASS && isClassLocation(location) ) {
				return _blackHoleFileObject;
			}

			return super.getJavaFileForOutput(location, className, kind, sibling);
		}

		private boolean isClassLocation(Location location) {

			if(!location.isOutputLocation())
				return false;

			if( location instanceof StandardLocation && ((StandardLocation)location) == StandardLocation.CLASS_OUTPUT )
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
