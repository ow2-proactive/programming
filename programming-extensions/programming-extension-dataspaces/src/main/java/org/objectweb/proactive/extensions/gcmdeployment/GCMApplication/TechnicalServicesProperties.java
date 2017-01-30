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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


public class TechnicalServicesProperties implements Iterable<Entry<String, HashMap<String, String>>> {

    public static final TechnicalServicesProperties EMPTY = new TechnicalServicesProperties();

    protected HashMap<String, HashMap<String, String>> data;

    /**
     * Construct an empty TechnicalServicesProperties
     */
    public TechnicalServicesProperties() {
        data = new HashMap<String, HashMap<String, String>>();
    }

    public TechnicalServicesProperties(HashMap<String, HashMap<String, String>> data) {
        this.data = data;
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    /**
     * Create a new TechnicalServicesProperties which is the combination of the properties passed as argument
     * with the current ones. The ones passed as argument override the current ones. 
     *    
     * @param techServ
     */
    public TechnicalServicesProperties getCombinationWith(TechnicalServicesProperties techServ) {

        @SuppressWarnings("unchecked")
        TechnicalServicesProperties res = new TechnicalServicesProperties((HashMap<String, HashMap<String, String>>) data.clone());

        if (techServ != null && !techServ.isEmpty()) {

            for (Map.Entry<String, HashMap<String, String>> entry : techServ) {

                HashMap<String, String> classProperties = res.data.get(entry.getKey());

                if (classProperties != null) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, String> cpClone = (HashMap<String, String>) classProperties.clone();
                    cpClone.putAll(entry.getValue());
                    res.data.put(entry.getKey(), cpClone);
                } else {
                    res.data.put(entry.getKey(), entry.getValue());
                }

            }
        }

        return res;
    }

    protected static void dumpMap(HashMap<String, String> hmap) {
        for (String entry : hmap.keySet()) {

            System.out.println("\tkey : " + entry + " - value : " + hmap.get(entry));
        }
    }

    public HashMap<String, String> getTechnicalServicesForClass(String serviceClass) {
        return data.get(serviceClass);
    }

    public Iterator<Map.Entry<String, HashMap<String, String>>> iterator() {
        return data.entrySet().iterator();
    }

}
