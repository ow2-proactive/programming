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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The singleton that holds all the known {@link PAProperty}
 *
 * When a module starts, it can register one or several {@link PAProperty} by using
 * the {@link #register(Class)} method.
 *
 * @since ProActive 4.3.0
 */
abstract public class PAProperties {

    private static HashMap<Class<?>, List<PAProperty>> map;

    static {
        // Loads the default central repository
        map = new HashMap<Class<?>, List<PAProperty>>();
        register(CentralPAPropertyRepository.class);
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
     *
     * @return All the loaded properties, sorted by declaring class
     */
    static public Map<Class<?>, List<PAProperty>> getAllProperties() {
        return map;
    }

}
