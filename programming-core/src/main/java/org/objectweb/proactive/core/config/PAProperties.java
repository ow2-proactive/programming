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
package org.objectweb.proactive.core.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.spi.ServiceRegistry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The singleton that holds all the known {@link PAProperty}
 *
 * Modules can register their own ProActive properties by providing a {@link PAPropertiesLoaderSPI}.
 * The SPI must be defined in META-INF/services/org.objectweb.proactive.core.config.PAProperties$PAPropertiesLoaderSPI
 * The SPI is automatically discovered by the JDK then all the PAProperty defined as static field are
 * automatically registered.
 *
 * @since ProActive 4.3.0
 */
@PublicAPI
abstract public class PAProperties {

    /**
     * This interface must be implemented by all the classes defining {@link PAProperties}
     *
     * The SPI must be put into META-INF/services/org.objectweb.proactive.core.config.PAProperties$PAPropertiesLoaderSPI
     */
    public interface PAPropertiesLoaderSPI {
    }

    static private final Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);

    static private HashMap<Class<?>, List<PAProperty>> map;

    static {
        // Loads the default central repository
        map = new HashMap<Class<?>, List<PAProperty>>();

        Iterator<PAPropertiesLoaderSPI> iter;
        iter = ServiceRegistry.lookupProviders(PAPropertiesLoaderSPI.class);

        while (iter.hasNext()) {
            try {
                PAPropertiesLoaderSPI spi = iter.next();
                logger.debug("Registering ProActive properties from " + spi.getClass());
                register(spi.getClass());
            } catch (Throwable err) {
                logger.warn("Failed to load ProActive property registry: " + err);
            }
        }
    }

    /**
     * Register a new set of {@link PAProperty}
     *
     * This method will register all the {@link PAProperty} defined as a static
     * field. Both public, protected and private fields are discovered.
     *
     * @param cl The class defining the properties
     */
    static synchronized public void register(Class<?> cl) {
        if (map.get(cl) != null) {
            // ERROR
            return;
        }

        Field[] fields = cl.getDeclaredFields();
        ArrayList<PAProperty> properties = new ArrayList<PAProperty>(fields.length);
        for (Field field : fields) {
            if (PAProperty.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    PAProperty prop = PAProperty.class.cast(field.get(null));
                    properties.add(prop);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        map.put(cl, properties);
    }

    /**
     * Get the {@link PAProperty} corresponding to the java property key
     *
     * @param name The name of the property
     * @return The property or null
     */
    static public synchronized PAProperty getProperty(String name) {
        for (List<PAProperty> list : map.values()) {
            for (PAProperty prop : list) {
                if (prop.getName().equals(name)) {
                    return prop;
                }
            }
        }

        return null;
    }

    /**
     * @return All the loaded properties, sorted by declaring class
     */
    static public Map<Class<?>, List<PAProperty>> getAllProperties() {
        return map;
    }

    private PAProperties() {
    }
}
