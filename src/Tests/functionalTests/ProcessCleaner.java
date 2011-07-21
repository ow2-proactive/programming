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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package functionalTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.utils.OperatingSystem;


/**
 * Kill all Java processes matching a pattern
 *
 *
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public class ProcessCleaner {
    final private Pattern pattern;

    /**
     *
     * @param regex
     */
    public ProcessCleaner(String regex) {
        this(Pattern.compile(regex));
    }

    /**
     *
     */
    public ProcessCleaner(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Get PID of all alive Java processes matching the pattern.
     *
     * @return
     *    PIDs
     * @throws IOException
     *    If PIDs cannot be retrieved
     */
    final public int[] getAliveProcesses() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(getJps().getAbsolutePath(), "-mlv");
        Process p = pb.start();

        ArrayList<String> pids = new ArrayList<String>(10);

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Reader r = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(r);

        String line = null;
        while ((line = br.readLine()) != null) {
            Matcher m = this.pattern.matcher(line);
            if (m.matches()) {
                String pid = line.substring(0, line.indexOf(" "));
                pids.add(pid);
            }
        }

        int[] ret = new int[pids.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = Integer.parseInt(pids.get(i));
        }
        return ret;
    }

    /**
     * Kill all alive Java processes matching the pattern.
     *
     * @throws IOException
     *    If some processes cannot be killed
     */
    final public void killAliveProcesses() throws IOException {
        ProcessKiller pk = ProcessKiller.get();

        int[] pids = this.getAliveProcesses();
        for (int pid : pids) {
            try {
                pk.kill(pid);
                System.err.println("Killed process " + pid);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Get the thread dump of all alive Java processes matching the pattern.
     *
     * @return
     *    All the thread dumps
     * @throws IOException
     *    If a thread dump cannot be retrieved
     */
    final public String[] getThreadDumps() throws IOException {
        int[] pids = this.getAliveProcesses();

        String[] dumps = new String[pids.length];
        for (int i = 0; i < dumps.length; i++) {
            dumps[i] = this.getThreadDump(pids[i]);
        }
        return dumps;
    }

    /**
     * Get the thread dump of a given Java processs.
     *
     * @param pid
     *    PID of the Java process
     * @return
     *    Its thread dumps
     * @throws IOException
     *    If the thread dump cannot be fetched
     */
    final public String getThreadDump(int pid) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(getJstack().getAbsolutePath(), Integer.toString(pid));
        Process p = pb.start();

        try {
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Reader r = new InputStreamReader(p.getInputStream());
        BufferedReader br = new BufferedReader(r);

        StringBuilder sb = new StringBuilder();
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            sb.append(line);
        }

        return sb.toString();
    }

    /**
     * Returns the command to start the jps util
     *
     * @return command to start jps
     */
    static private File getJps() {
        final String jpsName;
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                jpsName = "jps";
                break;
            case windows:
                jpsName = "jps.exe";
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        return new File(getJavaBinDir(), jpsName);
    }

    /**
     * Returns the command to start the jstack util
     *
     * @return command to start jstack
     */
    static private File getJstack() {
        final String jstackName;
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                jstackName = "jps";
                break;
            case windows:
                jstackName = "jps.exe";
                break;
            default:
                throw new IllegalStateException("Unsupported operating system");
        }

        return new File(getJavaBinDir(), jstackName);
    }

    /**
     * Returns the Java bin dir
     *
     * @return Java bin/ dir
     */
    static private File getJavaBinDir() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("java.home"));
        sb.append(File.separatorChar);
        sb.append("..");
        sb.append(File.separatorChar);
        sb.append("bin");
        sb.append(File.separatorChar);
        sb.toString();

        return new File(sb.toString());
    }
}
