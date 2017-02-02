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
package org.objectweb.proactive.core.util.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Appender;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.objectweb.proactive.core.config.Constants;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * @author The ProActive Team
 *
 *  This class stores all logger used in ProActive. It provides an easy way
 *  to create and to retrieve a logger.
 *  
 *  @see Loggers
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
                InputStream in = PAProperties.class.getResourceAsStream("distributed-log4j");
                try {
                    p.load(in);
                    success = true;
                } catch (IOException e1) {
                    System.err.println("Failed to read the proactive-collector-log4j file: " + e1.getMessage());
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
                                           ") exists but is not accessible, fallbacking on the default configuration");
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
     * This method overrides {@link Logger#getLogger} by supplying
     * its own factory type as a parameter.
     */
    synchronized public static Logger getLogger(String name) {
        return getLogger(name, (String) null);
    }

    /**
     * Same as {@link #getLogger(String)} but ensure backward compatibility with an old logger name.
     * 
     * When a Logger is renamed, we don't want to force the user to update their configuration. If 
     * the user configured the old logger then we automatically apply this configuration to the new 
     * logger. If the new logger has already been loaded then no changes are applied to it and it is
     * returned. So this method should be called at early stage.
     * 
     * @param name The current name of the logger
     * @param oldName The old name of the logger
     * @return The logger
     */
    synchronized public static Logger getLogger(String name, String oldName) {
        if (!loaded) {
            load();
        }

        Logger logger = null;

        if (oldName == null) {
            logger = Logger.getLogger(name, myFactory);
        } else {
            // Ensure backward compatibility. 
            // If user configured log4j for an old name, applies it's configuration to the new name.
            // If the new logger already exists, it is not modified. 
            Logger oldLogger = LogManager.exists(oldName);
            if (oldLogger != null) {
                logger = LogManager.exists(name);
                if (logger == null) {
                    // Apply the oldLogger configuration to the new Logger
                    logger = Logger.getLogger(name, myFactory);
                    logger.setAdditivity(oldLogger.getAdditivity());
                    logger.setLevel(oldLogger.getLevel());

                    @SuppressWarnings("unchecked")
                    Enumeration<Appender> appenders = oldLogger.getAllAppenders();
                    while (appenders.hasMoreElements()) {
                        Appender appender = appenders.nextElement();
                        logger.addAppender(appender);
                    }
                }
            }
        }

        return logger;
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
