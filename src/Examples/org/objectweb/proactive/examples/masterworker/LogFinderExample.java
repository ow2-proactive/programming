/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.masterworker;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.objectweb.proactive.extensions.masterworker.TaskException;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;
import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.examples.masterworker.util.Grep;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * This test class is an example on how to use the Master/Worker API to log a set of log files contained inside a directory
 * A first task will list the content of the directory, filter log files names, and sort the files by decreasing size.
 * Finally the grep tasks will copy the content of each file locally to each worker then execute the grep command on it.
 * This is not intended to be a real-life log analyzer as file copying is very expensive, but by this we simulate the idea of a replicated file system, which is mandatory for efficient log file analysis.
 * @author The ProActive Team
 *
 */
public class LogFinderExample extends AbstractExample {

    static ProActiveMaster<? extends Task, ArrayList> master;

    private static final String DEFAULT_LOG_PATH = "logs";
    private static final String DEFAULT_LOG_PATTERN = ".*\\.log";
    private static final String DEFAULT_UNIX_LOCAL_PATH = "/tmp/";
    private static final String DEFAULT_WINDOWS_LOCAL_PATH = "c:\\Temp";

    private static final String[] DEFAULT_PATTERNS = { "fviale", "JavaThread" };

    private static File logDirectory;
    private static String logPattern;
    private static File tmpDir;
    private static String[] patternsToFind;

    /**
     * @param args
     * @throws TaskException
     * @throws ProActiveException
     */
    public static void main(String[] args) throws Exception {
        //   Getting command line parameters and creating the master (see AbstractExample)
        init(args);

        if (master_vn_name == null) {
            master = new ProActiveMaster();
        } else {
            master = new ProActiveMaster(descriptor_url, master_vn_name);
        }

        registerShutdownHook(new Runnable() {
            public void run() {
                master.terminate(true);
            }

        });

        // Adding resources
        if (schedulerURL != null) {
            master.addResources(schedulerURL, login, password, classpath);
        } else if (vn_name == null) {
            master.addResources(descriptor_url);
        } else {
            master.addResources(descriptor_url, vn_name);
        }

        // Submitting the listing task
        List tasks = new ArrayList();
        tasks.add(new ListLogFiles(logDirectory, logPattern));
        master.solve(tasks);

        tasks.clear();

        ArrayList<File> listFiles = master.waitOneResult();
        for (File file : listFiles) {
            tasks.add(new GrepCountTask(file, tmpDir, patternsToFind));
        }
        master.solve(tasks);

        int[] totalCount = new int[patternsToFind.length];
        for (int i = 0; i < totalCount.length; i++) {
            totalCount[i] = 0;
        }

        // Collecting the results
        try {
            while (!master.isEmpty()) {
                ArrayList<Integer> answer = master.waitOneResult();
                for (int i = 0; i < totalCount.length; i++) {
                    totalCount[i] += answer.get(i);
                }

            }
        } catch (TaskException e) {
            // We catch user exceptions
            e.printStackTrace();
        }
        // wait for logs finition
        Thread.sleep(2000);
        for (int i = 0; i < totalCount.length; i++) {
            System.out.println("Found a total of " + totalCount[i] + " occurences of \"" + patternsToFind[i] +
                "\" in server log.");
        }
        PALifeCycle.exitSuccess();

    }

    /**
     * Initializing the example with command line arguments
     *
     * @param args command line arguments
     * @throws MalformedURLException
     */
    protected static void init(String[] args) throws Exception {

        command_options.addOption(OptionBuilder.withArgName("logDirectory").hasArg().withDescription(
                "directory where the log files are stored").create("logs"));
        command_options.addOption(OptionBuilder.withArgName("logPattern").hasArg().withDescription(
                "pattern for log files (e.g. *.log)").create("pattern"));
        command_options.addOption(OptionBuilder.withArgName("tmpDir").hasArg().withDescription(
                "local path where to store temporary files (e.g. /tmp/)").create("tmp"));

        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LogFinderExample", command_options);
        AbstractExample.init(args);

        String directory = cmd.getOptionValue("logs");
        if (directory == null) {
            logDirectory = new File(DEFAULT_LOG_PATH);
        } else {
            logDirectory = new File(directory);
        }

        if (!(logDirectory.exists() && logDirectory.canRead())) {
            throw new IllegalArgumentException(logDirectory + " does not exist or is not readable");
        }

        if (!(logDirectory.isDirectory())) {
            throw new IllegalArgumentException(logDirectory + " is not a directory");
        }

        String tmp = cmd.getOptionValue("tmp");
        if (tmp == null) {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                tmpDir = new File(DEFAULT_WINDOWS_LOCAL_PATH);
            } else {
                tmpDir = new File(DEFAULT_UNIX_LOCAL_PATH);
            }
        } else {
            tmpDir = new File(tmp);
        }

        if (!(tmpDir.exists() && tmpDir.canWrite())) {
            throw new IllegalArgumentException(tmpDir + " does not exist or is not writeable");
        }

        if (!(tmpDir.isDirectory())) {
            throw new IllegalArgumentException(tmpDir + " is not a directory");
        }

        String lpatt = cmd.getOptionValue("pattern");
        if (lpatt == null) {
            logPattern = DEFAULT_LOG_PATTERN;
        } else {
            logPattern = lpatt;
        }

        String[] patt = cmd.getArgs();
        if (patt.length == 0) {
            patternsToFind = DEFAULT_PATTERNS;
        } else {
            patternsToFind = patt;
        }
    }

    /**
     * A task executing a grep command on a file
     *
     * @author fviale
     */
    public static class GrepCountTask implements Task<ArrayList<Integer>> {

        private File srcFile;
        private File tmpFile;
        private File tmpDir;
        private String[] patterns;

        /**
         *  Creating the task with the given filename
         */
        public GrepCountTask(File file, File tmpDir, String[] patterns) {
            this.srcFile = file;
            this.tmpDir = tmpDir;
            this.patterns = patterns;

        }

        /**
         * Copies a file to the given destination file
         * @param srFile origin file
         * @param dtFile destination file
         * @throws IOException
         */
        private static void copyfile(File srFile, File dtFile) throws IOException {
            InputStream in = new FileInputStream(srFile);
            OutputStream out = new FileOutputStream(dtFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

        /**
         * Performs a grep on the given file using the given patterns
         * @param srFile file to grep
         * @param patterns text to find
         * @return number of occurences found for each pattern
         * @throws IOException
         */
        private static ArrayList<Integer> grepFile(File srFile, String[] patterns) throws IOException {

            ArrayList<Integer> answer = new ArrayList<Integer>(patterns.length);
            for (String pattern : patterns) {
                Grep.compile(pattern);
                answer.add(Grep.grep(srFile));

            }
            return answer;
        }

        /** {@inheritDoc} */
        public ArrayList<Integer> run(WorkerMemory memory) throws IOException, URISyntaxException {
            tmpFile = new File(tmpDir, srcFile.getName());
            if (!tmpFile.exists()) {
                copyfile(srcFile, tmpFile);
            }

            return grepFile(tmpFile, patterns);
        }
    }

    /**
     * Task which lists the log files in the specified folder 
     */
    public static class ListLogFiles implements Task<ArrayList<File>> {

        private File directory;
        private String pattern;

        /** Constructs a new ListLogFiles. */
        public ListLogFiles(File directory, String pattern) {
            this.directory = directory;
            this.pattern = pattern;
        }

        /** {@inheritDoc} */
        public ArrayList<File> run(WorkerMemory memory) throws IOException, URISyntaxException {
            ArrayList<File> all_files = findMatchingLogFiles();
            return decreasingSizeFileList(all_files);
        }

        /**
         * Finds file in the specified directory which match the specified log file pattern
         * @return
         */
        private ArrayList<File> findMatchingLogFiles() {
            ArrayList<File> answer = new ArrayList<File>();
            File[] files = directory.listFiles();
            for (File f : files) {
                if (f.getName().matches(pattern)) {
                    answer.add(f);
                }
            }
            return answer;
        }

        /**
         * Sorts the files in decending size order
         * @param input list of pathnames
         * @return sorted list of pathnames
         */
        private ArrayList<File> decreasingSizeFileList(ArrayList<File> input) {
            ArrayList<File> answer = new ArrayList<File>();

            SortedSet<File> files = new TreeSet<File>(new Comparator<File>() {

                public int compare(File o1, File o2) {
                    long diff = o1.length() - o2.length();
                    if (diff == 0)
                        return 0;
                    else if (diff > 0)
                        return 1;
                    else
                        return -1;

                }
            });
            for (File file : input) {
                files.add(file);
            }
            for (File file : files) {
                answer.add(file);
            }
            return answer;

        }

    }

}
