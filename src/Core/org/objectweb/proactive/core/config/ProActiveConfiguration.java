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
package org.objectweb.proactive.core.config;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.xml.ProActiveConfigurationParser;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * One and only one ProActiveConfiguration object is associated to each ProActive Runtime. It
 * contains the value of all the properties known by ProActive.
 *
 * Configuration parameters may be overriden according to the following priorities:</br>
 * <ol>
 * <li>System Java Properties</li>
 * <li>Custom configuration file</li>
 * <li>Default configuration file</li>
 * </ol>
 *
 */
public class ProActiveConfiguration {
    /** The ProActive Configuration associated to this ProActive Runtime */
    protected static ProActiveConfiguration singleton;

    /** All the known properties */
    protected final CustomProperties properties;

    /**
     * Default configuration file
     *
     * Must be in the same package than this class
     */
    protected static final String PROACTIVE_CONFIG_FILENAME = "ProActiveConfiguration.xml";

    /**
     * Default log4j configuration file
     *
     * Must be in the same package than this class
     */
    public static final String PROACTIVE_LOG_PROPERTIES_FILE = "ProActiveLoggers.properties";

    protected static final String FILE_PROTOCOL_PREFIX = "file:";

    /** User configuration directory */
    protected static final String PROACTIVE_USER_CONFIG_FILENAME = FILE_PROTOCOL_PREFIX +
        Constants.USER_CONFIG_DIR + File.separator + PROACTIVE_CONFIG_FILENAME;

    protected final Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);

    synchronized static public ProActiveConfiguration getInstance() {
        if (singleton == null) {
            singleton = new ProActiveConfiguration();
        }
        return singleton;
    }

    private ProActiveConfiguration() {
        this.properties = new CustomProperties();

        /* Properties are set from the lower priority to the higher priority sources. */

        // 1- Default config file
        this.properties.putAllFromConfigFile(this.getDefaultProperties());

        // 2- User config file
        this.properties.putAllFromConfigFile(this.getUserProperties());

        // 3- System java properties
        this.properties.putAllFromSystem(this.getsystemProperties());

        // Can't use setValue in this constructor
        System.setProperty(PAProperties.PA_OS.getKey(), OperatingSystem.getOperatingSystem().toString());
    }

    class CustomProperties extends Properties {
        /**
         * 
         */
        private static final long serialVersionUID = 42L;
        HashMap<String, String> exportedKeys = new HashMap<String, String>();

        public synchronized void putAllFromSystem(Map<? extends Object, ? extends Object> t) {
            for (Map.Entry<? extends Object, ? extends Object> entry : t.entrySet()) {
                put(entry.getKey(), entry.getValue(), false);
            }
        }

        public synchronized void putAllFromConfigFile(Map<? extends Object, ? extends Object> t) {
            for (Map.Entry<? extends Object, ? extends Object> entry : t.entrySet()) {
                put(entry.getKey(), entry.getValue(), true);
            }
        }

        public synchronized Object put(Object keyO, Object valueO, boolean exportAsSystem) {
            String key = (String) keyO;
            String value = (String) valueO;

            /*
             * Check the value of this property is valid according to its type.
             *
             * If the value is invalid, a warning message is printed and the value is SET.
             */

            PAProperties prop = PAProperties.getProperty(key);
            if (prop != null) {
                if (!prop.isValid(value)) {
                    logger.warn("Invalid value, " + value + " for key " + key + ". Must be a " +
                        prop.getType().toString());
                }
                if (prop.isSystemProperty()) {
                    logger.debug("Exported <" + key + ", " + value + "> as System property");

                    exportedKeys.put(key, System.getProperty(key));
                    System.setProperty(key, value);
                }
            } else {
                // This property is not known by ProActive
                if (key.startsWith("proactive.")) {
                    logger.warn("Property " + key + " is not declared inside " +
                        PAProperties.class.getSimpleName() + " , ignoring");
                } else {
                    logger.debug("System property " + key + " is not a ProActive property");
                    if (exportAsSystem) {
                        // it's not a proactive property and it was defined in a config file, so it need to be exported
                        logger.debug("Exported <" + key + ", " + value + "> as System property");

                        exportedKeys.put(key, System.getProperty(key));
                        System.setProperty(key, value);
                    }
                }
            }

            logger.debug("key:" + key + " --> value:" + value + (this.get(key) == null ? "" : " (OVERRIDE)"));

            return this.put(keyO, valueO);
        }

        @Override
        protected void finalize() throws Throwable {

            for (String key : exportedKeys.keySet()) {
                System.setProperty(key, exportedKeys.get(key));
            }
            exportedKeys = null;

            super.finalize();
        }
    }

    /**
     * Load the configuration, first look for user defined configuration files, firstly in the
     * system property Constants.PROPERTY_PA_CONFIGURATION_FILE, then a file called
     * .ProActiveConfiguration.xml at the user homedir. The default file is located in the same
     * directory as the ProActiceConfiguration class with the name proacticeConfiguration It is
     * obtained using <code>Class.getRessource()</code> If the property proactive.configuration is
     * set then its value is used as the configuration file
     */
    public synchronized static void load() {
        // Load them all !
        getInstance();
    }

    /**
     * returns the value of a property or null
     *
     * @param property
     *            the property
     * @return the value of the property
     */
    public String getProperty(String property) {
        return this.properties.getProperty(property);
    }

    /**
     * returns the value of a property or the default value
     *
     * @param property
     *            the property
     * @return the value of the property or the default value if the property does not exist
     */
    public String getProperty(String property, String defaultValue) {
        return this.properties.getProperty(property, defaultValue);
    }

    /**
     * set the value 'value' for the property key 'key'. <i>override any previous value</i>
     *
     * @param key
     *            the of the property
     * @param value
     *            the value of the property
     */
    protected void setProperty(String key, String value) {
        this.properties.put(key, value, true);
    }

    private Properties getDefaultProperties() {
        String defaultConfigFile = ProActiveConfiguration.class.getResource(PROACTIVE_CONFIG_FILENAME)
                .toString();
        logger.debug("Default Config File is: " + defaultConfigFile);

        return ProActiveConfigurationParser.parse(defaultConfigFile, null);

    }

    private Properties getUserProperties() {
        boolean defaultFile = false;
        Properties userProps = new Properties();

        /* Filename of the user configuration file */
        String fname = System.getProperty(PAProperties.PA_CONFIGURATION_FILE.getKey());
        if (fname == null) {
            defaultFile = true;
            fname = PROACTIVE_USER_CONFIG_FILENAME;
        }

        if (!fname.matches("^\\w{2,}+:.*$")) {
            // protocol prefix was not specified
            // using "file" protocol by default
            fname = FILE_PROTOCOL_PREFIX + fname;
        }

        URL u = null;
        try {
            u = new URL(fname);
            u.openStream();
            logger.debug("User Config File is: " + u.toExternalForm());
            userProps = ProActiveConfigurationParser.parse(u.toString(), userProps);
        } catch (Exception e) {
            if (!defaultFile) {
                logger.warn("Configuration file " + u.toExternalForm() + " not found");
            }
        }

        return userProps;
    }

    private Properties getsystemProperties() {
        return System.getProperties();
    }
}
