/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.core.xml;

import java.util.HashMap;
import java.util.Map;


public interface VariableContract {
    /**
     * Returns the value of the variable name passed as parameter.
     * @param name The name of the variable.
     * @return The value of the variable.
     */
    public String getValue(String name);

    /**
     * Tells if this contract is closed or not.
     *
     * @return True if it is closed, false otherwise.
     */
    public boolean isClosed();

    /**
     * This methods tells if a variable name is acceptable
     * @param var The variable name, without the ${} wrapping.
     * @return true if the variable is legal, false otherwise.
     */
    public boolean isLegalName(String var);

    /**
     * Loads the variable contract from a Java Properties file format
     * @param file The file location.
     * @throws org.xml.sax.SAXException
     */
    public void load(String file) throws org.xml.sax.SAXException;

    /**
     * Loads a file with Variable Contract tags into the this instance.
     * @param file
     */
    public void loadXML(String file);

    /**
     * Method for setting a group of variables from the program.
     * @see #setVariableFromProgram(String name, String value, VariableContractType type)
     * @throws NullPointerException if the arguments are null.
     * @throws IllegalArgumentException if setting the value breaches the variable (contract) type
     */
    public void setVariableFromProgram(HashMap<String, String> map, VariableContractType type)
            throws NullPointerException;

    /**
     * Method for setting variables value from the deploying application.
     *
     * @param name
     *            The name of the variable.
     * @param value
     *            Value of the variable
     * @throws NullPointerException
     *             if the arguments are null.
     * @throws IllegalArgumentException
     *             if setting the value breaches the variable (contract) type
     */
    public void setVariableFromProgram(String name, String value, VariableContractType type);

    public Map<String, String> toMap();
}
