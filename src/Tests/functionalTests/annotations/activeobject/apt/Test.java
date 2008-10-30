package functionalTests.annotations.activeobject.apt;

import functionalTests.FunctionalTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.extra.annotation.activeobject.ActiveObjectAnnotationProcessorFactory;

public class Test extends FunctionalTest {

	// log
	static final protected Logger _logger = Logger.getLogger("testsuite");

	public static final String PROACTIVE_HOME;
	public static final String INPUT_FILES_PATH;
	public static final String PROC_PATH;
	public static final String TEST_FILES_RELPATH = "/src/Tests/functionalTests/annotations/activeobject/apt/inputs/";
	public static final String TEST_FILES_PACKAGE = "functionalTests.annotations.activeobject.apt.inputs.";

	static {
		if(PAProperties.PA_HOME.isSet()){
			PROACTIVE_HOME = PAProperties.PA_HOME.getValue();
		}
		else {
			// guess the value
			PROACTIVE_HOME = functionalTests.annotations.activeobject.Test.PROACTIVE_HOME;
		}

		INPUT_FILES_PATH = PROACTIVE_HOME + TEST_FILES_RELPATH;
		PROC_PATH = functionalTests.annotations.activeobject.Test.PROC_PATH;

	}

	private String[] _aptCommand;
	private String _classpath;

	@org.junit.Before
	public void init() {
		_aptCommand = new String[7];
		_aptCommand[0] = getAptCommand();
		_aptCommand[1] = "-factorypath";
		_aptCommand[2] = PROC_PATH;
		_aptCommand[3] = "-factory";
		_aptCommand[4] = ActiveObjectAnnotationProcessorFactory.class.getName();
		_aptCommand[5] = "-nocompile";

		_classpath = PROC_PATH;
	}


	@org.junit.Test
	public void test() {

		try {
			// basic checks
			Assert.assertEquals( checkFile("WarningGettersSetters"), WARNING);
			Assert.assertEquals( checkFile("ErrorFinalClass"), ERROR);
			Assert.assertEquals( checkFile("ErrorFinalMethods"), ERROR);
			Assert.assertEquals( checkFile("ErrorNoArgConstructor"), ERROR);
			Assert.assertEquals( checkFile("WarningNoSerializable"), WARNING);
			Assert.assertEquals( checkFile("ErrorSynchronizationPrimitives"), new Result(2,0));

			// more complicated scenarios
			Assert.assertEquals( checkFile("ErrorReturnTypes"), new Result(4,0));
			Assert.assertEquals( checkFile("Reject"), new Result(5,2));
			Assert.assertEquals( checkFile("CorrectedReject"), OK);
		} catch (IOException e) {
			_logger.error("Cannot execute the command " + compressCommand(_aptCommand), e );
		}

	}

	/*
	 *  run the APT command on a single file, and return
	 *  the number of compilation errors and warnings
	 */
	private Result checkFile(String fileName) throws IOException {

		_aptCommand[6] = INPUT_FILES_PATH + File.separator +  fileName + ".java";// the input file

		ProcessBuilder processBuilder = new ProcessBuilder( Arrays.asList(_aptCommand) );
		Map<String, String> env = processBuilder.environment();
		env.put("CLASSPATH", _classpath);

		Process aptProcess = processBuilder.start();

		BufferedReader stderr = new BufferedReader(
				new InputStreamReader(
						aptProcess.getErrorStream()));
		//flushOutput(stderr);

		return getResults(stderr);
	}

	private Result getResults(BufferedReader stderr) {

		Result ret = new Result();

		try {
			if(stderr.read() == -1 ){
				// apt finished succesfully
				return ret;
			}

			for (String line = stderr.readLine(); line != null; line = stderr.readLine()) {
				if(Pattern.matches("\\d* error(s?)", line)) {
					ret.errors = extractNumber(line);
				}
				if(Pattern.matches("\\d* warning(s?)", line)) {
					ret.warnings = extractNumber(line);
				}
			}

			return ret;
		} catch(IOException e){
			// return what we have up until now
			return ret;
		}

	}

	private int extractNumber(String line) {
		return Integer.parseInt( line.split(" ")[0] );
	}


	private String compressCommand(String[] command) {

		String ret = "";
		for (String token : command) {
			ret = ret + " " + token;
		}

		return ret;
	}

	public String getAptCommand() {
		return FunctionalTest.getJavaBinDir() + "apt";
	}

	private void flushOutput(BufferedReader br) {

		try {
			if(br.read()==-1){
				_logger.debug("No output\n");
				return;
			}
			_logger.debug("\n");

			for (String line = br.readLine(); line != null; line = br.readLine()) {
				_logger.debug("\t|" + line + "|");
			}

			_logger.debug("\n");
			_logger.debug("---------------------------------------------------------------\n");
			_logger.debug("\n");
		} catch(IOException e){
			// nothing!
		}
	}

	private final Result OK = new Result(0,0);
	private final Result WARNING = new Result(0,1);
	private final Result ERROR = new Result(1,0);

	final class Result{
		int errors;
		int warnings;
		public Result() {
			errors = warnings = 0;
		}

		public Result(int e,int w) {
			errors = e; warnings = w;
		}

		@Override
		public boolean equals(Object obj) {
			Result rhs = (Result)obj;
			return errors == rhs.errors && warnings == rhs.warnings;
		}

		@Override
		public String toString() {
			return "errors:" + errors + ";warnings:" + warnings;
		}
	}

}
