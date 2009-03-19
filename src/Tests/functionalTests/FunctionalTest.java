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
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extra.messagerouting.remoteobject.MessageRoutingRemoteObjectFactory;
import org.objectweb.proactive.extra.messagerouting.router.Router;
import org.objectweb.proactive.extra.messagerouting.router.RouterConfig;


@Ignore
public class FunctionalTest {
    static final protected Logger logger = Logger.getLogger("testsuite");

    static final public String VAR_JVM_PARAMETERS = "JVM_PARAMETERS";
    static private Router router;

    @BeforeClass
    static public void configureMessageRouting() {
        try {
            // Configure the Message routing
            if (MessageRoutingRemoteObjectFactory.PROTOCOL_ID.equals(PAProperties.PA_COMMUNICATION_PROTOCOL
                    .getValue())) {
                RouterConfig config = new RouterConfig();

                if (!PAProperties.PA_NET_ROUTER_PORT.isSet() ||
                    PAProperties.PA_NET_ROUTER_PORT.getValueAsInt() == 0) {
                    router = Router.createAndStart(config);
                    PAProperties.PA_NET_ROUTER_PORT.setValue(router.getPort());
                } else {
                    config.setPort(PAProperties.PA_NET_ROUTER_PORT.getValueAsInt());
                    router = Router.createAndStart(config);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    static public void terminateMessageRouting() {
        if (router != null) {
            router.stop();
        }
    }

    static public String getJvmParameters() {
        StringBuilder jvmParameters = new StringBuilder(" ");

        jvmParameters.append(PAProperties.PA_TEST.getCmdLine());
        jvmParameters.append("true ");

        // Jetty: avoid to use SecureRandom for session tracking
        jvmParameters.append(PAProperties.PA_HTTP_JETTY_XML.getCmdLine());
        jvmParameters.append(PAProperties.PA_HTTP_JETTY_XML.getValue());

        jvmParameters.append(" -Dproactive.test=true ");

        jvmParameters.append(PAProperties.PA_COMMUNICATION_PROTOCOL.getCmdLine());
        jvmParameters.append(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue());
        jvmParameters.append(" ");

        if (MessageRoutingRemoteObjectFactory.PROTOCOL_ID.equals(PAProperties.PA_COMMUNICATION_PROTOCOL
                .getValue())) {
            jvmParameters.append(PAProperties.PA_NET_ROUTER_ADDRESS.getCmdLine());
            jvmParameters.append(PAProperties.PA_NET_ROUTER_ADDRESS.getValue());
            jvmParameters.append(" ");

            jvmParameters.append(PAProperties.PA_NET_ROUTER_PORT.getCmdLine());
            jvmParameters.append(PAProperties.PA_NET_ROUTER_PORT.getValue());
            jvmParameters.append(" ");
        }

        return jvmParameters.toString();
    }

    /** The amount of time given to a test to success or fail */
    static final private long TIMEOUT = 300000;

    /** A shutdown hook to kill all forked JVMs when exiting the main JVM */
    static final private Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            logger.trace("FunctionalTest Shutdown Hook");
            killProActive();
        }
    };

    /** The default variable contract to pass to PA(GCM)Deployment.load 
     * 
     * This variable contract MUST ALWAYS be used. Otherwise tests will fail 
     * when some protocol are enabled (message routing for example) 
     */
    public VariableContractImpl vContract;

    public FunctionalTest() {
        vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(FunctionalTest.VAR_JVM_PARAMETERS, getJvmParameters(),
                VariableContractType.ProgramVariable);
    }

    @BeforeClass
    public static void beforeClass() {
        logger.info(FunctionalTest.class.getName() + " @BeforeClass: beforeClass");
        // Set PA_TEST to automatically flag child processes
        // When PA_TEST is set GCM Deployment framework will add it to 
        // started JVM. 
        PAProperties.PA_TEST.setValue(true);

        logger.trace("beforeClass");

        logger.trace("Killing remaining ProActive Runtime");
        killProActive();

        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IllegalArgumentException e) {
            logger.trace("shutdownHook already registered");
        }

        long timeout = TIMEOUT;
        String ptt = System.getProperty("proactive.test.timeout");
        if (ptt != null) {
            timeout = Long.parseLong(ptt);
        }

        if (timeout != 0) {
            logger.trace("Timer will be fired in " + timeout + " ms");
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    // 1- Advertise the failure
                    logger.warn("FunctionalTest timeout reached !");

                    // 2- Display jstack output
                    // It can be useful to debug (live|dead)lock
                    Map<String, String> pids = getPids();

                    for (String pid : pids.keySet()) {
                        System.err.println("PID: " + pid + " Command: " + pids.get(pid));
                        System.err.println();

                        try {
                            Process p = Runtime.getRuntime().exec(getJSTACKCommand() + " " + pid);
                            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

                            for (String line = br.readLine(); line != null; line = br.readLine()) {
                                System.err.println("\t" + line);
                            }

                            System.err.println();
                            System.err
                                    .println("---------------------------------------------------------------");
                            System.err.println();
                            System.err.println();
                            System.err.flush();
                        } catch (IOException e) {
                            // Should not happen
                            e.printStackTrace();
                        }
                    }

                    // 3- That's all
                    // Shutdown hook will be triggered to kill all remaining ProActive Runtimes
                    System.exit(0);
                }
            };

            Timer timer = new Timer(true);
            timer.schedule(timerTask, timeout);
        } else {
            logger.trace("Timeout disabled");
        }
    }

    @AfterClass
    public static void afterClass() {
        logger.info(FunctionalTest.class.getName() + " @AfterClass: afterClass");

        logger.trace("Removing the shutdownHook");
        Runtime.getRuntime().removeShutdownHook(shutdownHook);

        if (router != null) {
            //     	       router.stop();
        }

        logger.trace("Killing remaining ProActive Runtime");
        killProActive();
    }

    static private void killProActiveWithJPS() throws IOException {
        String javaHome = System.getProperty("java.home");
        File jpsBin = null;
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                jpsBin = new File(javaHome + File.separator + ".." + File.separator + "bin" + File.separator +
                    "jps");
                break;
            case windows:
                jpsBin = new File(javaHome + File.separator + ".." + File.separator + "bin" + File.separator +
                    "jps.exe");
                break;
        }
        if (!jpsBin.exists()) {
            throw new FileNotFoundException("JPS not found: " + jpsBin.toString());
        }

        Process jps;
        jps = Runtime.getRuntime().exec(new String[] { jpsBin.toString(), "-mlv" }, null, null);
        Reader reader = new InputStreamReader(jps.getInputStream());
        BufferedReader bReader = new BufferedReader(reader);

        String line = bReader.readLine();
        while (line != null) {
            if (line.matches(".*proactive.test=true.*")) {
                logger.debug("MATCH " + line);

                String pid = line.substring(0, line.indexOf(" "));
                Process kill = null;
                switch (OperatingSystem.getOperatingSystem()) {
                    case unix:
                        kill = Runtime.getRuntime().exec(new String[] { "kill", "-9", pid });
                        break;
                    case windows:
                        kill = Runtime.getRuntime().exec(new String[] { "taskkill", "/PID", pid });
                        break;
                    default:
                        logger.info("Unsupported operating system");
                        break;
                }

                try {
                    kill.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                logger.debug("NO MATCH " + line);
            }
            line = bReader.readLine();
        }
    }

    static private void killProActiveWithScript() throws Exception {
        File dir = new File(PAProperties.PA_HOME.getValue());
        File command = null;
        switch (OperatingSystem.getOperatingSystem()) {
            case unix:
                command = new File(dir + File.separator + "dev" + File.separator + "scripts" +
                    File.separator + "killTests");
                break;
            default:
                break;
        }

        if (command != null) {
            if (command.exists()) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[] { command.getAbsolutePath() }, null,
                            dir);
                    p.waitFor();
                } catch (Exception e) {
                    logger.warn(e);
                }
            } else {
                throw new IOException(command + " does not exist");
            }
        } else {
            throw new Exception("No kill script defined for" +
                OperatingSystem.getOperatingSystem().toString());
        }
    }

    /**
     * Kill all ProActive runtimes
     */
    public static void killProActive() {
        try {
            killProActiveWithJPS();
        } catch (Exception jpsException) {
            logger.warn("JPS kill failed: " + jpsException.getMessage());
            try {
                killProActiveWithScript();
            } catch (Exception scriptException) {
                logger.warn("Script kill failed: ", scriptException);
            }
        }
    }

    /**
     * Returns a map<PID, CommandLine> of ProActive JVMs
     * 
     * @return
     */
    static public Map<String, String> getPids() {
        HashMap<String, String> pids = new HashMap<String, String>();

        try {
            // Run JPS to list all JVMs on this machine
            Process p = Runtime.getRuntime().exec(getJPSCommand() + " -ml");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                // Discard all non ProActive JVM
                if (line.contains("org.objectweb.proactive")) {
                    String[] fields = line.split(" ", 2);
                    pids.put(fields[0], fields[1]);
                }
            }
        } catch (IOException e) {
            // Should not happen
            e.printStackTrace();
        }
        return pids;
    }

    /**
     * Returns the command to start the jps util
     * 
     * @return command to start jps
     */
    static public String getJPSCommand() {
        return getJavaBinDir() + "jps";
    }

    /**
     * Returns the command to start the jstack util
     * 
     * @return command to start jstack
     */
    static public String getJSTACKCommand() {
        return getJavaBinDir() + "jstack";
    }

    /**
     * Returns the Java bin dir
     * 
     * @return Java bin/ dir
     */
    static public String getJavaBinDir() {
        StringBuilder sb = new StringBuilder();
        sb.append(System.getProperty("java.home"));
        sb.append(File.separatorChar);
        sb.append("..");
        sb.append(File.separatorChar);
        sb.append("bin");
        sb.append(File.separatorChar);
        return sb.toString();
    }
}
