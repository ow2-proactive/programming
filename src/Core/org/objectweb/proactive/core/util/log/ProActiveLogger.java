/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package org.objectweb.proactive.core.util.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * @author The ProActive Team
 *
 *  This class stores all logger used in ProActive. It provides an easy way
 *  to create and to retrieve a logger.
 */
public class ProActiveLogger extends Logger {

    private static boolean loaded = false;

    // Callers must be synchronized to avoid race conditions
    private static void load() {
        String configurationFile = System.getProperty("log4j.configuration");
        String collectorURL = System.getProperty("proactive.log4j.collector");
        boolean success = false;
        // If log4j.configuration is set, log4j will use this file automatically

        if (configurationFile == null) {
            // We have to load load the log4j configuration by ourself
            Properties p = new Properties();

            if (collectorURL != null) {
                // Load the ProActive log collector config file
                InputStream in = PAProperties.class.getResourceAsStream("proactive-collector-log4j");
                try {
                    p.load(in);
                    success = true;
                } catch (IOException e1) {
                    System.err.println("Failed to read the proactive-collector-log4j file: " +
                        e1.getMessage());
                }
            } else {
                // Load the proactive-log4j in the user configuration directory if available
                File f = new File(Constants.USER_CONFIG_DIR + File.separator +
                    ProActiveConfiguration.PROACTIVE_LOG_PROPERTIES_FILE);

                if (f.exists()) {
                    try {
                        InputStream in = new FileInputStream(f);
                        p.load(in);

                        success = true;
                    } catch (Exception e) {
                        System.err.println("the user's log4j configuration file (" + f.getAbsolutePath() +
                            ") exits but is not accessible, fallbacking on the default configuration");
                    }
                }
            }

            if (!success) {
                // Load the default proactive-log4j file embedded in the ProActive.jar
                InputStream in = PAProperties.class.getResourceAsStream("proactive-log4j");
                try {
                    p.load(in);
                } catch (IOException e1) {
                    System.err.println("Failed to read the default configuration file:" + e1.getMessage());
                }
            }

            PropertyConfigurator.configure(p);
        }

        myFactory = new ProActiveLoggerFactory();
        loaded = true;
    }

    private static ProActiveLoggerFactory myFactory;

    /** Log an exception that "cannot occur"
     *
     * Impossible exceptions should never be eaten or "printStackTraced" but logged
     * using this method. Because nothing is impossible... 
     * 
     * @param t The nasty exception
     */
    static public void logImpossibleException(Logger logger, Throwable t) {
        logger.error("The following impossible exception occured", t);
    }

    /** Log an exception we don't want to handle
    *
    * @param t The nasty exception
    */
    static public void logEatedException(Logger logger, String message, Throwable t) {
        /* Usually we don't want to see theses exceptions. DEBUG is the right level to use */
        logger.debug(message, t);
    }

    /** Log an exception we don't want to handle
    *
    * 
    * @param t The nasty exception
    */
    static public void logEatedException(Logger logger, Throwable t) {
        logEatedException(logger, "The following exception occured but we don't care ", t);
    }

    /**
       Just calls the parent constructor.
     */
    protected ProActiveLogger(String name) {
        super(name);
    }

    /**
       This method overrides {@link Logger#getLogger} by supplying
       its own factory type as a parameter.
     */
    synchronized public static Logger getLogger(String name) {
        if (!loaded) {
            load();
        }

        return Logger.getLogger(name, myFactory);
    }

    /**
     * Get corresponding stack trace as string
     * @param e A Throwable
     * @return The output of printStackTrace is returned as a String
     */
    public static String getStackTraceAsString(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
