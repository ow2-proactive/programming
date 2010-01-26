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
package org.objectweb.proactive.extensions.timitspmd.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.objectweb.proactive.extensions.timitspmd.util.XMLHelper;


/**
 * This class construct a serie of benchmarks thanks to a configuration file.
 *
 * @author The ProActive Team
 */
public class ConfigReader {
    public static String PROJECT_PATH;

    //    static {
    //        String classesPath = ConfigReader.class.getResource(".").getPath();
    //        ConfigReader.PROJECT_PATH = classesPath.substring(0,
    //                classesPath.indexOf("/classes"));
    //    }
    private Document document;
    private Element eTimit;
    private HashMap<String, String> globalVariables; // <variable name,value>
    private Series[] series;

    public ConfigReader(String filename) {
        // Get the <timit> root tag from configuration file
        this.document = XMLHelper.readFile(filename);
        this.eTimit = this.document.getRootElement();

        // Get global variables
        this.globalVariables = new HashMap<String, String>();

        // Add the default global variable PROJECT_PATH        
        //this.globalVariables.put("PROJECT_PATH", PROJECT_PATH);
        @SuppressWarnings("unchecked")
        Iterator<Element> it = this.eTimit.getChild("globalVariables").getChildren().iterator();
        while (it.hasNext()) {
            Element var = it.next();
            this.globalVariables.put(var.getAttributeValue("name"), var.getAttributeValue("value"));
        }

        // Read and parse all <serie> tags
        @SuppressWarnings("unchecked")
        List serieList = this.eTimit.getChildren("series");
        XMLHelper.replaceVariables(serieList, this.globalVariables);
        this.series = Series.toArray(serieList);
    }

    /**
     * Retrieve the serie of benchmarks generated from configuration file
     * @return the Series array
     */
    public Series[] getSeries() {
        return this.series.clone();
    }

    /**
     * Retrieve the all variable/value couple which were in <globalVariable>
     * tag from configuration file
     * @return the hashmap of variable/value couples
     */
    public HashMap<String, String> getGlobalVariables() {
        return this.globalVariables;
    }
}
